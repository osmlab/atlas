package org.openstreetmap.atlas.geography;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Polygon #fullyGeometricallyEncloses(Location)} performance test.
 *
 * @author mkalender
 */
public class PolygonPerformanceTest
{
    private static final Logger logger = LoggerFactory.getLogger(PolygonPerformanceTest.class);
    private static final int size = 5_000;
    private static final Random random = new Random();

    private static void testCoverPerformanceHelper(final int size, final Rectangle polygonBounds,
            final Function<Polygon, Location> locationRetrievalFunction,
            final Function<Boolean, Boolean> extraAssertFunction) throws Exception
    {
        final int iteration = 1_000;
        final int polygonPointBound = 100;
        final int polygonMinPointCount = 3;
        final List<Polygon> randomPolygons = Stream
                .generate(() -> Polygon.random(
                        random.nextInt(polygonPointBound) + polygonMinPointCount, polygonBounds))
                .limit(size).collect(Collectors.toList());

        double overallTotalTimeWithBoundCheck = 0;
        double overallTotalTimeWithoutBoundCheck = 0;
        for (int i = 0; i < iteration; i++)
        {
            double totalTimeWithBoundCheck = 0;
            final double totalTimeWithoutBoundCheck = 0;
            for (int j = 0; j < size; j++)
            {
                final Polygon polygon = randomPolygons.get(j);

                // Use location retrieval method, but send a copy of Polygon
                final Location location = locationRetrievalFunction
                        .apply(new Polygon(polygon.getPoints()));

                // Do enclosure check
                final Time timer = Time.now();
                final boolean resultWithBoundCheck = polygon.fullyGeometricallyEncloses(location);
                final Duration duration = timer.elapsedSince();
                totalTimeWithBoundCheck += duration.asMilliseconds();

                // Do extra checks
                Assert.assertTrue(extraAssertFunction.apply(resultWithBoundCheck));
            }

            overallTotalTimeWithBoundCheck += totalTimeWithBoundCheck;
            overallTotalTimeWithoutBoundCheck += totalTimeWithoutBoundCheck;
        }

        logger.info(
                "Overall time with bound check: {} ms, without bound check: {} ms (iteration: {}) \n",
                overallTotalTimeWithBoundCheck, overallTotalTimeWithoutBoundCheck, iteration);
    }

    @Test
    public void testPerformance() throws Exception
    {
        // Rectangles to test
        final Rectangle waState = Rectangle.forCorners(Location.forString("45.53714, -123.70605"),
                Location.forString("48.93693, -117.13623"));
        final Rectangle waStateNorthBorder = Rectangle.forCorners(
                Location.forString("45.53714, -123.70605"),
                Location.forString("45.53814, -117.13623"));
        final Rectangle coState = Rectangle.forCorners(Location.forString("36.87962, -108.7207"),
                Location.forString("40.6473, -102.30469"));

        // Case 1
        logger.info("Testing locations completely outside of the polygon's bounding box");
        logger.info("Size: {}, polygon bounds: {}, location bounds: {}", size, waState, coState);
        testCoverPerformanceHelper(size, waState, (polygon) -> Location.random(coState),
                (result) -> !result);

        // Case 2
        logger.info("Testing locations on the bounding box border");
        logger.info("Size: {}, polygon bounds: {}, location bounds: {}", size, waState,
                waStateNorthBorder);
        testCoverPerformanceHelper(size, waState, (polygon) -> Location.random(waStateNorthBorder),
                (result) -> true);

        // Case 3
        logger.info("Testing locations within the bounding box but outside of the polygon");
        logger.info("Size: {}, polygon bounds: {}, location bounds: {}", size, waState, waState);
        testCoverPerformanceHelper(size, waState, (polygon) ->
        {
            // Find a location that is in the same bounding box, but outside polygon
            Location randomLocation;
            do
            {
                randomLocation = Location.random(waState);
            }
            while (polygon.fullyGeometricallyEncloses(randomLocation));

            return randomLocation;
        }, (result) -> !result);

        // Case 4
        logger.info("Testing locations point on the polygon");
        logger.info("Size: {}, polygon bounds: {}", size, waState);
        testCoverPerformanceHelper(size, waState, (polygon) ->
        {
            final List<Location> polygonPoints = polygon.getPoints();
            final int aIndex = random.nextInt(polygonPoints.size());
            return polygonPoints.get(aIndex);
        }, (result) -> true);

        // Case 5
        logger.info("Testing locations inside the polygon");
        logger.info("Size: {}, polygon bounds: {}", size, waState);
        testCoverPerformanceHelper(size, waState, (polygon) ->
        {
            // Find a location that is inside the polygon
            Location randomLocation;
            do
            {
                randomLocation = Location.random(waState);
            }
            while (!polygon.fullyGeometricallyEncloses(randomLocation));

            return randomLocation;
        }, (result) -> result);
    }
}
