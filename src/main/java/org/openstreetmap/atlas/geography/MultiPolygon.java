package org.openstreetmap.atlas.geography;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;
import org.openstreetmap.atlas.geography.clipping.Clip;
import org.openstreetmap.atlas.geography.clipping.Clip.ClipType;
import org.openstreetmap.atlas.geography.clipping.GeometryOperation;
import org.openstreetmap.atlas.geography.converters.MultiPolygonStringConverter;
import org.openstreetmap.atlas.geography.converters.WkbMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.WktMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsLocationConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.geography.geojson.GeoJsonGeometry;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
import org.openstreetmap.atlas.geography.index.RTree;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Surface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Multiple {@link Polygon}s some inner, some outer.
 *
 * @author matthieun
 */
public class MultiPolygon
        implements Iterable<Polygon>, GeometricSurface, Serializable, GeoJsonGeometry
{
    public static final MultiPolygon MAXIMUM = forPolygon(Rectangle.MAXIMUM);
    public static final MultiPolygon TEST_MULTI_POLYGON;
    private static final Logger logger = LoggerFactory.getLogger(MultiPolygon.class);
    private static final long serialVersionUID = 4198234682870043547L;
    private static final int SIMPLE_STRING_LENGTH = 200;

    static
    {
        final MultiMap<Polygon, Polygon> outerToInners = new MultiMap<>();
        final Polygon outer = new Polygon(Location.CROSSING_85_280, Location.CROSSING_85_17,
                Location.TEST_1, Location.TEST_5);
        final Polygon inner = new Polygon(Location.TEST_6, Location.TEST_2, Location.TEST_7);
        outerToInners.add(outer, inner);
        TEST_MULTI_POLYGON = new MultiPolygon(outerToInners);
    }

    private final MultiMap<Polygon, Polygon> outerToInners;
    private Rectangle bounds;

    /**
     * @param polygons
     *            The outers of the multipolygon
     * @return A {@link MultiPolygon} with the provided {@link Polygon} as a single outer
     *         {@link Polygon}, with no inner {@link Polygon}
     */
    public static MultiPolygon forOuters(final Iterable<Polygon> polygons)
    {
        final MultiMap<Polygon, Polygon> multiMap = new MultiMap<>();
        polygons.forEach(polygon -> multiMap.put(polygon, Collections.emptyList()));
        return new MultiPolygon(multiMap);
    }

    public static MultiPolygon forOuters(final Polygon... polygons)
    {
        return MultiPolygon.forOuters(Arrays.asList(polygons));
    }

    /**
     * @param polygon
     *            A simple {@link Polygon}
     * @return A {@link MultiPolygon} with the provided {@link Polygon} as a single outer
     *         {@link Polygon}, with no inner {@link Polygon}
     */
    public static MultiPolygon forPolygon(final Polygon polygon)
    {
        final MultiMap<Polygon, Polygon> multiMap = new MultiMap<>();
        multiMap.put(polygon, new ArrayList<>());
        return new MultiPolygon(multiMap);
    }

    /**
     * Generate a {@link MultiPolygon} from Well Known Text
     *
     * @param wkt
     *            The {@link MultiPolygon} in well known text
     * @return The parsed {@link MultiPolygon}
     */
    public static MultiPolygon wkt(final String wkt)
    {
        return new WktMultiPolygonConverter().backwardConvert(wkt);
    }

    public MultiPolygon(final MultiMap<Polygon, Polygon> outerToInners)
    {
        this.outerToInners = outerToInners;
    }

    public GeoJsonObject asGeoJsonFeatureCollection()
    {
        final GeoJsonBuilder builder = new GeoJsonBuilder();
        return builder.createFeatureCollection(Iterables.translate(outers(),
                outerPolygon -> builder.createOneOuterMultiPolygon(
                        new MultiIterable<>(Collections.singleton(outerPolygon),
                                this.outerToInners.get(outerPolygon)))));
    }

    /**
     * Creates a JsonObject with GeoJSON geometry representing this multi-polygon.
     *
     * @return A JsonObject with GeoJSON geometry
     */
    @Override
    public JsonObject asGeoJsonGeometry()
    {
        return GeoJsonUtils.geometry(GeoJsonType.MULTI_POLYGON,
                GeoJsonUtils.multiPolygonToCoordinates(this));
    }

    public Iterable<LocationIterableProperties> asLocationIterableProperties()
    {
        final Iterable<LocationIterableProperties> outers = Iterables.translate(outers(), polygon ->
        {
            final Map<String, String> tags = new HashMap<>();
            tags.put("MultiPolygon", "outer");
            return new LocationIterableProperties(polygon, tags);
        });
        final Iterable<LocationIterableProperties> inners = Iterables.translate(inners(), polygon ->
        {
            final Map<String, String> tags = new HashMap<>();
            tags.put("MultiPolygon", "inner");
            return new LocationIterableProperties(polygon, tags);
        });
        return new MultiIterable<>(outers, inners);
    }

    /**
     * @return Optional of {@link Polygon} representation if possible
     */
    public Optional<Polygon> asSimplePolygon()
    {
        if (this.isSimplePolygon())
        {
            return outers().stream().findFirst();
        }
        logger.warn("Trying to read complex MultiPolygon as simple Polygon");
        return Optional.empty();
    }

    @Override
    public Rectangle bounds()
    {
        if (this.bounds == null && !this.isEmpty())
        {
            final Set<Location> locations = new HashSet<>();
            forEach(polygon -> polygon.forEach(locations::add));
            this.bounds = Rectangle.forLocations(locations);
        }
        return this.bounds;
    }

    /**
     * @param clipping
     *            The {@link MultiPolygon} clipping that {@link MultiPolygon}
     * @param clipType
     *            The type of clip (union, or, and or xor)
     * @return The {@link Clip} container, that can return the clipped {@link MultiPolygon}
     */
    public Clip clip(final MultiPolygon clipping, final ClipType clipType)
    {
        return new Clip(clipType, this, clipping);
    }

    /**
     * Concatenate multiple {@link MultiPolygon}s into one. If the two {@link MultiPolygon}s happen
     * to have the same outer polygon, then the other's inner polygons will be added and the
     * current's inner polygons will be erased.
     *
     * @param other
     *            The other {@link MultiPolygon} to concatenate.
     * @return The concatenated {@link MultiPolygon}
     */
    public MultiPolygon concatenate(final MultiPolygon other)
    {
        final MultiMap<Polygon, Polygon> result = new MultiMap<>();
        result.putAll(getOuterToInners());
        result.putAll(other.getOuterToInners());
        return new MultiPolygon(result);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof MultiPolygon)
        {
            final MultiPolygon that = (MultiPolygon) other;
            final Set<Polygon> thatOuters = that.outers();
            if (thatOuters.size() != this.outers().size())
            {
                return false;
            }
            for (final Polygon outer : this.outers())
            {
                if (!thatOuters.contains(outer))
                {
                    return false;
                }
                final List<Polygon> thatInners = that.innersOf(outer);
                if (thatInners.size() != this.innersOf(outer).size())
                {
                    return false;
                }
                for (final Polygon inner : this.innersOf(outer))
                {
                    if (!thatInners.contains(inner))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @param location
     *            A {@link Location} item
     * @return True if the {@link MultiPolygon} contains the provided item (i.e. it is within the
     *         outer polygons and not within the inner polygons)
     */
    @Override
    public boolean fullyGeometricallyEncloses(final Location location)
    {
        for (final Polygon outer : outers())
        {
            if (outer.fullyGeometricallyEncloses(location))
            {
                boolean result = true;
                // Checking the inners only when a location is within the outer saves the expensive
                // general inners() call.
                for (final Polygon inner : innersOf(outer))
                {
                    if (inner.fullyGeometricallyEncloses(location))
                    {
                        // If the inner is overlapping the location, we skip that outer and continue
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
     * Tests to see if entire surface of the provided {@link MultiPolygon} lies within this
     * {@link MultiPolygon}
     *
     * @param that
     *            the provided {@link MultiPolygon} to test
     * @return true if the conditions are met, false otherwise
     */
    @Override
    public boolean fullyGeometricallyEncloses(final MultiPolygon that)
    {
        final RTree<Polygon> thisOuters = RTree.forLocated(this.outers());
        // each outer of that must fit within one of this' outer/inner groups
        for (final Polygon thatOuter : that.outers())
        {
            boolean enclosedWithoutInnerOverlap = false;
            for (final Polygon thisOuter : thisOuters.get(thatOuter.bounds(), thatOuter::overlaps))
            {
                if (thisOuter.fullyGeometricallyEncloses(thatOuter))
                {
                    enclosedWithoutInnerOverlap = this.getOuterToInners().get(thisOuter).stream()
                            .noneMatch(that::overlaps);
                }
            }
            if (!enclosedWithoutInnerOverlap)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @param polyLine
     *            A {@link PolyLine} item
     * @return True if the {@link MultiPolygon} contains the provided {@link PolyLine}.
     */
    @Override
    public boolean fullyGeometricallyEncloses(final PolyLine polyLine)
    {
        for (final Polygon outer : outers())
        {
            if (outer.fullyGeometricallyEncloses(polyLine))
            {
                return this.getOuterToInners().get(outer).stream()
                        .noneMatch(inner -> inner.overlaps(polyLine));
            }
        }
        return false;
    }

    @Override
    public GeoJsonType getGeoJsonType()
    {
        return GeoJsonType.MULTI_POLYGON;
    }

    public MultiMap<Polygon, Polygon> getOuterToInners()
    {
        return this.outerToInners;
    }

    @Override
    public int hashCode()
    {
        int result = 0;
        for (final Polygon polygon : this)
        {
            result += polygon.hashCode();
        }
        return result;
    }

    public List<Polygon> inners()
    {
        return this.outerToInners.allValues();
    }

    public List<Polygon> innersOf(final Polygon outer)
    {
        if (this.outerToInners.containsKey(outer))
        {
            return this.outerToInners.get(outer);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean intersects(final PolyLine polyLine)
    {
        for (final Polygon outer : this.outers())
        {
            if (outer.intersects(polyLine))
            {
                return true;
            }
        }
        for (final Polygon inner : this.inners())
        {
            if (inner.intersects(polyLine))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return {@code true} if this {@link MultiPolygon} doesn't have any outer members
     */
    public boolean isEmpty()
    {
        return this.outerToInners.isEmpty();
    }

    /**
     * @return True if this {@link MultiPolygon} is valid according to the OGC SFS specification.
     *         See {@code org.locationtech.jts.geom.Geometry.isValid()}
     */
    public boolean isOGCValid()
    {
        return new JtsMultiPolygonToMultiPolygonConverter().backwardConvert(this).isValid();
    }

    /**
     * @return True if this {@link MultiPolygon} is valid according to the OSM specification. OSM
     *         allows some inners of the MultiPolygon to touch on more than a single point, to allow
     *         for one inner to be split in multiple parts tagged differently. Example: a forest
     *         with an inner, that is one side a meadow, and on the other side some marshland.
     */
    public boolean isOSMValid()
    {
        final org.locationtech.jts.geom.MultiPolygon jtsMultiPolygon = new JtsMultiPolygonToMultiPolygonConverter()
                .backwardConvert(this);
        final TopologyValidationError topologyValidationError = new IsValidOp(jtsMultiPolygon)
                .getValidationError();
        if (topologyValidationError != null)
        {
            // In this case, the geometry is not OGC valid, here we capture the
            // TopologyValidationError to know what to do next.
            if (TopologyValidationError.SELF_INTERSECTION == topologyValidationError.getErrorType())
            {
                final Location errorLocation = new JtsLocationConverter()
                        .backwardConvert(topologyValidationError.getCoordinate());
                final Rectangle errorExpandedBoundingBox = errorLocation
                        .boxAround(Distance.ONE_METER);
                final List<Polygon> ringsOfInterest = Iterables
                        .stream(new MultiIterable<>(outers(), inners()))
                        .filter(ringOfInterest -> ringOfInterest
                                .intersects(errorExpandedBoundingBox))
                        .collectToList();
                final List<GeometricObject> intersections = new ArrayList<>();
                for (int i = 0; i < ringsOfInterest.size(); i++)
                {
                    for (int j = i + 1; j < ringsOfInterest.size(); j++)
                    {
                        // Make sure this is just a PolyLine
                        final List<Polygon> candidates = new ArrayList<>();
                        candidates.add(ringsOfInterest.get(i));
                        candidates.add(ringsOfInterest.get(j));
                        GeometryOperation.intersection(candidates).ifPresent(intersections::add);
                    }
                }
                boolean allIntersectionsArePolyLines = true;
                for (final GeometricObject intersection : intersections)
                {
                    if (!isLinear(intersection))
                    {
                        allIntersectionsArePolyLines = false;
                        break;
                    }
                }
                return ringsOfInterest.size() > 1 && allIntersectionsArePolyLines;
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * @return whether this Multipolygon can be represented as a {@link Polygon}
     */
    public boolean isSimplePolygon()
    {
        return this.outers().size() == 1;
    }

    @Override
    public Iterator<Polygon> iterator()
    {
        return new MultiIterable<>(outers(), inners()).iterator();
    }

    /**
     * Merge multiple {@link MultiPolygon}s into one. If the two {@link MultiPolygon}s happen to
     * have the same outer polygon, then the two's inner polygons will be added to the same list.
     *
     * @param other
     *            The other {@link MultiPolygon} to merge.
     * @return The concatenated {@link MultiPolygon}
     */
    public MultiPolygon merge(final MultiPolygon other)
    {
        final MultiMap<Polygon, Polygon> result = new MultiMap<>();
        result.putAll(getOuterToInners());
        result.addAll(other.getOuterToInners());
        return new MultiPolygon(result);
    }

    public Set<Polygon> outers()
    {
        return this.outerToInners.keySet();
    }

    @Override
    public boolean overlaps(final MultiPolygon otherMultiPolygon)
    {
        return this.outers().stream().anyMatch(otherMultiPolygon::overlaps)
                && otherMultiPolygon.outers().stream().anyMatch(this::overlaps);
    }

    @Override
    public boolean overlaps(final PolyLine polyLine)
    {
        return overlapsInternal(polyLine, true);
    }

    public void saveAsGeoJson(final WritableResource resource)
    {
        final JsonWriter writer = new JsonWriter(resource);
        writer.write(asGeoJson());
        writer.close();
    }

    @Override
    public Surface surface()
    {
        Surface result = Surface.MINIMUM;
        for (final Polygon outer : this.outers())
        {
            result = result.add(outer.surface());
        }
        for (final Polygon inner : this.inners())
        {
            result = result.subtract(inner.surface());
        }
        return result;
    }

    @Override
    public Surface surfaceOnSphere()
    {
        Surface result = Surface.MINIMUM;
        for (final Polygon outer : this.outers())
        {
            result = result.add(outer.surfaceOnSphere());
        }
        for (final Polygon inner : this.inners())
        {
            result = result.subtract(inner.surfaceOnSphere());
        }
        return result;
    }

    public String toCompactString()
    {
        return new MultiPolygonStringConverter().backwardConvert(this);
    }

    public String toReadableString()
    {
        final String separator1 = "\n\t";
        final String separator2 = "\n\t\t";
        final StringBuilder builder = new StringBuilder();
        final StringList outers = new StringList();
        for (final Polygon outer : this.outers())
        {
            final StringList inners = new StringList();
            for (final Polygon inner : innersOf(outer))
            {
                inners.add("Inner: " + inner.toCompactString());
            }
            outers.add("Outer: " + outer.toCompactString() + separator2 + inners.join(separator2));
        }
        builder.append(outers.join(separator1));
        return builder.toString();
    }

    public String toSimpleString()
    {
        final String string = toCompactString();
        if (string.length() > SIMPLE_STRING_LENGTH + 1)
        {
            return string.substring(0, SIMPLE_STRING_LENGTH) + "...";
        }
        return string;
    }

    @Override
    public String toString()
    {
        return toWkt();
    }

    @Override
    public byte[] toWkb()
    {
        return new WkbMultiPolygonConverter().convert(this);
    }

    @Override
    public String toWkt()
    {
        return new WktMultiPolygonConverter().convert(this);
    }

    private boolean isLinear(final GeometricObject geometricObject)
    {
        return (geometricObject instanceof PolyLine && !(geometricObject instanceof Polygon))
                || geometricObject instanceof MultiPolyLine;
    }

    private boolean overlapsInternal(final PolyLine polyLine, final boolean runReverseCheck)
    {
        for (final Location location : polyLine)
        {
            if (fullyGeometricallyEncloses(location))
            {
                return true;
            }
        }
        if (runReverseCheck && polyLine instanceof Polygon)
        {
            final Polygon polygon = (Polygon) polyLine;
            for (final Polygon outer : this.outers())
            {
                if (polygon.overlaps(outer))
                {
                    boolean result = true;
                    for (final Polygon inner : this.innersOf(outer))
                    {
                        if (inner.fullyGeometricallyEncloses(polygon))
                        {
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
        }
        return this.intersects(polyLine);
    }
}
