package org.openstreetmap.atlas.geography.converters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Convert a {@link Location} to/from Well Known Text (WKT).
 *
 * @author matthieun
 */
public class WktLocationConverter implements TwoWayConverter<Location, String>
{
    @Override
    public Location backwardConvert(final String wkt)
    {
        Point geometry = null;
        final WKTReader myReader = new WKTReader();
        try
        {
            geometry = (Point) myReader.read(wkt);
        }
        catch (final ParseException | ClassCastException e)
        {
            throw new CoreException("Cannot parse wkt : {}", wkt);
        }
        return new JtsPointConverter().backwardConvert(geometry);
    }

    @Override
    public String convert(final Location location)
    {
        final Geometry geometry = new JtsPointConverter().convert(location);
        final String wkt = new WKTWriter().write(geometry);
        return wkt;
    }
}
