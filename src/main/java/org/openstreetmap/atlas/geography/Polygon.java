package org.openstreetmap.atlas.geography;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.converters.WktPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.GeometryStreamer;
import org.openstreetmap.atlas.geography.converters.jts.JtsLocationConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPrecisionManager;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Surface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.triangulate.ConformingDelaunayTriangulationBuilder;

/**
 * A {@link Polygon} is a {@link PolyLine} with an extra {@link Segment} between the last
 * {@link Location} and the first {@link Location}.
 *
 * @author matthieun
 */
public class Polygon extends PolyLine implements GeometricSurface
{
    public static final Polygon SILICON_VALLEY = new Polygon(Location.TEST_3, Location.TEST_7,
            Location.TEST_4, Location.TEST_1, Location.TEST_5);
    public static final Polygon SILICON_VALLEY_2 = new Polygon(Location.TEST_3, Location.TEST_7,
            Location.TEST_2, Location.TEST_1, Location.TEST_5);
    public static final Polygon TEST_BUILDING = new Polygon(
            Location.forString("37.3909505256542,-122.03104734420775"),
            Location.forString("37.39031973417266,-122.03141212463377"),
            Location.forString("37.390106627742895,-122.03113317489623"),
            Location.forString("37.39084823550426,-122.03062891960144"),
            Location.forString("37.3909505256542,-122.03104734420775"));
    public static final Polygon TEST_BUILDING_PART = new Polygon(
            Location.forString("37.390234491673446,-122.03111171722412"),
            Location.forString("37.39020252571126,-122.0311439037323"),
            Location.forString("37.39018121506223,-122.03110367059708"),
            Location.forString("37.39021104996917,-122.0310714840889"),
            Location.forString("37.390234491673446,-122.03111171722412"));

    private static final JtsPolygonConverter JTS_POLYGON_CONVERTER = new JtsPolygonConverter();

    private static final Logger logger = LoggerFactory.getLogger(Polygon.class);
    private static final long serialVersionUID = 2877026648358594354L;

    // Calculate sides starting from triangles
    private static final int MINIMUM_N_FOR_SIDE_CALCULATION = 3;
    private transient Area awtArea;
    private java.awt.Polygon awtPolygon;
    private transient Boolean awtOverflows;

    /**
     * Generate a random polygon within bounds.
     *
     * @param numberPoints
     *            The number of points in the polygon
     * @param bounds
     *            The bounds for the points to be in
     * @return The random {@link Polygon}
     */
    public static Polygon random(final int numberPoints, final Rectangle bounds)
    {
        final List<Location> locations = new ArrayList<>();
        IntStream.range(0, numberPoints).forEach(index -> locations.add(Location.random(bounds)));
        return new Polygon(locations);
    }

    /**
     * Generate a Polygon from Well Known Text
     *
     * @param wkt
     *            The polygon in well known text
     * @return The parsed {@link Polygon}
     */
    public static Polygon wkt(final String wkt)
    {
        return new WktPolygonConverter().backwardConvert(wkt);
    }

    public Polygon(final Iterable<Location> points)
    {
        this(Iterables.asList(points));
    }

    public Polygon(final List<Location> points)
    {
        super(points);
    }

    public Polygon(final Location... points)
    {
        this(Iterables.iterable(points));
    }

    /**
     * The segments that belong to this {@link Polygon} that are attached to this vertex
     *
     * @param vertexIndex
     *            the index of the vertex
     * @return The segments that belong to this {@link Polygon} that are attached to this vertex
     */
    public List<Segment> attachedSegments(final int vertexIndex)
    {
        verifyVertexIndex(vertexIndex);
        final List<Segment> result = new ArrayList<>();
        // Previous
        if (vertexIndex > 0)
        {
            result.add(segmentForIndex(vertexIndex - 1));
        }
        else
        {
            result.add(segmentForIndex(size() - 1));
        }
        // Next
        result.add(segmentForIndex(vertexIndex));
        return result;
    }

