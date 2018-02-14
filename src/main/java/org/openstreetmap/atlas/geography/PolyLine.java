package org.openstreetmap.atlas.geography;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Snapper.SnappedLocation;
import org.openstreetmap.atlas.geography.clipping.Clip;
import org.openstreetmap.atlas.geography.clipping.Clip.ClipType;
import org.openstreetmap.atlas.geography.converters.WktLocationConverter;
import org.openstreetmap.atlas.geography.converters.WktPolyLineConverter;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.geography.matching.PolyLineMatch;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Ratio;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PolyLine is a set of {@link Location}s in a specific order
 *
 * @author matthieun
 * @author mgostintsev
 * @author Sid
 */
public class PolyLine implements Collection<Location>, Located, Serializable
{
    private static final long serialVersionUID = -3291779878869865427L;
    protected static final int SIMPLE_STRING_LENGTH = 200;
    private static final Logger logger = LoggerFactory.getLogger(PolyLine.class);

    public static final PolyLine TEST_POLYLINE = new PolyLine(Location.TEST_3, Location.TEST_7,
            Location.TEST_4, Location.TEST_1, Location.TEST_5);

    public static final String SEPARATOR = ":";

    private final List<Location> points;

    public static GeoJsonObject asGeoJson(final Iterable<? extends Iterable<Location>> geometries)
    {
        return new GeoJsonBuilder().create(Iterables.translate(geometries,
                geometry -> new LocationIterableProperties(geometry, new HashMap<>())));
    }

    /**
     * Generate a random {@link PolyLine} within bounds.
     *
     * @param numberPoints
     *            The number of points in the {@link PolyLine}
     * @param bounds
     *            The bounds for the points to be in
     * @return The random {@link PolyLine}
     */
    public static PolyLine random(final int numberPoints, final Rectangle bounds)
    {
        final List<Location> locations = new ArrayList<>();
        IntStream.range(0, numberPoints).forEach(index -> locations.add(Location.random(bounds)));
        return new PolyLine(locations);
    }

    public static void saveAsGeoJson(final Iterable<? extends Iterable<Location>> geometries,
            final WritableResource resource)
    {
        final JsonWriter writer = new JsonWriter(resource);
        writer.write(asGeoJson(geometries).jsonObject());
        writer.close();
    }

    /**
     * Create a {@link PolyLine} from Well Known Text
     *
     * @param wkt
     *            The Well Known Text
     * @return The {@link PolyLine}
     */
    public static PolyLine wkt(final String wkt)
    {
        return new WktPolyLineConverter().backwardConvert(wkt);
    }

    public PolyLine(final Iterable<? extends Location> points)
    {
        this(Iterables.asList(points));
    }

    public PolyLine(final List<? extends Location> points)
    {
        if (points.isEmpty())
        {
            throw new CoreException("Cannot have an empty PolyLine or Polygon.");
        }
        this.points = new ArrayList<>(points);
    }

    public PolyLine(final Location... points)
    {
        this(Iterables.iterable(points));
    }

    @Override
    public boolean add(final Location e)
    {
        throw new IllegalAccessError("Cannot add a Location to a PolyLine.");
    }

    @Override
    public boolean addAll(final Collection<? extends Location> collection)
    {
        throw new IllegalAccessError("Cannot add Locations to a PolyLine.");
    }

