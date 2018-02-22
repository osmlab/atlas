package org.openstreetmap.atlas.utilities.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.converters.PolygonStringConverter;
import org.openstreetmap.atlas.geography.converters.WkbPolygonConverter;
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
 * Configurable {@link AtlasEntity} filter that passes through anything that intersects with include
 * polygons or anything that doesn't intersect with exclude polygons. Exclude polygons are looked at
 * only if no include polygons exist. Include takes precedence and polygons intersecting with
 * previously read polygons are dropped with a warning.
 * 
 * @author jklamer
 */
public class AtlasEntityPolygonsFilter implements Predicate<AtlasEntity>, Serializable
{

    public static final String EXCLUDED_POLYGONS_KEY = "filter.polygons.exclude";
    public static final String INCLUDED_POLYGONS_KEY = "filter.polygons.include";
    private static final PolygonStringConverter POLYGON_STRING_CONVERTER = new PolygonStringConverter();
    private static final List<String> POLYGON_STRING_FORMATS = Arrays.asList("atlas", "geojson",
            "wkt", "wkb");
    private static final WkbPolygonConverter WKB_POLYGON_CONVERTER = new WkbPolygonConverter();
    private static final WktPolygonConverter WKT_POLYGON_CONVERTER = new WktPolygonConverter();
    private static final Logger logger = LoggerFactory.getLogger(AtlasEntityPolygonsFilter.class);
    private static final long serialVersionUID = -3474748398986569205L;
    private List<Polygon> exclude = new ArrayList<>();
    private List<Polygon> include = new ArrayList<>();

    private static StringConverter<Optional<List<Polygon>>> getConverterForFormat(
            final String format)
    {
        switch (format)
        {
            case "atlas":
                return string -> Optional
                        .of(Collections.singletonList(POLYGON_STRING_CONVERTER.convert(string)));
            case "geojson":
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
            case "wkt":
                return string -> Optional.of(
                        Collections.singletonList(WKT_POLYGON_CONVERTER.backwardConvert(string)));
            case "wkb":
                return string -> Optional.of(Collections.singletonList(
                        WKB_POLYGON_CONVERTER.backwardConvert(WKBReader.hexToBytes(string))));
            default:
                logger.warn("No converter set up for {} format. Supported formats are {}", format,
                        POLYGON_STRING_FORMATS);
                return string -> Optional.empty();
        }
    }

    private static Predicate<Polygon> notOverlapping(final QuadTree<Polygon> vettedPolygons,
            final String polygonType)
    {
        return polygon ->
        {
            if (vettedPolygons.get(polygon.bounds()).stream().noneMatch(polygon::overlaps))
            {
                vettedPolygons.add(polygon.bounds(), polygon);
                return true;
            }
            else
            {
                logger.warn("Dropping {} polygon {}", polygonType, polygon);
                return false;
            }
        };
    }

    public AtlasEntityPolygonsFilter(final Map<String, List<String>> includePolygonMap,
            final Map<String, List<String>> excludePolygonMap)
    {
        final QuadTree<Polygon> vettedPolygons = new QuadTree<>();
        if (includePolygonMap != null)
        {
            this.include = this.getPolygonsFromMap(includePolygonMap)
                    .filter(notOverlapping(vettedPolygons, "include")).collect(Collectors.toList());
        }
        if (excludePolygonMap != null)
        {
            this.exclude = this.getPolygonsFromMap(excludePolygonMap)
                    .filter(notOverlapping(vettedPolygons, "exclude")).collect(Collectors.toList());
        }
    }

    public AtlasEntityPolygonsFilter(final Configuration configuration)
    {
        this(configuration.get(INCLUDED_POLYGONS_KEY).value(),
                configuration.get(EXCLUDED_POLYGONS_KEY).value());
    }

    @Override
    public boolean test(final AtlasEntity object)
    {
        return noPolygons().or(isIncluded()).or(isNotExcluded()).test(object);
    }

    private Stream<Polygon> getPolygonsFromMap(final Map<String, List<String>> polygonLists)
    {
        if (polygonLists.isEmpty())
        {
            return Stream.empty();
        }
        return polygonLists.entrySet().stream()
                .flatMap(formatAndPolygonStrings -> formatAndPolygonStrings.getValue().stream()
                        .map(getConverterForFormat(formatAndPolygonStrings.getKey())::convert)
                        .filter(Optional::isPresent).map(Optional::get).flatMap(List::stream));
    }

    private Predicate<AtlasEntity> isIncluded()
    {
        return entity -> this.include.stream().anyMatch(entity::intersects);
    }

    private Predicate<AtlasEntity> isNotExcluded()
    {
        // if there are include polygons the entity needs to be in them which is not tested here
        return entity -> this.include.isEmpty()
                && this.exclude.stream().noneMatch(entity::intersects);
    }

    private Predicate<AtlasEntity> noPolygons()
    {
        return entity -> this.include.isEmpty() && this.exclude.isEmpty();
    }
}
