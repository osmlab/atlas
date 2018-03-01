package org.openstreetmap.atlas.utilities.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.converters.MultiPolygonStringConverter;
import org.openstreetmap.atlas.geography.converters.PolygonStringConverter;
import org.openstreetmap.atlas.geography.converters.WkbMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.WkbPolygonConverter;
import org.openstreetmap.atlas.geography.converters.WktMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.WktPolygonConverter;
import org.openstreetmap.atlas.geography.index.QuadTree;
import org.openstreetmap.atlas.streaming.readers.GeoJsonReader;
import org.openstreetmap.atlas.streaming.readers.json.serializers.PropertiesLocated;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.WKBReader;

/**
 * Configurable {@link AtlasEntity} filter that passes through anything that intersects with
 * includePolygons polygons or anything that doesn't intersect with exclude polygons. Exclude
 * polygons are looked at only if no includePolygons polygons exist. Include takes precedence and
 * polygons intersecting with previously read polygons are dropped with a warning.
 * 
 * @author jklamer
 */
public final class AtlasEntityPolygonsFilter implements Predicate<AtlasEntity>, Serializable
{
    public static final String EXCLUDED_MULTIPOLYGONS_KEY = "filter.multipolygons.exclude";
    public static final String EXCLUDED_POLYGONS_KEY = "filter.polygons.exclude";
    public static final String INCLUDED_MULTIPOLYGONS_KEY = "filter.multipolygons.include";
    public static final String INCLUDED_POLYGONS_KEY = "filter.polygons.include";
    private static final Logger logger = LoggerFactory.getLogger(AtlasEntityPolygonsFilter.class);
    public static final IntersectionPolicy DEFAULT_INTERSECTION_POLICY = new IntersectionPolicy()
    {
        @Override
        public boolean multiPolygonEntityIntersecting(final MultiPolygon multiPolygon,
                final AtlasEntity entity)
        {
            if (entity instanceof LineItem)
            {
                return multiPolygon.overlaps(((LineItem) entity).asPolyLine());
            }
            if (entity instanceof LocationItem)
            {
                return multiPolygon
                        .fullyGeometricallyEncloses(((LocationItem) entity).getLocation());
            }
            if (entity instanceof Area)
            {
                return multiPolygon.overlaps(((Area) entity).asPolygon());
            }
            if (entity instanceof Relation)
            {
                return ((Relation) entity).members().stream().map(RelationMember::getEntity)
                        .anyMatch(relationEntity -> this
                                .multiPolygonEntityIntersecting(multiPolygon, relationEntity));
            }
            else
            {
                logger.warn("Unknown AtlasEntity Implementation {}",
                        entity.getClass().getSimpleName());
                return false;
            }
        }

        @Override
        public boolean polygonEntityIntersecting(final Polygon polygon, final AtlasEntity entity)
        {
            return entity.intersects(polygon);
        }
    };
    private static final long serialVersionUID = -3474748398986569205L;
    private List<MultiPolygon> excludeMultiPolygons = new ArrayList<>();
    private List<Polygon> excludePolygons = new ArrayList<>();
    private Type filterType;
    private List<MultiPolygon> includeMultiPolygons = new ArrayList<>();
    private List<Polygon> includePolygons = new ArrayList<>();
    private IntersectionPolicy intersectionPolicy;

    public static AtlasEntityPolygonsFilter forConfiguration(final Configuration configuration)
    {
        return forConfiguration(configuration, DEFAULT_INTERSECTION_POLICY);
    }

    public static AtlasEntityPolygonsFilter forConfiguration(final Configuration configuration,
            final IntersectionPolicy intersectionPolicy)
    {
        return forConfigurationValues(configuration.get(INCLUDED_POLYGONS_KEY).value(),
                configuration.get(INCLUDED_MULTIPOLYGONS_KEY).value(),
                configuration.get(EXCLUDED_POLYGONS_KEY).value(),
                configuration.get(EXCLUDED_MULTIPOLYGONS_KEY).value(), intersectionPolicy);
    }

