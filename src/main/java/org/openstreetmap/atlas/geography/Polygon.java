package org.openstreetmap.atlas.geography;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.IntStream;

import org.locationtech.jts.algorithm.match.HausdorffSimilarityMeasure;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.triangulate.ConformingDelaunayTriangulationBuilder;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.converters.WkbPolygonConverter;
import org.openstreetmap.atlas.geography.converters.WktPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.GeometryStreamer;
import org.openstreetmap.atlas.geography.converters.jts.JtsLocationConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPrecisionManager;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Surface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

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
    public static final Polygon CENTER = new Polygon(Location.CENTER);

    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTIPOLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();
    private static final JtsPolygonConverter JTS_POLYGON_CONVERTER = new JtsPolygonConverter();
    private static final JtsPointConverter JTS_POINT_CONVERTER = new JtsPointConverter();
    private static final JtsPolyLineConverter JTS_POLYLINE_CONVERTER = new JtsPolyLineConverter();

    private static final Logger logger = LoggerFactory.getLogger(Polygon.class);
    private static final long serialVersionUID = 2877026648358594354L;

    // Calculate sides starting from triangles
    private static final int MINIMUM_N_FOR_SIDE_CALCULATION = 3;
    private transient PreparedGeometry prepared;

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
        // This was Iterables.asList. `super` creates a new ArrayList, so we don't have to worry
        // about the backing array being modified.
        // This was 6% of a test run in a single validation (there were other validations run, so
        // this may be larger). After the new run, it was 3% (async Allocation Profiler)
        this(Arrays.asList(points));
    }

    @Override
    public JsonObject asGeoJsonGeometry()
    {
        return GeoJsonUtils.geometry(GeoJsonType.POLYGON, GeoJsonUtils.polygonToCoordinates(this));
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
        if (!this.first().equals(this.last()))
        {
            return new MultiIterable<>(this, Iterables.from(this.first()));
        }
        return this;
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
        if (this.prepared == null)
        {
            this.prepared = PreparedGeometryFactory.prepare(JTS_POLYGON_CONVERTER.convert(this));
        }
        return this.prepared.covers(JTS_POINT_CONVERTER.convert(location));
    }

    @Override
    public boolean fullyGeometricallyEncloses(final MultiPolygon multiPolygon)
    {
        if (this.prepared == null)
        {
            this.prepared = PreparedGeometryFactory.prepare(JTS_POLYGON_CONVERTER.convert(this));
        }
        return this.prepared.covers(JTS_MULTIPOLYGON_CONVERTER.backwardConvert(multiPolygon));
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
        if (this.prepared == null)
        {
            this.prepared = PreparedGeometryFactory.prepare(JTS_POLYGON_CONVERTER.convert(this));
        }
        if (polyLine instanceof Polygon)
        {
            return this.prepared.covers(JTS_POLYGON_CONVERTER.convert((Polygon) polyLine));
        }
        return this.prepared.covers(JTS_POLYLINE_CONVERTER.convert(polyLine));
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
        if (this.prepared == null)
        {
            this.prepared = PreparedGeometryFactory.prepare(JTS_POLYGON_CONVERTER.convert(this));
        }
        return this.prepared.covers(JTS_POLYGON_CONVERTER.convert(rectangle));
    }

    @Override
    public GeoJsonType getGeoJsonType()
    {
        return GeoJsonType.POLYGON;
    }

    /**
     * Returns a location that is the closest point within the polygon to the centroid. The function
     * delegates to the Geometry class which delegates to the InteriorPointPoint class. You can see
     * the javadocs in the link below. <a href=
     * "https://locationtech.github.io/jts/javadoc/org/locationtech/jts/algorithm/InteriorPointPoint.html"></a>
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
     * @see <a href=
     *      "http://www.gutenberg.org/files/19770/19770-pdf.pdf?session_id=374cbf5aca81b1a742aac0879dbea5eb35f914ea"></a>
     * @see <a href="http://mathforum.org/library/drmath/view/51879.html"></a>
     * @see <a href="http://mathforum.org/library/drmath/view/65316.html"></a>
     */
    public boolean isClockwise()
    {
        // Formula to calculate the area of triangle on a sphere is (A + B + C - Pi) * radius *
        // radius.
        // Equation (A + B + C - Pi) is called the spherical excess. We are going to divide our
        // polygon in triangles and then calculate the signed area of each triangle. Sum of the
        // areas of these triangles will be the area of this polygon
        double sphericalExcess = 0;
        Location previousLocation = null;

        for (final Location point : this.closedLoop())
        {
            final Location currentLocation = point;

            if (previousLocation != null)
            {
                // for the sake of simplicity we are using two vertices from the polygon and the
                // third vertex would be North Pole.
                // Please refer "Spherical Trigonometry by I.Todhunter".
                // Section starting on page 7 and 17 for triangle identities and trigonometric
                // functions.
                // Also look on page 71 for getting the area of triangle
                final double latitudeOne = previousLocation.getLatitude().asRadians();
                final double latitudeTwo = currentLocation.getLatitude().asRadians();
                final double deltaLongitude = currentLocation.getLongitude().asRadians()
                        - previousLocation.getLongitude().asRadians();

                final double alpha = Math
                        .sqrt((1 - Math.sin(latitudeOne)) / (1 + Math.sin(latitudeOne)))
                        * Math.sqrt((1 - Math.sin(latitudeTwo)) / (1 + Math.sin(latitudeTwo)));

                // You can derive this from the formula on Page 74, point 102 of the book
                sphericalExcess += 2 * Math.atan2(alpha * Math.sin(deltaLongitude),
                        1 + alpha * Math.cos(deltaLongitude));
            }
            previousLocation = currentLocation;
        }

        // Instead of area of polygon this method returns the spherical access as multiplying with
        // Earth (radius) ^ 2 is not going to change the sign of the area
        return sphericalExcess <= 0;
    }

    public boolean isSimilarTo(final Polygon other)
    {
        final double similarity = new HausdorffSimilarityMeasure()
                .measure(JTS_POLYGON_CONVERTER.convert(this), JTS_POLYGON_CONVERTER.convert(other));
        return similarity > SIMILARITY_THRESHOLD;
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
        if (this.prepared == null)
        {
            this.prepared = PreparedGeometryFactory.prepare(JTS_POLYGON_CONVERTER.convert(this));
        }
        return this.prepared.intersects(JTS_MULTIPOLYGON_CONVERTER.backwardConvert(multiPolygon));
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
        if (this.prepared == null)
        {
            this.prepared = PreparedGeometryFactory.prepare(JTS_POLYGON_CONVERTER.convert(this));
        }
        if (polyline instanceof Polygon)
        {
            return this.prepared.intersects(JTS_POLYGON_CONVERTER.convert((Polygon) polyline));
        }
        return this.prepared.intersects(JTS_POLYLINE_CONVERTER.convert(polyline));
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
     * @return This {@link Polygon} as Well Known Binary
     */
    @Override
    public byte[] toWkb()
    {
        return new WkbPolygonConverter().convert(this);
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
                if (hasNext())
                {
                    this.read = true;
                    return first();
                }
                else
                {
                    throw new NoSuchElementException();
                }
            }
        });
    }

    private void verifyVertexIndex(final int index)
    {
        if (index < 0 || index >= size())
        {
            throw new CoreException("Invalid Vertex Index {}.", index);
        }
    }
}
