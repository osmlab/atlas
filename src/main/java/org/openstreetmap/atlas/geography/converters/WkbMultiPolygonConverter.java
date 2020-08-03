package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Converter class for conversion between Wkb byte array and {@link MultiPolygon}
 *
 * @author jklamer
 * @author matthieun
 */
public class WkbMultiPolygonConverter extends WkMultiPolygonConverter<byte[]>
{
    private static final TwoWayConverter<byte[], Geometry> CONVERTER = new TwoWayConverter<byte[], Geometry>()
    {
        @Override
        public byte[] backwardConvert(final Geometry geometry)
        {
            return new WKBWriter().write(geometry);
        }

        @Override
        public Geometry convert(final byte[] kyte)
        {
            try
            {
                return new WKBReader().read(kyte);
            }
            catch (final ParseException e)
            {
                throw new CoreException("Unable to parse WKB", e);
            }
        }
    };

    @Override
    TwoWayConverter<byte[], Geometry> getGeometryConverter()
    {
        return CONVERTER;
    }
}