    public static AtlasEntityPolygonsFilter forConfigurationValues(
            final Map<String, List<String>> includePolygonMap,
            final Map<String, List<String>> includeMultiPolygonMap,
            final Map<String, List<String>> excludePolygonMap,
            final Map<String, List<String>> excludeMultiPolygonMap,
            final IntersectionPolicy intersectionPolicy)
    {
        final List<Polygon> includePolygons;
        final List<Polygon> excludePolygons;
        final List<MultiPolygon> includeMultiPolygons;
        final List<MultiPolygon> excludeMultiPolygons;

        includePolygons = includePolygonMap != null ? getPolygonsFromFormatMap(includePolygonMap)
                : Collections.EMPTY_LIST;
        includeMultiPolygons = includeMultiPolygonMap != null
                ? getMultiPolygonsFromFormatMap(includeMultiPolygonMap) : Collections.EMPTY_LIST;
        excludePolygons = excludePolygonMap != null ? getPolygonsFromFormatMap(excludePolygonMap)
                : Collections.EMPTY_LIST;
        excludeMultiPolygons = excludeMultiPolygonMap != null
                ? getMultiPolygonsFromFormatMap(excludeMultiPolygonMap) : Collections.EMPTY_LIST;

        if (!includePolygons.isEmpty() || !includeMultiPolygons.isEmpty())
        {
            return Type.INCLUDE.polygonsAndMultiPolygons(intersectionPolicy, includePolygons,
                    includeMultiPolygons);
        }
        else
        {
            return Type.EXCLUDE.polygonsAndMultiPolygons(intersectionPolicy, excludePolygons,
                    excludeMultiPolygons);
        }
    }

    public static AtlasEntityPolygonsFilter forConfigurationValues(
            final Map<String, List<String>> includePolygonMap,
            final Map<String, List<String>> includeMultiPolygonMap,
            final Map<String, List<String>> excludePolygonMap,
            final Map<String, List<String>> excludeMultiPolygonMap)
    {
        return forConfigurationValues(includePolygonMap, includeMultiPolygonMap, excludePolygonMap,
                excludeMultiPolygonMap, DEFAULT_INTERSECTION_POLICY);
    }

    private static StringConverter<Optional<List<MultiPolygon>>> getMultiPolygonConverterForFormat(
            final String format)
    {
        switch (PolygonStringFormat.getEnum(format))
        {
            case ATLAS:
                return string -> Optional.of(Collections
                        .singletonList(new MultiPolygonStringConverter().convert(string)));
            case GEOJSON:
                return string ->
                {
                    final List<MultiPolygon> multiPolygons = new ArrayList<>();
                    final GeoJsonReader reader = new GeoJsonReader(new StringResource(string));
                    while (reader.hasNext())
                    {
                        final PropertiesLocated propertiesLocated = reader.next();
                        if (propertiesLocated.getItem() instanceof MultiPolygon)
                        {
                            multiPolygons.add((MultiPolygon) propertiesLocated.getItem());
                        }
                        else
                        {
                            logger.warn("MultiPolygon Filter does not support item {}",
                                    propertiesLocated.toString());
                        }
                    }
                    return Optional.of(multiPolygons).filter(list -> !list.isEmpty());
                };
            case WKB:
                return string -> Optional
                        .of(Collections.singletonList(new WkbMultiPolygonConverter()
                                .backwardConvert(WKBReader.hexToBytes(string))));
            case WKT:
                return string -> Optional.of(Collections
                        .singletonList(new WktMultiPolygonConverter().backwardConvert(string)));
            case UNSUPPORTED:
            default:
                logger.warn("No converter set up for {} format. Supported formats are {}", format,
                        Arrays.copyOf(PolygonStringFormat.values(),
                                PolygonStringFormat.values().length - 1));
                return string -> Optional.empty();
        }
    }

