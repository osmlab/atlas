package org.openstreetmap.atlas.utilities.filters;

import java.io.Serializable;
import java.util.ArrayList;
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
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.converters.PolygonStringFormat;
import org.openstreetmap.atlas.geography.index.QuadTree;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final long serialVersionUID = -3474748398986569205L;
    private Type filterType;
    private IntersectionPolicy intersectionPolicy;
    private List<MultiPolygon> multiPolygons = new ArrayList<>();
    private List<Polygon> polygons = new ArrayList<>();

    public static AtlasEntityPolygonsFilter forConfiguration(final Configuration configuration)
    {
        return forConfiguration(configuration, IntersectionPolicy.DEFAULT_INTERSECTION_POLICY);
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
            if (!excludePolygons.isEmpty() || !excludeMultiPolygons.isEmpty())
            {
                logger.warn(
                        "Ignoring exclude polygons and multipolygons passed through configuration");
            }
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
                excludeMultiPolygonMap, IntersectionPolicy.DEFAULT_INTERSECTION_POLICY);
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
                        .map(PolygonStringFormat
                                .getEnumForFormat(formatAndMultiPolygonStrings.getKey())
                                .getMultiPolygonConverter())
                        .filter(Optional::isPresent).map(Optional::get).flatMap(List::stream))
                .collect(Collectors.toList());
    }

    private static List<Polygon> getPolygonsFromFormatMap(
            final Map<String, List<String>> polygonLists)
    {
        if (polygonLists.isEmpty())
        {
            return Collections.emptyList();
        }
        return polygonLists.entrySet().stream()
                .flatMap(formatAndPolygonStrings -> formatAndPolygonStrings.getValue().stream()
                        .map(PolygonStringFormat.getEnumForFormat(formatAndPolygonStrings.getKey())
                                .getPolygonConverter())
                        .filter(Optional::isPresent).map(Optional::get).flatMap(List::stream))
                .collect(Collectors.toList());
    }

    private static Predicate<Located> locatedOverlappingPredicate(final Polygon polygon,
            final MultiPolygon multiPolygon)
    {
        Predicate<Located> returnPredicate = located -> false;
        if (polygon != null)
        {
            returnPredicate = returnPredicate.or(located ->
            {
                if (located instanceof Polygon)
                {
                    return polygon.overlaps((Polygon) located);
                }
                else
                {
                    return polygon.overlaps((MultiPolygon) located);
                }
            });
        }
        if (multiPolygon != null)
        {
            returnPredicate = returnPredicate.or(located ->
            {
                if (located instanceof Polygon)
                {
                    return multiPolygon.overlaps((Polygon) located);
                }
                else
                {
                    return multiPolygon.overlaps((MultiPolygon) located);
                }
            });
        }
        return returnPredicate;
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
                    .noneMatch(locatedOverlappingPredicate(null, multiPolygon)))
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
                    .noneMatch(locatedOverlappingPredicate(polygon, null)))
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
        this(filterType, IntersectionPolicy.DEFAULT_INTERSECTION_POLICY, polygons, multiPolygons);
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
            this.polygons.addAll(polygons == null ? Collections.EMPTY_SET
                    : polygons.stream().filter(notOverlappingPolygon(vettedPolygons, Type.INCLUDE))
                            .collect(Collectors.toSet()));
            this.multiPolygons.addAll(multiPolygons == null ? Collections.EMPTY_SET
                    : multiPolygons.stream()
                            .filter(notOverlappingMultipolygon(vettedPolygons, Type.INCLUDE))
                            .collect(Collectors.toSet()));
        }
        else
        {
            this.polygons.addAll(polygons == null ? Collections.EMPTY_SET
                    : polygons.stream().filter(notOverlappingPolygon(vettedPolygons, Type.EXCLUDE))
                            .collect(Collectors.toSet()));
            this.multiPolygons.addAll(multiPolygons == null ? Collections.EMPTY_SET
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

    private Predicate<AtlasEntity> isIncluded()
    {
        return entity -> this.filterType == Type.INCLUDE && (this.polygons.stream().anyMatch(
                polygon -> this.intersectionPolicy.polygonEntityIntersecting(polygon, entity))
                || this.multiPolygons.stream().anyMatch(multiPolygon -> this.intersectionPolicy
                        .multiPolygonEntityIntersecting(multiPolygon, entity)));
    }

    private Predicate<AtlasEntity> isNotExcluded()
    {
        return entity -> this.filterType == Type.EXCLUDE
                && this.polygons.stream()
                        .noneMatch(polygon -> this.intersectionPolicy
                                .polygonEntityIntersecting(polygon, entity))
                && this.multiPolygons.stream().noneMatch(multiPolygon -> this.intersectionPolicy
                        .multiPolygonEntityIntersecting(multiPolygon, entity));
    }

    private Predicate<AtlasEntity> noPolygons()
    {
        return entity -> this.polygons.isEmpty() && this.multiPolygons.isEmpty();
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
}