    /**
     * Return a {@link List} of {@link Tuple} that contains the Angle {@link Angle} and
     * {@link Location} of all {@link Angle}s that are greater than or equal to the target
     * {@link Angle}.
     *
     * @param target
     *            The threshold {@link Angle} used for comparison.
     * @return The {@link List} of {@link Tuple} that contains the {@link Angle} and
     *         {@link Location} of all results
     */
    public List<Tuple<Angle, Location>> anglesGreaterThanOrEqualTo(final Angle target)
    {
        final List<Tuple<Angle, Location>> result = new ArrayList<>();
        final List<Segment> segments = segments();
        if (segments.isEmpty() || segments.size() == 1)
        {
            return result;
        }

        for (int i = 1; i < segments.size(); i++)
        {
            final Segment first = segments.get(i - 1);
            final Segment second = segments.get(i);
            final Optional<Heading> firstHeading = first.heading();
            final Optional<Heading> secondHeading = second.heading();
            if (firstHeading.isPresent() && secondHeading.isPresent())
            {
                final Angle candidate = firstHeading.get().difference(secondHeading.get());
                if (candidate.isGreaterThanOrEqualTo(target))
                {
                    final Tuple<Angle, Location> tuple = Tuple.createTuple(candidate, first.end());
                    result.add(tuple);
                }
            }
        }

        return result;
    }

    /**
     * Return a {@link List} of {@link Tuple} that contains the {@link Angle} and {@link Location}
     * of all {@link Angle}s that are less than or equal to the target {@link Angle}.
     *
     * @param target
     *            The threshold {@link Angle} used for comparison.
     * @return The {@link List} of {@link Tuple} that contains the {@link Angle} and
     *         {@link Location} of all results
     */
    public List<Tuple<Angle, Location>> anglesLessThanOrEqualTo(final Angle target)
    {
        final List<Tuple<Angle, Location>> result = new ArrayList<>();
        final List<Segment> segments = segments();
        if (segments.isEmpty() || segments.size() == 1)
        {
            return result;
        }

        for (int i = 1; i < segments.size(); i++)
        {
            final Segment first = segments.get(i - 1);
            final Segment second = segments.get(i);
            final Optional<Heading> firstHeading = first.heading();
            final Optional<Heading> secondHeading = second.heading();
            if (firstHeading.isPresent() && secondHeading.isPresent())
            {
                final Angle candidate = firstHeading.get().difference(secondHeading.get());
                if (candidate.isLessThanOrEqualTo(target))
                {
                    final Tuple<Angle, Location> tuple = Tuple.createTuple(candidate, first.end());
                    result.add(tuple);
                }
            }
        }

        return result;
    }

    /**
     * Append the given {@link PolyLine} to this one, if possible.
     *
     * @param other
     *            The {@link PolyLine} to append
     * @return the new, combined {@link PolyLine}
     */
    public PolyLine append(final PolyLine other)
    {
        if (this.last().equals(other.first()))
        {
            return new PolyLine(new MultiIterable<>(this, other.truncate(1, 0)));
        }
        else
        {
            throw new CoreException(
                    "Cannot append {} to {} - the end and start points do not match.",
                    other.toWkt(), this.toWkt());
        }
    }

    public GeoJsonObject asGeoJson()
    {
        final List<Iterable<Location>> geometries = new ArrayList<>();
        geometries.add(this);
        return asGeoJson(geometries);
    }

    /**
     * Return the average distance from this {@link PolyLine}'s shape points to the other shape, and
     * the other shape's shape points to this polyline.
     *
     * @param other
     *            The other shape to compare to
     * @return The two way cost distance to the other {@link PolyLine}
     */
    public Distance averageDistanceTo(final PolyLine other)
    {
        return averageOneWayDistanceTo(other).add(other.averageOneWayDistanceTo(this))
                .scaleBy(Ratio.HALF);
    }

    /**
     * Return the average distance from this {@link PolyLine}'s shape points to the other shape,
     * using a one-way snapping.
     *
     * @param other
     *            The other shape to compare to
     * @return The one way cost distance to the other {@link PolyLine}
     */
    public Distance averageOneWayDistanceTo(final PolyLine other)
    {
        Distance costDistance = Distance.ZERO;
        for (final Location shapePoint : this)
        {
            costDistance = costDistance.add(shapePoint.snapTo(other).getDistance());
        }
        return costDistance.scaleBy(1.0 / this.size());
    }

