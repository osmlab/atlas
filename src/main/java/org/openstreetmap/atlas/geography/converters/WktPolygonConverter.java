package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Given an WKT string generate a Polygon and vice-versa
 *
 * @author cstaylor
 * @author matthieun
 */
public class WktPolygonConverter implements TwoWayConverter<Polygon, String>
{
    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();

    @Override
    public Polygon backwardConvert(final String wkt)
    {
        final WKTReader myReader = new WKTReader();
        try
        {
            final Geometry result = myReader.read(wkt);
            if (result instanceof org.locationtech.jts.geom.Polygon)
            {
                return new JtsPolygonConverter()
                        .backwardConvert((org.locationtech.jts.geom.Polygon) result);
            }
            else if (result instanceof org.locationtech.jts.geom.MultiPolygon)
            {
                final MultiPolygon castResult = JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                        .convert((org.locationtech.jts.geom.MultiPolygon) result);
                if (castResult.outers().size() == 1 && castResult.inners().isEmpty())
                {
                    return castResult.outers().iterator().next();
                }
            }
            throw new CoreException(
                    "Cannot convert wkt which is not a Polygon or single-outer MultiPolygon: {}",
                    wkt);
        }
        catch (final ParseException e)
        {
            throw new CoreException("Cannot parse wkt: {}", wkt, e);
        }
    }

    @Override
    public String convert(final Polygon polygon)
    {
        final Geometry geometry = new JtsPolygonConverter().convert(polygon);
        return new WKTWriter().write(geometry);
    }
}
