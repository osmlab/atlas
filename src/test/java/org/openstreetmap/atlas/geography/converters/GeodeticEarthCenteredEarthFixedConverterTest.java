package org.openstreetmap.atlas.geography.converters;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Altitude;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.coordinates.EarthCenteredEarthFixedCoordinate;
import org.openstreetmap.atlas.geography.coordinates.GeodeticCoordinate;

/**
 * {@link GeodeticEarthCenteredEarthFixedConverter} test.
 *
 * @author mgostintsev
 */
public class GeodeticEarthCenteredEarthFixedConverterTest
{
    private static final GeodeticEarthCenteredEarthFixedConverter CONVERTER = new GeodeticEarthCenteredEarthFixedConverter();

    @Test
    public void testConversionToEarthCenteredEarthFixed()
    {
        final EarthCenteredEarthFixedCoordinate earthCenteredCoordinates = new EarthCenteredEarthFixedCoordinate(
                -576793.17, -5376363.47, 3372298.51);
        final GeodeticCoordinate geodeticCoordinates = CONVERTER
                .backwardConvert(earthCenteredCoordinates);

        Assert.assertEquals(32.12345, geodeticCoordinates.getLatitude().asDegrees(), 1e-3);
        Assert.assertEquals(-96.12345, geodeticCoordinates.getLongitude().asDegrees(), 1e-3);
        Assert.assertEquals(500.0, geodeticCoordinates.getAltitude().asMeters(), 1e-2);
    }

    @Test
    public void testConversionToGeodetic()
    {
        final GeodeticCoordinate geodeticCoordinates = new GeodeticCoordinate(
                Latitude.degrees(32.12345), Longitude.degrees(-96.12345), Altitude.meters(500.0));
        final EarthCenteredEarthFixedCoordinate earthCenteredCoordinates = CONVERTER
                .convert(geodeticCoordinates);

        Assert.assertEquals(-576793.17, earthCenteredCoordinates.getX(), 1e-2);
        Assert.assertEquals(-5376363.47, earthCenteredCoordinates.getY(), 1e-2);
        Assert.assertEquals(3372298.51, earthCenteredCoordinates.getZ(), 1e-2);
    }

    @Test
    public void testFullConversionCycle()
    {
        final double latitude = -39.664914;
        final double longitude = 176.881899;
        final double altitude = 300.0;

        final GeodeticCoordinate geodeticCoordinates = new GeodeticCoordinate(
                Latitude.degrees(latitude), Longitude.degrees(longitude),
                Altitude.meters(altitude));

        final EarthCenteredEarthFixedCoordinate earthCenteredCoordinates = CONVERTER
                .convert(geodeticCoordinates);

        assertEquals(-4909490.91860, earthCenteredCoordinates.getX(), 1e-2);
        assertEquals(267444.11617, earthCenteredCoordinates.getY(), 1e-2);
        assertEquals(-4049606.55365, earthCenteredCoordinates.getZ(), 1e-2);

        final GeodeticCoordinate backToGeodetic = CONVERTER
                .backwardConvert(earthCenteredCoordinates);
        assertEquals(latitude, backToGeodetic.getLatitude().asDegrees(), 1e-6);
        assertEquals(longitude, backToGeodetic.getLongitude().asDegrees(), 1e-6);
        assertEquals(altitude, backToGeodetic.getAltitude().asMeters(), 1e-6);
    }
}
