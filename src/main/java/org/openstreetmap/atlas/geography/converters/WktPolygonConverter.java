package org.openstreetmap.atlas.geography.converters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Given an WKT string generate a Polygon and vice-versa
 *
 * @author cstaylor
 */
public class WktPolygonConverter implements TwoWayConverter<Polygon, String>
{
    @Override
    public Polygon backwardConvert(final String wkt)
    {
        com.vividsolutions.jts.geom.Polygon geometry = null;
        final WKTReader myReader = new WKTReader();
        try
        {
            geometry = (com.vividsolutions.jts.geom.Polygon) myReader.read(wkt);
        }
        catch (final ParseException | ClassCastException e)
        {
            throw new CoreException("Cannot parse wkt : {}", wkt);
        }
        return new JtsPolygonConverter().backwardConvert(geometry);
    }

    @Override
    public String convert(final Polygon polygon)
    {
        final Geometry geometry = new JtsPolygonConverter().convert(polygon);
        return new WKTWriter().write(geometry);
    }
}
