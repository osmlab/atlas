package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Converter class for conversion between Wkb byte array and {@link MultiPolygon}
 *
 * @author jklamer
 */
public class WkbMultiPolygonConverter implements TwoWayConverter<MultiPolygon, byte[]>
{
    private static final WKBReader WKB_READER = new WKBReader();

    @Override
    public MultiPolygon backwardConvert(final byte[] wkb)
    {
        try
        {
            final Geometry result = WKB_READER.read(wkb);
            if (result instanceof org.locationtech.jts.geom.Polygon)
            {
                return MultiPolygon
                        .forPolygon(new JtsPolygonConverter().backwardConvert((Polygon) result));
            }
            else if (result instanceof org.locationtech.jts.geom.MultiPolygon)
            {
                return new JtsMultiPolygonToMultiPolygonConverter()
                        .convert((org.locationtech.jts.geom.MultiPolygon) result);
            }
            else
            {
                throw new CoreException("Unknown type: {}", result.getClass().getCanonicalName());
            }
        }
        catch (final ParseException | ClassCastException e)
        {
            throw new CoreException("Cannot parse wkb", e);
        }
    }

    @Override
    public byte[] convert(final MultiPolygon multiPolygon)
    {
        final Geometry geometry = new JtsMultiPolygonToMultiPolygonConverter()
                .backwardConvert(multiPolygon);
        final byte[] wkb = new WKBWriter().write(geometry);
        return wkb;
    }
}
