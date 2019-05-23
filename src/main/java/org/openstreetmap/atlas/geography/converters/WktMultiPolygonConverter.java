package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Given an WKT string generate a {@link MultiPolygon} and vice-versa
 *
 * @author matthieun
 */
public class WktMultiPolygonConverter implements TwoWayConverter<MultiPolygon, String>
{
    private static final JtsPolygonToMultiPolygonConverter POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsPolygonToMultiPolygonConverter();
    private static final JtsMultiPolygonToMultiPolygonConverter MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();

    @Override
    public MultiPolygon backwardConvert(final String wkt)
    {
        final WKTReader myReader = new WKTReader();
        try
        {
            final Geometry result = myReader.read(wkt);
            if (result instanceof org.locationtech.jts.geom.Polygon)
            {
                final org.locationtech.jts.geom.Polygon jtsPolygon = (org.locationtech.jts.geom.Polygon) result;
                return POLYGON_TO_MULTI_POLYGON_CONVERTER.convert(jtsPolygon);
            }
            else if (result instanceof org.locationtech.jts.geom.MultiPolygon)
            {
                return MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                        .convert((org.locationtech.jts.geom.MultiPolygon) result);
            }
            else
            {
                throw new CoreException("Unknown type: {}", result.getClass().getCanonicalName());
            }
        }
        catch (final ParseException | ClassCastException e)
        {
            throw new CoreException("Cannot parse wkt : {}", wkt, e);
        }
    }

    @Override
    public String convert(final MultiPolygon multiPolygon)
    {
        final org.locationtech.jts.geom.MultiPolygon geometry = MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                .backwardConvert(multiPolygon);
        return new WKTWriter().write(geometry);
    }
}
