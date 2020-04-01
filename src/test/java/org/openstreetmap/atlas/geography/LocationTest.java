package org.openstreetmap.atlas.geography;

import java.util.function.BiFunction;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Ratio;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class LocationTest extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(LocationTest.class);
    private static final double DELTA = 1e-15;

    private static final Switch<BiFunction<Location, Location, Distance>> DISTANCE_TYPE = new Switch<>(
            "dist", "equirectangular or haversine or mixed", value ->
            {
                switch (value)
                {
                    case "equirectangular":
                        return (one, two) -> one.equirectangularDistanceTo(two);
                    case "haversine":
                        return (one, two) -> one.haversineDistanceTo(two);
                    case "mixed":
                        return (one, two) -> one.distanceTo(two);
                    default:
                        throw new CoreException("{} is not a recognized distance", value);
                }
            }, Optionality.OPTIONAL, "equirectangular");

    public static void main(final String[] args)
    {
        new LocationTest().run(args);
    }

    @Test
    public void testAntiMeridianMidPoint()
    {
        final Location location1 = Location.forWkt("POINT(-180 25)");
        final Location location2 = Location.forWkt("POINT(-180 -25)");
        Assert.assertEquals(Location.forWkt("POINT(-180 0)"), location1.midPoint(location2));
        Assert.assertEquals(Location.forWkt("POINT(-180 0)"), location2.midPoint(location1));

        final Location location3 = Location.forWkt("POINT(180 25)");
        final Location location4 = Location.forWkt("POINT(180 -25)");
        Assert.assertEquals(Location.forWkt("POINT(180 0)"), location3.midPoint(location4));
        Assert.assertEquals(Location.forWkt("POINT(180 0)"), location4.midPoint(location3));
    }

    @Test
    public void testCrossingAntimeridian()
    {
        final Location one = new Location(Latitude.degrees(37), Longitude.degrees(179.998));
        final Location two = new Location(Latitude.degrees(37), Longitude.degrees(179.999));
        // This one is actually -180.0
        final Location thr = new Location(Latitude.degrees(37), Longitude.degrees(180.0));
        final Location fur = new Location(Latitude.degrees(37), Longitude.degrees(-179.999));
        logger.info("one equirectangular two: {}", one.equirectangularDistanceTo(two));
        logger.info("two equirectangular thr: {}", two.equirectangularDistanceTo(thr));
        logger.info("thr equirectangular fur: {}", thr.equirectangularDistanceTo(fur));
        logger.info("one haversine two: {}", one.haversineDistanceTo(two));
        logger.info("two haversine thr: {}", two.haversineDistanceTo(thr));
        logger.info("thr haversine fur: {}", thr.haversineDistanceTo(fur));
        logger.info("one mixed two: {}", one.distanceTo(two));
        logger.info("two mixed thr: {}", two.distanceTo(thr));
        logger.info("thr mixed fur: {}", thr.distanceTo(fur));
        logger.info("one heading two: {}", one.headingTo(two));
        logger.info("two heading thr: {}", two.headingTo(thr));
        logger.info("thr heading fur: {}", thr.headingTo(fur));

        Assert.assertEquals(one.distanceTo(two).asMeters(), two.distanceTo(thr).asMeters(), 3);
        Assert.assertEquals(two.distanceTo(thr).asMeters(), thr.distanceTo(fur).asMeters(), 3);

        Assert.assertEquals(one.headingTo(two).asDegrees(), two.headingTo(thr).asDegrees(), 3);
        Assert.assertEquals(two.headingTo(thr).asDegrees(), thr.headingTo(fur).asDegrees(), 3);
    }

    @Test
    public void testDistanceTo()
    {
        final Location location1 = new Location(Latitude.degrees(37.336900),
                Longitude.degrees(-122.005414));
        logger.info("Location1: " + location1);
        final Location location2 = new Location(Latitude.degrees(37.332758),
                Longitude.degrees(-122.005409));
        logger.info("Location2: " + location1);

        final Distance approximation1 = location1.equirectangularDistanceTo(location2);
        final Distance approximation2 = location1.haversineDistanceTo(location2);

        Assert.assertTrue(Distance.FIFTEEN_HUNDRED_FEET.difference(approximation1)
                .isLessThan(Distance.meters(10)));
        Assert.assertTrue(Distance.FIFTEEN_HUNDRED_FEET.difference(approximation2)
                .isLessThan(Distance.meters(10)));

        // So the two methods have basically no difference, should choose equirectangular
        // approximation for most cases
        Assert.assertTrue(approximation1.difference(approximation2).isLessThan(Distance.meters(1)));
    }

    @Test
    public void testHeadingTo()
    {
        final Location location1 = new Location(Latitude.degrees(37.336900),
                Longitude.degrees(-122.005414));
        final Location location2 = new Location(Latitude.degrees(37.332758),
                Longitude.degrees(-122.005409));

        Assert.assertTrue(Heading.degrees(180).difference(location1.headingTo(location2))
                .isLessThan(Angle.degrees(1)));
    }

    @Test
    public void testLoxodromicMidPoint()
    {
        final Location location1 = new Location(Latitude.degrees(51.127), Longitude.degrees(1.338));
        final Location location2 = new Location(Latitude.degrees(50.964), Longitude.degrees(1.853));
        final Location midpoint = location1.loxodromicMidPoint(location2);

        Assert.assertEquals(51.0455, midpoint.getLatitude().asDegrees(), DELTA);
        Assert.assertEquals(1.5957265, midpoint.getLongitude().asDegrees(), DELTA);

        final Location location3 = new Location(Latitude.degrees(49), Longitude.degrees(-95.153));
        final Location location4 = new Location(Latitude.degrees(49), Longitude.degrees(-123.323));
        final Location midpoint2 = location3.loxodromicMidPoint(location4);

        Assert.assertEquals(49.0, midpoint2.getLatitude().asDegrees(), DELTA);
        Assert.assertEquals(-109.238, midpoint2.getLongitude().asDegrees(), DELTA);

        final Location location5 = new Location(Latitude.degrees(40.0), Longitude.degrees(-180.0));
        final Location location6 = new Location(Latitude.degrees(50.0), Longitude.degrees(-180.0));
        final Location midpoint3 = location5.loxodromicMidPoint(location6);

        Assert.assertEquals(45.0, midpoint3.getLatitude().asDegrees(), DELTA);
        Assert.assertEquals(-180.0, midpoint3.getLongitude().asDegrees(), DELTA);

        final Location location7 = new Location(Latitude.degrees(40.0), Longitude.degrees(180.0));
        final Location location8 = new Location(Latitude.degrees(50.0), Longitude.degrees(180.0));
        final Location midpoint4 = location7.loxodromicMidPoint(location8);

        Assert.assertEquals(45.0, midpoint4.getLatitude().asDegrees(), DELTA);
        Assert.assertEquals(180.0, midpoint4.getLongitude().asDegrees(), DELTA);

        final Location location9 = new Location(Latitude.degrees(-16.5), Longitude.degrees(-180));
        final Location location10 = new Location(Latitude.degrees(-17.0), Longitude.degrees(-180));
        final Location midpoint5 = location9.loxodromicMidPoint(location10);

        Assert.assertEquals(-16.75, midpoint5.getLatitude().asDegrees(), DELTA);
        Assert.assertEquals(-180.0, midpoint5.getLongitude().asDegrees(), DELTA);
    }

    @Test
    public void testMidPoint()
    {
        final Location location1 = new Location(Latitude.degrees(52.205), Longitude.degrees(0.119));
        final Location location2 = new Location(Latitude.degrees(48.857), Longitude.degrees(2.351));
        final Location midpoint = location1.midPoint(location2);

        Assert.assertEquals(50.5363269, midpoint.getLatitude().asDegrees(), DELTA);
        Assert.assertEquals(1.2746141, midpoint.getLongitude().asDegrees(), DELTA);
    }

    @Test
    public void testMidPointAccuracyAndSpeed()
    {
        final Location location1 = new Location(Latitude.degrees(51.127), Longitude.degrees(1.338));
        final Location location2 = new Location(Latitude.degrees(50.964), Longitude.degrees(1.853));

        final Time beginning = Time.now();
        final Location derivedMidPoint = location1.shiftAlongGreatCircle(
                location1.headingTo(location2),
                location1.distanceTo(location2).scaleBy(Ratio.HALF));
        System.out.println("Derived Duration: " + beginning.elapsedSince());
        System.out.println(derivedMidPoint.toString() + "\n");

        final Time beginning2 = Time.now();
        final Location calculatedMidPoint = location1.midPoint(location2);
        System.out.println("Calculated Duration: " + beginning2.elapsedSince());
        System.out.println(calculatedMidPoint.toString() + "\n");

        final Time beginning3 = Time.now();
        final Location calculatedLoxodromicMidPoint = location1.loxodromicMidPoint(location2);
        System.out.println("Calculated Loxodromic Duration: " + beginning3.elapsedSince());
        System.out.println(calculatedLoxodromicMidPoint.toString() + "\n");
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        long counter = 0L;
        @SuppressWarnings("unchecked")
        final BiFunction<Location, Location, Distance> distance = (BiFunction<Location, Location, Distance>) command
                .get(DISTANCE_TYPE);
        while (true)
        {
            final Location location1 = Location.random(Rectangle.MAXIMUM);
            final Location location2 = Location.random(Rectangle.MAXIMUM);
            distance.apply(location1, location2);
            counter++;
            if (counter % 10_000_000 == 0)
            {
                logger.info("{}", counter);
            }
        }
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(DISTANCE_TYPE);
    }
}
