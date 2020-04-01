package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

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
