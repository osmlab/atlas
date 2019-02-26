package org.openstreetmap.atlas.geography.converters.jts;

import org.locationtech.jts.geom.Coordinate;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Convert a {@link Location} to a {@link Coordinate} from the JTS library. The {@link Location}'s
 * {@link Latitude} dm7 value becomes the {@link Coordinate}'s y value, and the {@link Location}'s
 * {@link Longitude} dm7 value becomes the the {@link Coordinate}'s x value.
 *
 * @author matthieun
 */
public class JtsLocationConverter implements TwoWayConverter<Location, Coordinate>
{
    @Override
    public Location backwardConvert(final Coordinate coordinate)
    {
        return new Location(Latitude.degrees(coordinate.y), Longitude.degrees(coordinate.x));
    }

    @Override
    public Coordinate convert(final Location location)
    {
        return new Coordinate(location.getLongitude().asDegrees(),
                location.getLatitude().asDegrees());
    }
}
