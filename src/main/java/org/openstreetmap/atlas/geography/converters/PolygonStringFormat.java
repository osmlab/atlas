package org.openstreetmap.atlas.geography.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.streaming.readers.GeoJsonReader;
import org.openstreetmap.atlas.streaming.readers.json.serializers.PropertiesLocated;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.WKBReader;

/**
 * The enum for supported Polygon and Multipolygon string formats. Contains functions to return
 * {@link Polygon} and {@link MultiPolygon} {@link StringConverter}s.
 *
 * @author jklamer
 */
public enum PolygonStringFormat
{
    ATLAS("atlas"),
    GEOJSON("geojson"),
    WKT("wkt"),
    WKB("wkb"),
    UNSUPPORTED("UNSUPPORTED");

    private static final Logger logger = LoggerFactory.getLogger(PolygonStringFormat.class);
    private String format;

    public static PolygonStringFormat getEnumForFormat(final String format)
    {
        for (final PolygonStringFormat polygonStringFormat : values())
        {
            if (polygonStringFormat.getFormat().equalsIgnoreCase(format))
            {
                return polygonStringFormat;
            }
        }
        return UNSUPPORTED;
    }

    PolygonStringFormat(final String format)
    {
        this.format = format;
    }

    public String getFormat()
    {
        return this.format;
    }

    public StringConverter<Optional<List<MultiPolygon>>> getMultiPolygonConverter()
    {
        switch (this)
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
                        Arrays.copyOf(values(), values().length - 1));
                return string -> Optional.empty();
        }
    }

    public StringConverter<Optional<List<Polygon>>> getPolygonConverter()
    {
        switch (this)
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
                        Arrays.copyOf(values(), values().length - 1));
                return string -> Optional.empty();
        }
    }

    @Override
    public String toString()
    {
        return getFormat();
    }
}
