package org.openstreetmap.atlas.geography.converters;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

/**
 * Conversion from a {@link PolyLine} into a Well Known Binary (WKB) byte array. Uses JTS for
 * Coordinate creation and WKB creation and writing.
 *
 * @author robert_stack
 */
public class WkbPolyLineConverter implements TwoWayConverter<PolyLine, byte[]>
{
    @Override
    public PolyLine backwardConvert(final byte[] wkb)
    {
        PolyLine polyLine = null;
        Geometry geometry = null;
        final WKBReader myReader = new WKBReader();
        try
        {
            geometry = myReader.read(wkb);
        }
        catch (final ParseException e)
        {
            throw new CoreException("Cannot parse wkb : {}", WKBWriter.toHex(wkb));
        }

        final Coordinate[] coordinates = geometry.getCoordinates();
        final List<Location> locations = new ArrayList<>();
        for (int i = 0; i < coordinates.length; i++)
        {
            // y = latitude, x = longitude from JTS Coordinate format
            locations.add(new Location(Latitude.degrees(coordinates[i].y),
                    Longitude.degrees(coordinates[i].x)));
        }
        polyLine = new PolyLine(locations);

        return polyLine;
    }

    @Override
    public byte[] convert(final PolyLine polyLine)
    {
        final Geometry geometry;
        final List<Coordinate> cooordinates = new ArrayList<>();
        for (final Location location : polyLine)
        {
            // swap latitude/longitude for JTS Coordinate format
            cooordinates.add(new Coordinate(location.getLongitude().asDegrees(),
                    location.getLatitude().asDegrees()));
        }
        final Coordinate[] coordinateArray = cooordinates
                .toArray(new Coordinate[cooordinates.size()]);
        if (coordinateArray.length == 1)
        {
            geometry = new GeometryFactory().createPoint(coordinateArray[0]);
        }
        else
        {
            geometry = new GeometryFactory().createLineString(coordinateArray);
        }

        final byte[] wkb = new WKBWriter().write(geometry);

        return wkb;
    }
}
