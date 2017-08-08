package org.openstreetmap.atlas.utilities.timezone;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author tony
 */
public class TimeZoneTest
{
    private static Location BERMUDA_INSIDE = new Location(Latitude.degrees(32.3009619),
            Longitude.degrees(-64.770893));
    private static Location BERMUDA_OUTSIDE_CLOSE = BERMUDA_INSIDE
            .shiftAlongGreatCircle(Heading.EAST, Distance.SEA_TERRITORY_ZONE);
    private static Location BERMUDA_OUTSIDE_FARAWAY = BERMUDA_INSIDE
            .shiftAlongGreatCircle(Heading.EAST, Distance.SEA_TERRITORY_ZONE.scaleBy(3));

    /**
     * This is a case where there is a large timezone -- America/Denver -- and then a smaller
     * timezone --America/Phoenix -- contained within that large timezone -- and then a yet smaller
     * timezone (in this case the same as the large timezone -- America/Denver -- ) contained within
     * that second timezone
     */
    private static Location ARIZONA_MULTI_POLYGON_LEVEL_3 = new Location(
            Latitude.degrees(35.7468571), Longitude.degrees(-110.14448));
    private static Location ARIZONA_MULTI_POLYGON_LEVEL_2 = ARIZONA_MULTI_POLYGON_LEVEL_3
            .shiftAlongGreatCircle(Heading.WEST, Distance.miles(10));
    private static Location ARIZONA_MULTI_POLYGON_LEVEL_1 = ARIZONA_MULTI_POLYGON_LEVEL_3
            .shiftAlongGreatCircle(Heading.EAST, Distance.miles(10));

    private static Location LOCATION_IN_NEVADA = new Location(Latitude.degrees(41.8834138),
            Longitude.degrees(-114.19474));
    private static Location LOCATION_IN_IDAHO = LOCATION_IN_NEVADA
            .shiftAlongGreatCircle(Heading.NORTH, Distance.miles(10));
    private static Location LOCATION_IN_UTAH = LOCATION_IN_NEVADA
            .shiftAlongGreatCircle(Heading.EAST, Distance.miles(10));

    @Test
    public void testArizonaMultiPolygon()
    {
        final TimeZoneMap map = new TimeZoneMap(
                ARIZONA_MULTI_POLYGON_LEVEL_3.bounds().expand(Distance.miles(50)));
        Assert.assertEquals("America/Denver", map.timeZone(ARIZONA_MULTI_POLYGON_LEVEL_3).getID());
        Assert.assertEquals("America/Phoenix", map.timeZone(ARIZONA_MULTI_POLYGON_LEVEL_2).getID());
        Assert.assertEquals("America/Denver", map.timeZone(ARIZONA_MULTI_POLYGON_LEVEL_1).getID());
    }

    @Test
    public void testBoundary()
    {
        final TimeZoneMap map = new TimeZoneMap(
                LOCATION_IN_NEVADA.bounds().expand(Distance.miles(50)));
        Assert.assertEquals("America/Los_Angeles", map.timeZone(LOCATION_IN_NEVADA).getID());
        Assert.assertEquals("America/Boise", map.timeZone(LOCATION_IN_IDAHO).getID());
        Assert.assertEquals("America/Denver", map.timeZone(LOCATION_IN_UTAH).getID());
    }

    @Test
    public void testIsland()
    {
        final TimeZoneMap map = new TimeZoneMap(BERMUDA_INSIDE.bounds().expand(Distance.miles(50)));
        Assert.assertEquals("Atlantic/Bermuda", map.timeZone(BERMUDA_INSIDE).getID());
        Assert.assertEquals("Atlantic/Bermuda", map.timeZone(BERMUDA_OUTSIDE_CLOSE).getID());
        // in the middle of the sea
        Assert.assertEquals("GMT-04:00", map.timeZone(BERMUDA_OUTSIDE_FARAWAY).getID());
    }
}
