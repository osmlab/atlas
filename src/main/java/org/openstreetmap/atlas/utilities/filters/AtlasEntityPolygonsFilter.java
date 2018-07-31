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

import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.converters.PolygonStringFormat;
import org.openstreetmap.atlas.geography.index.RTree;
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
    private RTree<GeometricSurface> geometricSurfaces = new RTree<>();
    private IntersectionPolicy intersectionPolicy;

    public static Collection<GeometricSurface> createSurfaceCollection(
            final Collection<? extends GeometricSurface> collection1,
            final Collection<? extends GeometricSurface> collection2)
    {
        final ArrayList<GeometricSurface> returnCollection = new ArrayList<>();
        returnCollection.addAll(collection1);
        returnCollection.addAll(collection2);
        return returnCollection;
    }

    public static AtlasEntityPolygonsFilter forConfiguration(final Configuration configuration,
            final IntersectionPolicy intersectionPolicy)
    {
        return forConfigurationValues(configuration.get(INCLUDED_POLYGONS_KEY).value(),
                configuration.get(INCLUDED_MULTIPOLYGONS_KEY).value(),
                configuration.get(EXCLUDED_POLYGONS_KEY).value(),
                configuration.get(EXCLUDED_MULTIPOLYGONS_KEY).value(), intersectionPolicy);
    }

    public static AtlasEntityPolygonsFilter forConfiguration(final Configuration configuration)
    {
        return forConfiguration(configuration, IntersectionPolicy.DEFAULT_INTERSECTION_POLICY);
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

    private AtlasEntityPolygonsFilter(final Type filterType,
            final Collection<? extends GeometricSurface> geometricSurfaces)
    {
        this(filterType, IntersectionPolicy.DEFAULT_INTERSECTION_POLICY, geometricSurfaces);
    }

    @SuppressWarnings("unchecked")
    private AtlasEntityPolygonsFilter(final Type filterType,
            final IntersectionPolicy intersectionPolicy,
            final Collection<? extends GeometricSurface> geometricSurfaces)
    {
        this.filterType = filterType;
        this.intersectionPolicy = intersectionPolicy;
        this.geometricSurfaces = (RTree<GeometricSurface>) RTree
                .forLocated(geometricSurfaces == null ? Collections.EMPTY_SET : geometricSurfaces);
    }

    @Override
    public boolean test(final AtlasEntity object)
    {
        return noSurfaces().or(isIncluded()).or(isNotExcluded()).test(object);
    }

    private Predicate<AtlasEntity> isIncluded()
    {
        return entity -> this.filterType == Type.INCLUDE && (this.geometricSurfaces
                .get(entity.bounds()).stream().anyMatch(geometricSurface -> this.intersectionPolicy
                        .geometricSurfaceEntityIntersecting(geometricSurface, entity)));
    }

    private Predicate<AtlasEntity> isNotExcluded()
    {
        return entity -> this.filterType == Type.EXCLUDE && this.geometricSurfaces
                .get(entity.bounds()).stream().noneMatch(geometricSurface -> this.intersectionPolicy
                        .geometricSurfaceEntityIntersecting(geometricSurface, entity));
    }

    private Predicate<AtlasEntity> noSurfaces()
    {
        return entity -> this.geometricSurfaces.isEmpty();
    }

    /**
     * The filter Type, either {@link Type#INCLUDE} or {@link Type#EXCLUDE}. Used for filter
     * construction.
     */
    public enum Type
    {
        INCLUDE,
        EXCLUDE;

        public AtlasEntityPolygonsFilter geometricSurfaces(
                final IntersectionPolicy intersectionPolicy,
                final Collection<GeometricSurface> geometricSurfaces)
        {
            return new AtlasEntityPolygonsFilter(this, intersectionPolicy, geometricSurfaces);
        }

        public AtlasEntityPolygonsFilter geometricSurfaces(
                final Collection<GeometricSurface> geometricSurfaces)
        {
            return new AtlasEntityPolygonsFilter(this, geometricSurfaces);
        }

        public AtlasEntityPolygonsFilter multiPolygons(final Collection<MultiPolygon> multiPolygons)
        {
            return new AtlasEntityPolygonsFilter(this, multiPolygons);
        }

        public AtlasEntityPolygonsFilter multiPolygons(final IntersectionPolicy intersectionPolicy,
                final Collection<MultiPolygon> multiPolygons)
        {
            return new AtlasEntityPolygonsFilter(this, intersectionPolicy, multiPolygons);
        }

        public AtlasEntityPolygonsFilter polygons(final Collection<Polygon> polygons)
        {
            return new AtlasEntityPolygonsFilter(this, polygons);
        }

        public AtlasEntityPolygonsFilter polygons(final IntersectionPolicy intersectionPolicy,
                final Collection<Polygon> polygons)
        {
            return new AtlasEntityPolygonsFilter(this, intersectionPolicy, polygons);
        }

        public AtlasEntityPolygonsFilter polygonsAndMultiPolygons(
                final Collection<Polygon> polygons, final Collection<MultiPolygon> multiPolygons)
        {
            return new AtlasEntityPolygonsFilter(this,
                    createSurfaceCollection(polygons, multiPolygons));
        }

        public AtlasEntityPolygonsFilter polygonsAndMultiPolygons(
                final IntersectionPolicy intersectionPolicy, final Collection<Polygon> polygons,
                final Collection<MultiPolygon> multiPolygons)
        {
            return new AtlasEntityPolygonsFilter(this, intersectionPolicy,
                    createSurfaceCollection(polygons, multiPolygons));
        }
    }
}
