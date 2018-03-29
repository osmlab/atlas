package org.openstreetmap.atlas.geography.converters.proto;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.proto.ProtoLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoLocationConverterTest
{
    private static final Logger logger = LoggerFactory.getLogger(ProtoLocationConverterTest.class);

    @Test
    public void testLocationToProtoLocation()
    {
        final ProtoLocationConverter converter = new ProtoLocationConverter();
        final Location location = new Location(Latitude.dm7(1), Longitude.dm7(1));

        final ProtoLocation protoLocation = ProtoLocation.newBuilder().setLatitude(1)
                .setLongitude(1).build();
        final ProtoLocation convertedFromLocation = converter.backwardConvert(location);

        logger.info("{}", protoLocation);
        logger.info("{}", convertedFromLocation);
        Assert.assertEquals(protoLocation, convertedFromLocation);
    }

    @Test
    public void testProtoLocationToLocation()
    {
        final ProtoLocationConverter converter = new ProtoLocationConverter();
        final ProtoLocation protoLocation = ProtoLocation.newBuilder().setLatitude(1)
                .setLongitude(1).build();

        final Location location = new Location(Latitude.dm7(1), Longitude.dm7(1));
        final Location convertedFromProtoLocation = converter.convert(protoLocation);

        logger.info("{}", location);
        logger.info("{}", convertedFromProtoLocation);
        Assert.assertEquals(location, convertedFromProtoLocation);
    }
}
