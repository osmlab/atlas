package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Convert a {@link Location} to/from Well Known Binary (WKB).
 *
 * @author matthieun
 */
public class WkbLocationConverter implements TwoWayConverter<Location, byte[]>
{
    @Override
    public Location backwardConvert(final byte[] wkb)
    {
        Point geometry = null;
        final WKBReader myReader = new WKBReader();
        try
        {
            geometry = (Point) myReader.read(wkb);
        }
        catch (final ParseException | ClassCastException e)
        {
            throw new CoreException("Cannot parse wkb : {}", WKBWriter.toHex(wkb));
        }
        return new JtsPointConverter().backwardConvert(geometry);
    }

    @Override
    public byte[] convert(final Location location)
    {
        final Geometry geometry = new JtsPointConverter().convert(location);
        final byte[] wkb = new WKBWriter().write(geometry);
        return wkb;
    }
}
