package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Given an WKT string generate a {@link MultiPolygon} and vice-versa
 *
 * @author matthieun
 */
public class WktMultiPolygonConverter extends WkMultiPolygonConverter<String>
{
    private static final TwoWayConverter<String, Geometry> CONVERTER = new TwoWayConverter<String, Geometry>()
    {
        @Override
        public String backwardConvert(final Geometry geometry)
        {
            return new WKTWriter().write(geometry);
        }

        @Override
        public Geometry convert(final String wkt)
        {
            try
            {
                return new WKTReader().read(wkt);
            }
            catch (final ParseException e)
            {
                throw new CoreException("Unable to parse WKT", e);
            }
        }
    };

    @Override
    TwoWayConverter<String, Geometry> getGeometryConverter()
    {
        return CONVERTER;
    }
}
