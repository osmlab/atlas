package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
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
        org.locationtech.jts.geom.MultiPolygon geometry = null;
        try
        {
            geometry = (org.locationtech.jts.geom.MultiPolygon) WKB_READER.read(wkb);
        }
        catch (final ParseException | ClassCastException e)
        {
            throw new CoreException("Cannot parse wkb : {}", WKBWriter.toHex(wkb));
        }
        return new JtsMultiPolygonToMultiPolygonConverter().convert(geometry);
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