    /**
     * Return a sub-{@link PolyLine} of this {@link PolyLine}
     *
     * @param start
     *            The start location to include
     * @param startOccurrence
     *            The occurrence index starting from 0 for the end location, in case of self
     *            intersecting or ring polylines.
     * @param end
     *            The end location to include
     * @param endOccurrence
     *            The occurrence index starting from 0 for the end location, in case of self
     *            intersecting or ring polylines.
     * @return The sub-{@link PolyLine} including start and end
     */
    public PolyLine between(final Location start, final int startOccurrence, final Location end,
            final int endOccurrence)
    {
        final List<Location> result = new ArrayList<>();
        boolean started = false;
        int startIndex = 0;
        int endIndex = 0;
        for (final Location location : this)
        {
            if (location.equals(start) && startOccurrence == startIndex++)
            {
                started = true;
            }
            if (location.equals(end) && endOccurrence == endIndex++)
            {
                if (!started)
                {
                    throw new CoreException(
                            "Found end first! {}(occurrence {}) and {}(occurrence {}) are not in order with respect to {}",
                            start, startOccurrence, end, endOccurrence, this.toWkt());
                }
                started = false;
                result.add(location);
                // Break here to avoid confusion with self-intersecting polylines.
                break;
            }
            if (started)
            {
                result.add(location);
            }
        }
        if (started)
        {
            throw new CoreException("(Start was {}) End {} is not in polyLine {}", start, end,
                    this);
        }
        return new PolyLine(result);
    }

    @Override
    public Rectangle bounds()
    {
        return Rectangle.forLocations(this);
    }

    @Override
    public void clear()
    {
        throw new IllegalAccessError("A polyline is immutable");
    }

    /**
     * Clip this feature on a {@link MultiPolygon}
     *
     * @param clipping
     *            The {@link MultiPolygon} to clip to
     * @param clipType
     *            The clip type (AND, OR, XOR or NOT).
     * @return The clip object containing the clipped features.
     */
    public Clip clip(final MultiPolygon clipping, final ClipType clipType)
    {
        return new Clip(clipType, this, clipping);
    }

    /**
     * Clip this feature on a {@link Polygon}
     *
     * @param clipping
     *            The {@link Polygon} to clip to
     * @param clipType
     *            The clip type (AND, OR, XOR or NOT).
     * @return The clip object containing the clipped features.
     */
    public Clip clip(final Polygon clipping, final ClipType clipType)
    {
        return new Clip(clipType, this, clipping);
    }