    private static List<MultiPolygon> getMultiPolygonsFromFormatMap(
            final Map<String, List<String>> multiPolygonLists)
    {
        if (multiPolygonLists.isEmpty())
        {
            return Collections.emptyList();
        }
        return multiPolygonLists.entrySet().stream()
                .flatMap(formatAndMultiPolygonStrings -> formatAndMultiPolygonStrings.getValue()
                        .stream()
                        .map(getMultiPolygonConverterForFormat(
                                formatAndMultiPolygonStrings.getKey())::convert)
                        .filter(Optional::isPresent).map(Optional::get).flatMap(List::stream))
                .collect(Collectors.toList());
    }

    private static StringConverter<Optional<List<Polygon>>> getPolygonConverterForFormat(
            final String format)
    {
        switch (PolygonStringFormat.getEnum(format))
        {
            case ATLAS:
                return string -> Optional.of(
                        Collections.singletonList(new PolygonStringConverter().convert(string)));
            case GEOJSON:
                return string ->
                {
                    final List<Polygon> polygons = new ArrayList<>();
                    final GeoJsonReader reader = new GeoJsonReader(new StringResource(string));
                    while (reader.hasNext())
                    {
                        final PropertiesLocated propertiesLocated = reader.next();
                        if (propertiesLocated.getItem() instanceof Polygon)
                        {
                            polygons.add((Polygon) propertiesLocated.getItem());
                        }
                        else
                        {
                            logger.warn("Polygon Filter does not support item {}",
                                    propertiesLocated.toString());
                        }
                    }
                    return Optional.of(polygons).filter(list -> !list.isEmpty());
                };
            case WKT:
                return string -> Optional.of(Collections
                        .singletonList(new WktPolygonConverter().backwardConvert(string)));
            case WKB:
                return string -> Optional.of(Collections.singletonList(
                        new WkbPolygonConverter().backwardConvert(WKBReader.hexToBytes(string))));
            case UNSUPPORTED:
            default:
                logger.warn("No converter set up for {} format. Supported formats are {}", format,
                        Arrays.copyOf(PolygonStringFormat.values(),
                                PolygonStringFormat.values().length - 1));
                return string -> Optional.empty();
        }
    }

    private static List<Polygon> getPolygonsFromFormatMap(
            final Map<String, List<String>> polygonLists)
    {
        if (polygonLists.isEmpty())
        {
            return Collections.emptyList();
        }
        return polygonLists.entrySet().stream()
                .flatMap(formatAndPolygonStrings -> formatAndPolygonStrings.getValue().stream().map(
                        getPolygonConverterForFormat(formatAndPolygonStrings.getKey())::convert)
                        .filter(Optional::isPresent).map(Optional::get).flatMap(List::stream))
                .collect(Collectors.toList());
    }

    /**
     * Returns predicate that filters out MultiPolygons that overlap with any Polygons or outer
     * MultiPolygons in the provided QuadTree. This also adds any non-intersecting MultiPolygon to
     * the quad tree.
     *
     * @param vettedPolygons
     *            non-overlapping polygons and multipolygons
     * @param polygonType
     *            whether this is an include or exclude Polygon
     * @return filter for multipolygons
     */
    private static Predicate<MultiPolygon> notOverlappingMultipolygon(
            final QuadTree<Located> vettedPolygons, final Type polygonType)
    {
        return multiPolygon ->
        {
            if (vettedPolygons.get(multiPolygon.bounds()).stream()
                    .filter(located -> located instanceof Polygon).map(located -> (Polygon) located)
                    .noneMatch(multiPolygon::overlaps)
                    && vettedPolygons.get(multiPolygon.bounds()).stream()
                            .filter(located -> located instanceof MultiPolygon)
                            .map(located -> (MultiPolygon) located)
                            .noneMatch(multiPolygon::overlaps))
            {
                vettedPolygons.add(multiPolygon.bounds(), multiPolygon);
                return true;
            }
            else
            {
                logger.warn("Dropping {} MultiPolygon {}", polygonType, multiPolygon);
                return false;
            }
        };
    }

