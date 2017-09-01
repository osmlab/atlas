package org.openstreetmap.atlas.geography.coordinates;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Altitude;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Longitude;

/**
 * Tests {@link EarthCenteredEarthFixedCoordinate} and {@link GeodeticCoordinate} functionality.
 *
 * @author mgostintsev
 */
public class CoordinatesTest
{
    @Test
    public void testEarthCenteredEarthFixedCoordinateEquality()
    {
        final EarthCenteredEarthFixedCoordinate coordinate = new EarthCenteredEarthFixedCoordinate(
                -576793.17, -5376363.47, 3372298.51);

        final EarthCenteredEarthFixedCoordinate identicalCoordinate = new EarthCenteredEarthFixedCoordinate(
                -576793.17, -5376363.47, 3372298.51);

        final EarthCenteredEarthFixedCoordinate newCoordinate = new EarthCenteredEarthFixedCoordinate(
                -576123.17, -5376992.47, 3372123.51);

        Assert.assertEquals(coordinate, identicalCoordinate);
        Assert.assertNotEquals(coordinate, newCoordinate);
    }

    @Test
    public void testGeodeticCoordinateEquality()
    {
        final GeodeticCoordinate coordinate = new GeodeticCoordinate(Latitude.degrees(32.12345),
                Longitude.degrees(-96.12345), Altitude.meters(500.0));

        final GeodeticCoordinate identicalCoordinate = new GeodeticCoordinate(
                Latitude.degrees(32.12345), Longitude.degrees(-96.12345), Altitude.meters(500.0));

        final GeodeticCoordinate newCoordinate = new GeodeticCoordinate(Latitude.degrees(45.24252),
                Longitude.degrees(-99.14141), Altitude.meters(700.0));

        Assert.assertEquals(coordinate, identicalCoordinate);
        Assert.assertNotEquals(coordinate, newCoordinate);
    }
}
