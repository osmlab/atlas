package org.openstreetmap.atlas.proto.converters;

import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.proto.ProtoLocation;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Converts back and forth between ProtoLocation and Location
 *
 * @author lcram
 */
public class ProtoLocationConverter implements TwoWayConverter<ProtoLocation, Location>
{
    @Override
    public ProtoLocation backwardConvert(final Location location)
    {
        final ProtoLocation.Builder protoLocationBuilder = ProtoLocation.newBuilder();
        protoLocationBuilder.setLatitude(Math.toIntExact(location.getLatitude().asDm7()));
        protoLocationBuilder.setLongitude(Math.toIntExact(location.getLongitude().asDm7()));
        return protoLocationBuilder.build();
    }

    @Override
    public Location convert(final ProtoLocation protoLocation)
    {
        final Longitude longitude = Longitude.dm7(protoLocation.getLongitude());
        final Latitude latitude = Latitude.dm7(protoLocation.getLatitude());
        return new Location(latitude, longitude);
    }
}