    /**
     * This will return the centroid of a given polygon. It can handle complex polygons including
     * multiple polygons. This will not necessarily return a location that is contained within the
     * original polygon. For example if you have two concentric circles forming a donut shape, one
     * smaller one contained within the bigger one. The centroid of that polygon will be at the
     * center technically outside of the polygon. This is a very different concept to a
     * representative point.
     *
     * @return a Location object that is the centroid of the polygon
     */
    public Location center()
    {
        final Point point = JTS_POLYGON_CONVERTER.convert(this).getCentroid();
        return new JtsLocationConverter().backwardConvert(point.getCoordinate());
    }

    /**
     * @return An iterable of {@link Location}s that will return the first item again at the end.
     */
    public Iterable<Location> closedLoop()
    {
        return new MultiIterable<>(this, Iterables.from(this.first()));
    }

    /**
     * Tests if this {@link Polygon} fully encloses (geometrically contains) a {@link Location}
     * <p>
     * Here is the definition of contains (insideness) of awt point.
     * <p>
     * Definition of insideness: A point is considered to lie inside a Shape if and only if: it lies
     * completely inside the Shape boundary or it lies exactly on the Shape boundary and the space
     * immediately adjacent to the point in the increasing X direction is entirely inside the
     * boundary or it lies exactly on a horizontal boundary segment and the space immediately
     * adjacent to the point in the increasing Y direction is inside the boundary.
     * <p>
     * In the case of a massive polygon (larger than 75% of the earth's width) the JTS definition of
     * covers is used instead, which will return true if the location lies within the polygon or
     * anywhere on the boundary.
     * <p>
     *
     * @param location
     *            The {@link Location} to test
     * @return True if the {@link Polygon} contains the {@link Location}
     */
    @Override
    public boolean fullyGeometricallyEncloses(final Location location)
    {
        // if this value overflows, use JTS to correctly calculate covers
        if (awtOverflows())
        {
            final com.vividsolutions.jts.geom.Polygon polygon = JTS_POLYGON_CONVERTER.convert(this);
            final Point point = new JtsPointConverter().convert(location);
            return polygon.covers(point);
        }
        // for most cases use the faster awt covers
        else
        {
            return awtPolygon().contains(location.asAwtPoint());
        }
    }

    @Override
    public boolean fullyGeometricallyEncloses(final MultiPolygon multiPolygon)
    {
        return multiPolygon.outers().stream().allMatch(this::fullyGeometricallyEncloses);
    }