    /**
     * Returns predicate that filters out polygons that intersect with any polygons in the provided
     * QuadTree. This also adds any non-intersecting polygon to the quad tree.
     *
     * @param vettedPolygons
     *            non-overlapping polygons and multipolygons
     * @param polygonType
     *            whether this is an include or exclude Polygon
     * @return filter for polygons
     */
    private static Predicate<Polygon> notOverlappingPolygon(final QuadTree<Located> vettedPolygons,
            final Type polygonType)
    {
        return polygon ->
        {
            if (vettedPolygons.get(polygon.bounds()).stream()
                    .filter(located -> located instanceof Polygon).map(located -> (Polygon) located)
                    .noneMatch(polygon::overlaps)
                    && vettedPolygons.get(polygon.bounds()).stream()
                            .filter(located -> located instanceof MultiPolygon)
                            .map(located -> (MultiPolygon) located).noneMatch(polygon::overlaps))
            {
                vettedPolygons.add(polygon.bounds(), polygon);
                return true;
            }
            else
            {
                logger.warn("Dropping {} Polygon {}", polygonType, polygon);
                return false;
            }
        };
    }

    private AtlasEntityPolygonsFilter(final Type filterType, final Collection<Polygon> polygons,
            final Collection<MultiPolygon> multiPolygons)
    {
        this(filterType, DEFAULT_INTERSECTION_POLICY, polygons, multiPolygons);
    }

    private AtlasEntityPolygonsFilter(final Type filterType,
            final IntersectionPolicy intersectionPolicy, final Collection<Polygon> polygons,
            final Collection<MultiPolygon> multiPolygons)
    {
        this.filterType = filterType;
        this.intersectionPolicy = intersectionPolicy;
        final QuadTree<Located> vettedPolygons = new QuadTree<>();
        if (filterType == Type.INCLUDE)
        {
            this.includePolygons.addAll(polygons == null ? Collections.EMPTY_SET
                    : polygons.stream().filter(notOverlappingPolygon(vettedPolygons, Type.INCLUDE))
                            .collect(Collectors.toSet()));
            this.includeMultiPolygons.addAll(multiPolygons == null ? Collections.EMPTY_SET
                    : multiPolygons.stream()
                            .filter(notOverlappingMultipolygon(vettedPolygons, Type.INCLUDE))
                            .collect(Collectors.toSet()));
        }
        else
        {
            this.excludePolygons.addAll(polygons == null ? Collections.EMPTY_SET
                    : polygons.stream().filter(notOverlappingPolygon(vettedPolygons, Type.EXCLUDE))
                            .collect(Collectors.toSet()));
            this.excludeMultiPolygons.addAll(multiPolygons == null ? Collections.EMPTY_SET
                    : multiPolygons.stream()
                            .filter(notOverlappingMultipolygon(vettedPolygons, Type.EXCLUDE))
                            .collect(Collectors.toSet()));
        }
    }

    @Override
    public boolean test(final AtlasEntity object)
    {
        return noPolygons().or(isIncluded()).or(isNotExcluded()).test(object);
    }

    private boolean excludePolygonsEmpty()
    {
        return this.excludePolygons.isEmpty() && this.excludeMultiPolygons.isEmpty();
    }

    private boolean includePolygonsEmpty()
    {
        return this.includePolygons.isEmpty() && this.includeMultiPolygons.isEmpty();
    }

    private Predicate<AtlasEntity> isIncluded()
    {
        return entity -> this.filterType == Type.INCLUDE && (this.includePolygons.stream().anyMatch(
                polygon -> this.intersectionPolicy.polygonEntityIntersecting(polygon, entity))
                || this.includeMultiPolygons.stream()
                        .anyMatch(multiPolygon -> this.intersectionPolicy
                                .multiPolygonEntityIntersecting(multiPolygon, entity)));
    }