    /**
     * @param location
     *            The {@link Location} to test
     * @return True if one of the vertices of this {@link PolyLine} is the provided {@link Location}
     */
    public boolean contains(final Location location)
    {
        for (final Location thisLocation : this)
        {
            if (thisLocation.equals(location))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public final boolean contains(final Object object)
    {
        if (object instanceof Location)
        {
            return contains((Location) object);
        }
        if (object instanceof Segment)
        {
            return contains((Segment) object);
        }
        throw new IllegalAccessError(
                "A polyline can contain a Segment or Location only. Maybe you meant \"covers\"?");
    }

    /**
     * @param segment
     *            The {@link Segment} to test
     * @return True if one of the segments of this {@link PolyLine} is the provided {@link Segment}
     */
    public boolean contains(final Segment segment)
    {
        final List<Segment> segments = this.segments();
        for (final Segment thisSegment : segments)
        {
            if (thisSegment.equals(segment))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(final Collection<?> collection)
    {
        throw new IllegalAccessError();
    }

    /**
     * Get the cost {@link Distance} to the set of {@link Segment}s that are connected and that
     * provide a new {@link PolyLine} that is the closest in shape to this {@link PolyLine}
     *
     * @param candidates
     *            The candidate {@link PolyLine}s to match to this {@link PolyLine}
     * @return The best reconstructed match from this PolyLine.
     */
    public PolyLineMatch costDistanceToOneWay(final Iterable<PolyLine> candidates)
    {
        return new PolyLineMatch(this, Iterables.asList(candidates));
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof PolyLine)
        {
            final PolyLine that = (PolyLine) other;
            return Iterables.equals(this, that);
        }
        return false;
    }

    /**
     * Tests if this {@link PolyLine} has the same shape as another {@link PolyLine}. This is
     * different from equals as some {@link PolyLine}s that are different can still have the same
     * shape, by being reversed or by self intersecting in the same point for example.
     *
     * @param other
     *            The other {@link PolyLine} to compare to
     * @return True if they both have the same shape
     */
    public boolean equalsShape(final PolyLine other)
    {
        return this.overlapsShapeOf(other) && other.overlapsShapeOf(this);
    }

    /**
     * @return the final {@link Heading} for this {@link PolyLine}, based on the {@link Heading} of
     *         the last {@link Segment}.
     */
    public Optional<Heading> finalHeading()
    {
        final List<Segment> segments = this.segments();
        return segments.size() > 0 ? segments.get(segments.size() - 1).heading() : Optional.empty();
    }

    /**
     * @return The first {@link Location} of this {@link PolyLine}
     */
    public Location first()
    {
        return size() > 0 ? get(0) : null;
    }

    /**
     * @param index
     *            The index to query
     * @return The {@link Location} at the index provided in this {@link PolyLine}
     */
    public Location get(final int index)
    {
        if (index < 0 || index >= size())
        {
            throw new CoreException("Cannot get a Location with index " + index
                    + ", which is not between 0 and " + size());
        }
        return this.points.get(index);
    }

    @Override
    public int hashCode()
    {
        int result = 0;
        for (final Location location : this)
        {
            result += location.hashCode();
        }
        return result;
    }

    /**
     * @return The difference, if available, between the last {@link Segment}'s {@link Heading} and
     *         the first {@link Segment}'s {@link Heading}
     */
    public Optional<Angle> headingDifference()
    {
        if (this.size() <= 1)
        {
            return Optional.empty();
        }
        if (this.size() == 2)
        {
            return Optional.of(Angle.NONE);
        }
        else
        {
            final List<Segment> segments = this.segments();
            final Segment first = segments.get(0);
            final Segment last = segments.get(segments.size() - 1);
            final Optional<Heading> heading1 = first.heading();
            if (!heading1.isPresent())
            {
                return Optional.empty();
            }
            final Optional<Heading> heading2 = last.heading();
            if (!heading2.isPresent())
            {
                return Optional.empty();
            }
            return Optional.of(heading2.get().subtract(heading1.get()));
        }
    }

    /**
     * @return the initial {@link Heading} for this {@link PolyLine}, based on the {@link Heading}
     *         of the first {@link Segment}.
     */
    public Optional<Heading> initialHeading()
    {
        final List<Segment> segments = this.segments();
        return segments.size() > 0 ? segments.get(0).heading() : Optional.empty();
    }

    /**
     * @return All the locations in this {@link PolyLine} except the first and last.
     */
    public Iterable<Location> innerLocations()
    {
        return this.truncate(1, 1);
    }

    public Set<Location> intersections(final PolyLine candidate)
    {
        final Set<Location> result = new HashSet<>();
        if (this instanceof Segment)
        {
            result.addAll(candidate.intersections((Segment) this));
        }
        else
        {
            final List<Segment> segments = this.segments();
            segments.forEach(segment ->
            {
                final Set<Location> intersections = segment.intersections(candidate);
                result.addAll(intersections);
            });
        }
        return result;
    }

    public Set<Location> intersections(final Segment candidate)
    {
        final Set<Location> result = new HashSet<>();
        final List<Segment> segments = this.segments();
        segments.forEach(segment ->
        {
            final Location intersection = segment.intersection(candidate);
            if (intersection != null)
            {
                result.add(intersection);
            }
        });
        return result;
    }

    /**
     * Test if two {@link PolyLine}s intersect.
     *
     * @param other
     *            The other {@link PolyLine}
     * @return True if this {@link PolyLine} intersects the other at least once.
     */
    public boolean intersects(final PolyLine other)
    {
        final List<Segment> segments = this.segments();
        final List<Segment> otherSegments = other.segments();

        for (final Segment segment : segments)
        {
            for (final Segment otherSegment : otherSegments)
            {
                if (segment.intersects(otherSegment))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public final boolean isEmpty()
    {
        return this.points.isEmpty();
    }

    /**
     * @return True if this {@link PolyLine} is a single point, i.e. all the points are the same.
     */
    public boolean isPoint()
    {
        Location firstPoint = null;
        for (final Location point : this.points)
        {
            if (firstPoint == null)
            {
                firstPoint = point;
                continue;
            }
            if (!point.equals(firstPoint))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<Location> iterator()
    {
        return this.points.iterator();
    }

    public Location last()
    {
        return this.points.size() > 0 ? get(size() - 1) : null;
    }

    public Distance length()
    {
        Distance result = Distance.ZERO;
        final List<Segment> segments = this.segments();
        for (final Segment segment : segments)
        {
            result = result.add(segment.length());
        }
        return result;
    }

    /**
     * @return The biggest Angle in this {@link PolyLine}
     */
    public Angle maximumAngle()
    {
        final List<Segment> segments = segments();
        if (segments.isEmpty())
        {
            return null;
        }
        if (segments.size() == 1)
        {
            return Angle.NONE;
        }
        Angle maximum = Angle.NONE;
        for (int i = 1; i < segments.size(); i++)
        {
            final Segment first = segments.get(i - 1);
            final Segment second = segments.get(i);
            final Optional<Heading> firstHeading = first.heading();
            final Optional<Heading> secondHeading = second.heading();
            if (firstHeading.isPresent() && secondHeading.isPresent())
            {
                final Angle candidate = firstHeading.get().difference(secondHeading.get());
                if (candidate.isGreaterThan(maximum))
                {
                    maximum = candidate;
                }
            }
        }
        return maximum;
    }

    /**
     * @return The location of the biggest Angle in this {@link PolyLine}
     */
    public Optional<Location> maximumAngleLocation()
    {
        final List<Segment> segments = segments();
        if (segments.isEmpty() || segments.size() == 1)
        {
            return Optional.empty();
        }

        Angle maximum = Angle.NONE;
        Location maximumAngleLocation = null;
        for (int i = 1; i < segments.size(); i++)
        {
            final Segment first = segments.get(i - 1);
            final Segment second = segments.get(i);
            final Optional<Heading> firstHeading = first.heading();
            final Optional<Heading> secondHeading = second.heading();

            if (firstHeading.isPresent() && secondHeading.isPresent())
            {
                final Angle candidate = firstHeading.get().difference(secondHeading.get());
                if (candidate.isGreaterThan(maximum) || maximumAngleLocation == null)
                {
                    maximum = candidate;
                    maximumAngleLocation = first.end();
                }
            }
        }
        return Optional.ofNullable(maximumAngleLocation);
    }

    public Location middle()
    {
        return offsetFromStart(Ratio.HALF);
    }

    /**
     * Get the number of times a location appears in this {@link PolyLine}. Most useful for self
     * intersecting or ring {@link PolyLine}s.
     *
     * @param node
     *            The location to test
     * @return The number of occurrences in this {@link PolyLine}. 0 if it never shows up.
     */
    public int occurrences(final Location node)
    {
        int result = 0;
        for (final Location location : this)
        {
            if (location.equals(node))
            {
                result++;
            }
        }
        return result;
    }

    /**
     * Get the offset from the start of the node's location
     *
     * @param node
     *            The location to test
     * @param occurrenceIndex
     *            In case of a self intersecting polyline (one or more locations appear more than
     *            once), indicate the index at which this method should return the location. 0 would
     *            be first occurrence, 1 second, etc.
     * @return The offset ratio from the start of the {@link PolyLine}
     */
    public Ratio offsetFromStart(final Location node, final int occurrenceIndex)
    {
        final Distance max = this.length();
        Distance candidate = Distance.ZERO;
        Location previous = this.first();
        int index = 0;
        for (final Location location : this)
        {
            candidate = candidate.add(previous.distanceTo(location));
            if (location.equals(node) && occurrenceIndex == index++)
            {
                return Ratio.ratio(candidate.asMeters() / max.asMeters());
            }
            previous = location;
        }
        throw new CoreException("The location {} is not a node of the PolyLine", node);
    }

    public Location offsetFromStart(final Ratio ratio)
    {
        final Distance length = length();
        final Distance stop = length.scaleBy(ratio);
        Distance accumulated = Distance.ZERO;
        final List<Segment> segments = this.segments();

        for (final Segment segment : segments)
        {
            if (accumulated.add(segment.length()).isGreaterThan(stop))
            {
                // This is the proper segment
                final Ratio segmentRatio = Ratio.ratio(
                        stop.substract(accumulated).asMeters() / segment.length().asMeters());
                return segment.offsetFromStart(segmentRatio);
            }
            if (accumulated.add(segment.length()).equals(stop))
            {
                return segment.end();
            }
            accumulated = accumulated.add(segment.length());
        }
        throw new CoreException("This exception should never be thrown.");
    }

    /**
     * @return The overall heading of the {@link PolyLine}: the heading between the start point and
     *         the end point.
     */
    public Optional<Heading> overallHeading()
    {
        if (this.isPoint())
        {
            logger.warn("Cannot compute a segment's heading when the polyline has zero length : {}",
                    this);
            return Optional.empty();
        }
        return Optional.ofNullable(this.first().headingTo(this.last()));
    }

    /**
     * Tests if this {@link PolyLine} has at least the same shape as another {@link PolyLine}. If
     * this {@link PolyLine} is made up of {@link Segment}s ABC and the given {@link PolyLine} is
     * made up of BC, this would return true, despite the excess {@link Segment}.
     *
     * @param other
     *            The other {@link PolyLine} to compare to
     * @return True if this {@link PolyLine} has at least the same shape as the other (but possibly
     *         more)
     */
    public boolean overlapsShapeOf(final PolyLine other)
    {
        final Set<Segment> thisSegments = new HashSet<>();
        final List<Segment> segments = this.segments();
        segments.forEach(segment ->
        {
            thisSegments.add(segment);
            thisSegments.add(segment.reversed());
        });

        final List<Segment> otherSegments = other.segments();
        for (final Segment otherSegment : otherSegments)
        {
            if (!thisSegments.contains(otherSegment))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Prepends the given {@link PolyLine} to this one, if possible.
     *
     * @param other
     *            The {@link PolyLine} to prepend
     * @return the new, combined {@link PolyLine}
     */
    public PolyLine prepend(final PolyLine other)
    {
        if (this.first().equals(other.last()))
        {
            return new PolyLine(new MultiIterable<>(other, this.truncate(1, 0)));
        }
        else
        {
            throw new CoreException(
                    "Cannot prepend {} to {} - the end and start points do not match.",
                    other.toWkt(), this.toWkt());
        }
    }

    @Override
    public boolean remove(final Object object)
    {
        throw new IllegalAccessError("A polyline is immutable");
    }

    @Override
    public boolean removeAll(final Collection<?> collection)
    {
        throw new IllegalAccessError("A polyline is immutable");
    }

    @Override
    public boolean retainAll(final Collection<?> collection)
    {
        throw new IllegalAccessError("A polyline is immutable");
    }

    public PolyLine reversed()
    {
        final List<Location> reversed = new ArrayList<>();
        for (int i = this.size() - 1; i >= 0; i--)
        {
            reversed.add(this.get(i));
        }
        return new PolyLine(reversed);
    }

    public void saveAsGeoJson(final WritableResource resource)
    {
        final List<Iterable<Location>> geometries = new ArrayList<>();
        geometries.add(this);
        saveAsGeoJson(geometries, resource);
    }

    /**
     * @return All the {@link Segment}s that represent this {@link PolyLine}. If the
     *         {@link PolyLine} is empty, then the {@link Segment} list is empty. If the
     *         {@link PolyLine} has only one item, the {@link Segment} list contains only one
     *         {@link Segment} made of twice the same {@link Location}. If the {@link PolyLine} has
     *         more than one {@link Location}, then the result is a list of {@link Segment}s. Note:
     *         This method should be used carefully. Each call to it will cause a rebuild of the
     *         {@link List}, which can be very inefficient for long {@link PolyLine}s. To avoid
     *         this, the caller can call segments once and cache the results.
     */
    public List<Segment> segments()
    {
        final List<Segment> result = new ArrayList<>();
        if (size() == 1)
        {
            result.add(new Segment(get(0), get(0)));
        }
        else if (this instanceof Segment)
        {
            result.add((Segment) this);
        }
        else
        {
            Location previous = null;
            for (final Location location : this)
            {
                if (previous == null)
                {
                    previous = location;
                    continue;
                }
                result.add(new Segment(previous, location));
                previous = location;
            }
        }
        return result;
    }

    /**
     * Returns a Set of {@link Location} for all self-intersections, other than shape points for
     * this {@link PolyLine}. Separated from selfIntersects() to avoid degrading its performance.
     *
     * @return the set of locations
     */
    public Set<Location> selfIntersections()
    {
        Set<Location> intersections = null;

        final boolean isPolygon = this instanceof Polygon;

        // Exclude point-segments, so we know which segments are actually consecutive
        final List<Segment> segments = this.segments().stream()
                .filter(segment -> !segment.isPoint()).collect(Collectors.toList());

        // Consecutive segments should not be considered (they always have common point)
        for (int i = 0; i < segments.size() - 2; i++)
        {
            // For Polygons the last segment is consecutive to the first
            final int limit = isPolygon && i == 0 ? segments.size() - 1 : segments.size();

            // Only consider 'higher' segments.
            // No need to do a reverse check with the 'lower' segments again.
            for (int j = i + 2; j < limit; j++)
            {
                final Location intersection = segments.get(i).intersection(segments.get(j));

                if (intersection != null)
                {
                    // Self-intersection is a low probability event.
                    // Only allocate if needed
                    if (intersections == null)
                    {
                        intersections = new HashSet<>();
                    }

                    intersections.add(intersection);
                }
            }
        }

        return intersections == null ? Collections.emptySet() : intersections;
    }

    /**
     * @return True if the {@link PolyLine} self intersects at locations other than shape points.
     */
    public boolean selfIntersects()
    {
        // See comments on algorithm in selfIntersections()

        final boolean isPolygon = this instanceof Polygon;
        final List<Segment> segments = this.segments().stream()
                .filter(segment -> !segment.isPoint()).collect(Collectors.toList());

        for (int i = 0; i < segments.size() - 2; i++)
        {
            final int limit = isPolygon && i == 0 ? segments.size() - 1 : segments.size();
            for (int j = i + 2; j < limit; j++)
            {
                if (segments.get(i).intersection(segments.get(j)) != null)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Return the smaller one between the shortest distance from this {@link PolyLine}'s shape
     * points to the other shape, and the other shape's shape points to this polyline.
     *
     * @param other
     *            The other shape to compare to
     * @return The two way shortest distance to the other {@link PolyLine}
     */
    public Distance shortestDistanceTo(final PolyLine other)
    {
        final Distance one = shortestOneWayDistanceTo(other);
        final Distance two = other.shortestOneWayDistanceTo(this);
        return one.isLessThan(two) ? one : two;
    }

    /**
     * Return the shortest distance from this {@link PolyLine}'s shape points to the other shape,
     * using a one-way snapping.
     *
     * @param other
     *            The other shape to compare to
     * @return The shortest one way cost distance to the other {@link PolyLine}
     */
    public Distance shortestOneWayDistanceTo(final PolyLine other)
    {
        Distance shortest = Distance.MAXIMUM;
        for (final Location shapePoint : this)
        {
            final Distance current = shapePoint.snapTo(other).getDistance();
            shortest = current.isLessThan(shortest) ? current : shortest;
        }
        return shortest;
    }

    @Override
    public int size()
    {
        return this.points.size();
    }

    /**
     * Snap an origin {@link Location} to this {@link PolyLine} using a {@link Snapper}
     *
     * @param origin
     *            The origin {@link Location} to snap
     * @return The corresponding {@link SnappedLocation}
     */
    public SnappedLocation snapFrom(final Location origin)
    {
        return new Snapper().snap(origin, this);
    }

    @Override
    public Object[] toArray()
    {
        return this.points.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] array)
    {
        return this.points.toArray(array);
    }

    public String toCompactString()
    {
        final StringList stringList = new StringList();
        this.forEach(location ->
        {
            stringList.add(location.toCompactString());
        });
        return stringList.join(SEPARATOR);
    }

    public String toSimpleString()
    {
        final String string = toCompactString();
        if (string.length() > SIMPLE_STRING_LENGTH + 1)
        {
            return string.substring(0, SIMPLE_STRING_LENGTH / 2) + "..."
                    + string.substring(string.length() - SIMPLE_STRING_LENGTH / 2);
        }
        return string;
    }

    @Override
    public String toString()
    {
        return toWkt();
    }

    /**
     * @return This {@link PolyLine} as Well Known Text
     */
    public String toWkt()
    {
        if (this.size() == 1)
        {
            // Handle a single location polyLine
            return new WktLocationConverter().convert(this.first());
        }
        return new WktPolyLineConverter().convert(this);
    }

    /**
     * Truncates this {@link PolyLine} at the given start and end index
     *
     * @param startIndex
     *            The index before which to truncate from the start
     * @param endIndex
     *            The index after which to truncate from the end
     * @return all the locations in this {@link PolyLine} after truncation.
     */
    public Iterable<Location> truncate(final int startIndex, final int endIndex)
    {
        if (startIndex < 0 || endIndex < 0 || startIndex >= this.size() || endIndex >= this.size()
                || startIndex + endIndex >= this.size())
        {
            throw new CoreException("Invalid start index {} or end index {} supplied.", startIndex,
                    endIndex);
        }

        return Iterables.stream(this).truncate(startIndex, endIndex);
    }

    /**
     * @return This {@link PolyLine} without duplicate consecutive shape points. Non-consecutive
     *         shape points will remain unchanged.
     */
    public PolyLine withoutDuplicateConsecutiveShapePoints()
    {
        final List<Location> shapePoints = new ArrayList<>();
        boolean hasDuplicates = false;

        final Iterator<Location> locationIterator = this.iterator();
        // PolyLines are only valid if at least one point exists, so it is safe to call next() once.
        Location previousLocation = locationIterator.next();
        shapePoints.add(previousLocation);

        while (locationIterator.hasNext())
        {
            final Location currentLocation = locationIterator.next();

            if (!currentLocation.equals(previousLocation))
            {
                shapePoints.add(currentLocation);
            }
            else
            {
                hasDuplicates = true;
            }
            previousLocation = currentLocation;
        }
        return hasDuplicates ? new PolyLine(shapePoints) : this;
    }

    protected final List<Location> getPoints()
    {
        return this.points;
    }
}