    /**
     * Tests if this {@link Polygon} fully encloses (geometrically contains) a {@link PolyLine}.
     * Note: this will return false for the case when the {@link Polygon} and given {@link PolyLine}
     * are stacked on top of each other - i.e. have an identical shape as one another.
     *
     * @param polyLine
     *            The {@link PolyLine} to test
     * @return True if this {@link Polygon} wraps (geometrically contains) the provided
     *         {@link PolyLine}
     */
    @Override
    public boolean fullyGeometricallyEncloses(final PolyLine polyLine)
    {
        final List<Segment> segments = polyLine.segments();
        for (final Segment segment : segments)
        {
            if (!fullyGeometricallyEncloses(segment))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if this {@link Polygon} fully encloses (geometrically contains) a {@link Rectangle}.
     * Note: this will return false for the case when the {@link Polygon} has an identical shape as
     * the given {@link Rectangle}.
     *
     * @param rectangle
     *            The {@link Rectangle} to test
     * @return True if this {@link Polygon} wraps (geometrically contains) the provided
     *         {@link Rectangle}
     */
    public boolean fullyGeometricallyEncloses(final Rectangle rectangle)
    {
        final Rectangle bounds = this.bounds();
        if (!bounds.fullyGeometricallyEncloses(rectangle))
        {
            // The item is not within the bounds of this Polygon
            return false;
        }
        // The item is within the bounds of this Polygon
        // if this value overflows, use JTS to correctly calculate covers
        if (awtOverflows())
        {
            final com.vividsolutions.jts.geom.Polygon polygon = JTS_POLYGON_CONVERTER.convert(this);
            return polygon.covers(JTS_POLYGON_CONVERTER.convert(rectangle));
        }
        // for most cases use the faster awt covers
        else
        {
            return awtArea().contains(rectangle.asAwtRectangle());
        }
    }

    /**
     * Tests if this {@link Polygon} wraps (geometrically contains) a {@link Segment}
     *
     * @param segment
     *            The {@link Segment} to test
     * @return True if this {@link Polygon} wraps (geometrically contains) the provided
     *         {@link Segment}
     */
    public boolean fullyGeometricallyEncloses(final Segment segment)
    {
        final Set<Location> intersections = this.intersections(segment);
        for (final Location intersection : intersections)
        {
            if (!intersection.equals(segment.start()) && !intersection.equals(segment.end()))
            {
                // This is a non-end intersection
                return false;
            }
        }
        return this.fullyGeometricallyEncloses(segment.middle());
    }

    /**
     * Returns a location that is the closest point within the polygon to the centroid. The function
     * delegates to the Geometry class which delegates to the InteriorPointPoint class. You can see
     * the javadocs in the link below. <a href=
     * "http://www.vividsolutions.com/jts/javadoc/com/vividsolutions/jts/algorithm/InteriorPointPoint">
     * http://www.vividsolutions.com/jts/javadoc/com/vividsolutions/jts/algorithm/InteriorPointPoint
     * </a> .html
     *
     * @return location that is the closest point within the polygon to the centroid
     */
    public Location interiorCenter()
    {
        final Point point = JTS_POLYGON_CONVERTER.convert(this).getInteriorPoint();
        return new JtsLocationConverter().backwardConvert(point.getCoordinate());
    }

    /**
     * @param expectedNumberOfSides
     *            Expected number of sides
     * @param threshold
     *            {@link Angle} threshold that decides whether a {@link Heading} difference between
     *            segments should be counted towards heading change count or not
     * @return true if this {@link Polygon} has approximately n sides while ignoring {@link Heading}
     *         differences between inner segments that are below given threshold.
     */
    public boolean isApproximatelyNSided(final int expectedNumberOfSides, final Angle threshold)
    {
        // Ignore if polygon doesn't have enough inner shape points
        if (expectedNumberOfSides < MINIMUM_N_FOR_SIDE_CALCULATION
                || this.size() < expectedNumberOfSides)
        {
            return false;
        }

        // An N sided shape should have (n-1) heading changes
        final int expectedHeadingChangeCount = expectedNumberOfSides - 1;

        // Fetch segments and count them
        final List<Segment> segments = this.segments();
        final int segmentSize = segments.size();

        // Index to keep track of segment to work on
        int segmentIndex = 0;

        // Keep track of heading changes
        int headingChangeCount = 0;

        // Find initial heading
        Optional<Heading> previousHeading = Optional.empty();
        while (segmentIndex < segmentSize)
        {
            // Make sure we start with some heading. Edges with single points do not have heading.
            previousHeading = segments.get(segmentIndex++).heading();
            if (previousHeading.isPresent())
            {
                break;
            }
        }

        // Make sure we start with some heading
        if (!previousHeading.isPresent())
        {
            logger.trace("{} doesn't have a heading to calculate number of sides.", this);
            return false;
        }

        // Go over rest of the segments and count heading changes
        while (segmentIndex < segmentSize && headingChangeCount <= expectedHeadingChangeCount)
        {
            final Optional<Heading> nextHeading = segments.get(segmentIndex++).heading();

            // If heading difference is greater than threshold, then increment heading
            // change counter and update previous heading, which is used as reference
            if (nextHeading.isPresent()
                    && previousHeading.get().difference(nextHeading.get()).isGreaterThan(threshold))
            {
                headingChangeCount++;
                previousHeading = nextHeading;
            }
        }

        return headingChangeCount == expectedHeadingChangeCount;
    }

    /**
     * @return True if this {@link Polygon} is arranged clockwise, false otherwise.
     * @see <a href="http://stackoverflow.com/questions/1165647"></a>
     */
    public boolean isClockwise()
    {
        long sum = 0;
        long lastLatitude = Long.MIN_VALUE;
        long lastLongitude = Long.MIN_VALUE;
        for (final Location point : this)
        {
            if (lastLongitude != Long.MIN_VALUE)
            {
                sum += (point.getLongitude().asDm7() - lastLongitude)
                        * (point.getLatitude().asDm7() + lastLatitude);
            }
            lastLongitude = point.getLongitude().asDm7();
            lastLatitude = point.getLatitude().asDm7();
        }
        return sum >= 0;
    }

    public int nextSegmentIndex(final int currentVertexIndex)
    {
        verifyVertexIndex(currentVertexIndex);
        return currentVertexIndex;
    }

    public int nextVertexIndex(final int currentVertexIndex)
    {
        verifyVertexIndex(currentVertexIndex);
        if (currentVertexIndex == size() - 1)
        {
            return 0;
        }
        else
        {
            return currentVertexIndex + 1;
        }
    }

    @Override
    public boolean overlaps(final MultiPolygon multiPolygon)
    {
        for (final Polygon outer : multiPolygon.outers())
        {
            final List<Polygon> inners = multiPolygon.innersOf(outer);
            if (this.overlaps(outer))
            {
                boolean result = true;
                for (final Polygon inner : inners)
                {
                    if (inner.fullyGeometricallyEncloses(this))
                    {
                        // The feature is fully inside an inner polygon, hence not overlapped
                        result = false;
                        break;
                    }
                }
                if (result)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests if this {@link Polygon} intersects/overlaps the given {@link PolyLine} at any point.
     * This is different than the {@link #fullyGeometricallyEncloses(PolyLine)} method, in that it
     * doesn't require full containment within the {@link Polygon}, just any overlap.
     *
     * @param polyline
     *            The {@link PolyLine} to test
     * @return True if this {@link Polygon} intersects/overlaps the given {@link PolyLine}.
     */
    @Override
    public boolean overlaps(final PolyLine polyline)
    {
        return overlapsInternal(polyline, true);
    }

    public int previousSegmentIndex(final int currentVertexIndex)
    {
        return previousVertexIndex(currentVertexIndex);
    }

    public int previousVertexIndex(final int currentVertexIndex)
    {
        verifyVertexIndex(currentVertexIndex);
        if (currentVertexIndex == 0)
        {
            return size() - 1;
        }
        else
        {
            return currentVertexIndex - 1;
        }
    }

    @Override
    public Polygon reversed()
    {
        return new Polygon(super.reversed().getPoints());
    }

    public Segment segmentForIndex(final int index)
    {
        if (index >= size())
        {
            throw new CoreException("Invalid index");
        }
        return new Segment(this.get(index),
                index == size() - 1 ? this.get(0) : this.get(index + 1));
    }

    @Override
    public List<Segment> segments()
    {
        final List<Segment> result = super.segments();
        // close the loop
        result.add(new Segment(last(), first()));
        return result;
    }

    /**
     * @return The surface of this polygon. Not valid if the polygon self-intersects, and/or
     *         overlaps itself
     * @see "http://www.mathopenref.com/coordpolygonarea2.html"
     */
    @Override
    public Surface surface()
    {
        long dm7Squared = 0L;
        final Iterator<Location> loopOnItself = loopOnItself().iterator();
        if (!loopOnItself.hasNext())
        {
            return Surface.forDm7Squared(0);
        }
        Location current = loopOnItself.next();
        Location next = null;
        while (loopOnItself.hasNext())
        {
            next = loopOnItself.next();
            dm7Squared += (current.getLongitude().asDm7() + next.getLongitude().asDm7())
                    * (current.getLatitude().asDm7() - next.getLatitude().asDm7());
            current = next;
        }
        return Surface.forDm7Squared(Math.abs(Math.round(dm7Squared / 2.0)));
    }

    /**
     * @return The approximate surface area of this polygon if it were projected onto the Earth. Not
     *         valid if the polygon self-intersects, and/or overlaps itself. Uses "Some Algorithms
     *         for Polygons on a Sphere" paper as reference.
     * @see "https://trs.jpl.nasa.gov/bitstream/handle/2014/41271/07-0286.pdf"
     */
    @Override
    public Surface surfaceOnSphere()
    {
        double dm7 = 0L;

        final List<Location> locations = Lists.newArrayList(this.closedLoop());
        if (locations.size() > 2)
        {
            double radians = 0L;
            for (int index = 0; index < locations.size() - 1; index++)
            {
                radians += (locations.get(index + 1).getLongitude().asRadians()
                        - locations.get(index).getLongitude().asRadians())
                        * (2 + Math.sin(locations.get(index).getLatitude().asRadians())
                                + Math.sin(locations.get(index + 1).getLatitude().asRadians()));
            }
            radians = Math.abs(radians / 2.0);

            // Calculations are in Radians, convert to Degrees.
            dm7 = radians * ((double) Angle.DM7_PER_RADIAN * (double) Angle.DM7_PER_RADIAN);
        }

        return Surface.forDm7Squared(Math.round(dm7));
    }

    /**
     * @return This {@link Polygon} as Well Known Text
     */
    @Override
    public String toWkt()
    {
        return new WktPolygonConverter().convert(this);
    }

    /**
     * Triangulate this {@link Polygon}, using the JTS library.
     *
     * @return All the triangles that form this {@link Polygon}.
     */
    public List<Polygon> triangles()
    {
        final ConformingDelaunayTriangulationBuilder trianguler = new ConformingDelaunayTriangulationBuilder();
        // Populate the delaunay triangulation builder
        trianguler.setSites(JTS_POLYGON_CONVERTER.convert(this));
        final GeometryCollection triangleCollection = (GeometryCollection) trianguler
                .getTriangles(JtsPrecisionManager.getGeometryFactory());
        // Get the output and convert back to Core Polygons, filter out the extraneous polygons from
        // the Delaunay triangulation.
        return Iterables.stream(GeometryStreamer.streamPolygons(triangleCollection))
                .map(JTS_POLYGON_CONVERTER.revert())
                .filter(polygon -> fullyGeometricallyEncloses(polygon.center())).collectToList();
    }

    /**
     * Remove a vertex
     *
     * @param index
     *            The index of the vertex to remove
     * @return The new {@link Polygon} without the specified vertex
     */
    public Polygon withoutVertex(final int index)
    {
        if (index < 0 || index >= this.size())
        {
            throw new CoreException("{} is not a vertex index of {}", index, this);
        }
        final List<Location> vertices = Iterables.asList(this);
        vertices.remove(index);
        return new Polygon(vertices);
    }

    /**
     * Remove a vertex
     *
     * @param vertex
     *            The vertex to remove
     * @return The new {@link Polygon} without the specified vertex
     */
    public Polygon withoutVertex(final Location vertex)
    {
        int index = 0;
        for (final Location location : this)
        {
            if (location.equals(vertex))
            {
                return withoutVertex(index);
            }
            index++;
        }
        throw new CoreException("{} is not a vertex of {}", vertex, this);
    }

    protected Area awtArea()
    {
        if (this.awtArea == null)
        {
            this.awtArea = new Area(awtPolygon());
        }
        return this.awtArea;
    }

    private boolean awtOverflows()
    {
        if (this.awtOverflows == null)
        {
            final Rectangle bounds = bounds();
            this.awtOverflows = bounds.width().asDm7() <= 0 || bounds.height().asDm7() <= 0;
        }
        return this.awtOverflows;
    }

    private java.awt.Polygon awtPolygon()
    {
        if (this.awtPolygon == null)
        {
            final int size = size();
            final int[] xArray = new int[size];
            final int[] yArray = new int[size];
            int index = 0;
            for (final Location location : this)
            {
                xArray[index] = (int) location.getLongitude().asDm7();
                yArray[index] = (int) location.getLatitude().asDm7();
                index++;
            }
            this.awtPolygon = new java.awt.Polygon(xArray, yArray, size);
        }
        return this.awtPolygon;
    }

    private Iterable<Location> loopOnItself()
    {
        return new MultiIterable<>(this, () -> new Iterator<Location>()
        {
            private boolean read = false;

            @Override
            public boolean hasNext()
            {
                return !this.read;
            }

            @Override
            public Location next()
            {
                if (!this.read)
                {
                    this.read = true;
                    return first();
                }
                return null;
            }
        });
    }

    private boolean overlapsInternal(final PolyLine polyline, final boolean runReverseCheck)
    {
        for (final Location location : polyline)
        {
            if (fullyGeometricallyEncloses(location))
            {
                return true;
            }
        }
        if (runReverseCheck && polyline instanceof Polygon
                && ((Polygon) polyline).overlapsInternal(this, false))
        {
            return true;
        }
        return this.intersects(polyline);
    }

    private void verifyVertexIndex(final int index)
    {
        if (index < 0 || index >= size())
        {
            throw new CoreException("Invalid Vertex Index {}.", index);
        }
    }
}