    private Predicate<AtlasEntity> isNotExcluded()
    {
        return entity -> this.filterType == Type.EXCLUDE
                && this.excludePolygons.stream()
                        .noneMatch(polygon -> this.intersectionPolicy
                                .polygonEntityIntersecting(polygon, entity))
                && this.excludeMultiPolygons.stream()
                        .noneMatch(multiPolygon -> this.intersectionPolicy
                                .multiPolygonEntityIntersecting(multiPolygon, entity));
    }

    private Predicate<AtlasEntity> noPolygons()
    {
        return entity -> this.includePolygonsEmpty() && this.excludePolygonsEmpty();
    }

    /**
     * Interface to be implemented if custom intersection logic is required. The
     * {@link AtlasEntityPolygonsFilter#DEFAULT_INTERSECTION_POLICY} is that an entity is said to
     * intersect when any part of the entity overlaps with any area of the Polygon or MultiPolygon.
     */
    public interface IntersectionPolicy extends Serializable
    {
        boolean multiPolygonEntityIntersecting(MultiPolygon multiPolygon, AtlasEntity entity);

        boolean polygonEntityIntersecting(Polygon polygon, AtlasEntity entity);
    }

    /**
     * The filter Type, either {@link Type#INCLUDE} or {@link Type#EXCLUDE}. Used for filter
     * construction.
     */
    public enum Type
    {
        INCLUDE,
        EXCLUDE;

        public AtlasEntityPolygonsFilter multiPolygons(final Collection<MultiPolygon> multiPolygons)
        {
            return new AtlasEntityPolygonsFilter(this, null, multiPolygons);
        }

        public AtlasEntityPolygonsFilter multiPolygons(final IntersectionPolicy intersectionPolicy,
                final Collection<MultiPolygon> multiPolygons)
        {
            return new AtlasEntityPolygonsFilter(this, intersectionPolicy, null, multiPolygons);
        }

        public AtlasEntityPolygonsFilter polygons(final Collection<Polygon> polygons)
        {
            return new AtlasEntityPolygonsFilter(this, polygons, null);
        }

        public AtlasEntityPolygonsFilter polygons(final IntersectionPolicy intersectionPolicy,
                final Collection<Polygon> polygons)
        {
            return new AtlasEntityPolygonsFilter(this, intersectionPolicy, polygons, null);
        }

        public AtlasEntityPolygonsFilter polygonsAndMultiPolygons(
                final Collection<Polygon> polygons, final Collection<MultiPolygon> multiPolygons)
        {
            return new AtlasEntityPolygonsFilter(this, polygons, multiPolygons);
        }

        public AtlasEntityPolygonsFilter polygonsAndMultiPolygons(
                final IntersectionPolicy intersectionPolicy, final Collection<Polygon> polygons,
                final Collection<MultiPolygon> multiPolygons)
        {
            return new AtlasEntityPolygonsFilter(this, intersectionPolicy, polygons, multiPolygons);
        }
    }

    /**
     * The enum for supported Polygon and Multipolygon string formats
     */
    private enum PolygonStringFormat
    {
        ATLAS("atlas"),
        GEOJSON("geojson"),
        WKT("wkt"),
        WKB("wkb"),
        UNSUPPORTED("UNSUPPORTED");

        private String format;

        public static PolygonStringFormat getEnum(final String format)
        {
            for (final PolygonStringFormat polygonStringFormat : values())
            {
                if (polygonStringFormat.getFormat().equalsIgnoreCase(format))
                {
                    return polygonStringFormat;
                }
            }
            return PolygonStringFormat.UNSUPPORTED;
        }

        PolygonStringFormat(final String format)
        {
            this.format = format;
        }

        public String getFormat()
        {
            return this.format;
        }

        @Override
        public String toString()
        {
            return getFormat();
        }
    }
}
