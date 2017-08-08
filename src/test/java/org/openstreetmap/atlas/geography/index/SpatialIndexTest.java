package org.openstreetmap.atlas.geography.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.Test;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tony
 */
public class SpatialIndexTest
{
    private static Logger logger = LoggerFactory.getLogger(SpatialIndexTest.class);
    private static final int CASE_NUMBER_FOR_EACH_BOUND = 100_000;
    private static final int CASE_NUMBER_FOR_READ_TEST = CASE_NUMBER_FOR_EACH_BOUND / 100;

    // A bounding box inside (-10, -10) and (10, 10)
    private final Rectangle right = Rectangle.forLocations(
            new Location(Latitude.degrees(-10), Longitude.degrees(-10)),
            new Location(Latitude.degrees(10), Longitude.degrees(10)));

    // A bounding box inside (-30, -10) and (-10, 10)
    private final Rectangle left = Rectangle.forLocations(
            new Location(Latitude.degrees(-10), Longitude.degrees(-30)),
            new Location(Latitude.degrees(10), Longitude.degrees(-10)));

    private List<Location> locationsInsideRight = new ArrayList<>();
    private List<Location> locationsInsideLeft = new ArrayList<>();
    private List<Location> locationsOnRightBorder = new ArrayList<>();
    private List<Rectangle> rectanglesInsideRight = new ArrayList<>();
    private List<Rectangle> rectanglesInsideLeft = new ArrayList<>();
    private List<Rectangle> rectanglesOnBorder = new ArrayList<>();
    private final QuadTree<Location> quadTreeLocation = new QuadTree<>();
    private final QuadTree<Rectangle> quadTreeRectangle = new QuadTree<>();
    private final RTree<Location> rTreeLocation = new RTree<>();
    private final RTree<Rectangle> rTreeRectangle = new RTree<>();

    @SuppressWarnings("unchecked")
    @Test
    public void testAccuracy()
    {
        // generate test case
        this.locationsOnRightBorder = new ArrayList<>();
        this.locationsOnRightBorder.add(new Location(Latitude.degrees(-10), Longitude.degrees(0)));
        this.locationsOnRightBorder.add(new Location(Latitude.degrees(10), Longitude.degrees(5)));
        // this record is on left right border
        this.locationsOnRightBorder.add(new Location(Latitude.degrees(0), Longitude.degrees(-10)));
        this.locationsInsideRight.add(new Location(Latitude.degrees(-9), Longitude.degrees(0)));
        this.locationsInsideRight.add(new Location(Latitude.degrees(0), Longitude.degrees(0)));
        this.locationsInsideRight.add(new Location(Latitude.degrees(3), Longitude.degrees(-5)));
        this.locationsInsideLeft.add(new Location(Latitude.degrees(0), Longitude.degrees(-25)));
        this.locationsInsideLeft.add(new Location(Latitude.degrees(5), Longitude.degrees(-29)));
        // this record is very close to left right border
        this.locationsInsideLeft
                .add(new Location(Latitude.degrees(0), Longitude.degrees(-10.000001)));
        assertEquals(3, this.locationsOnRightBorder.size());
        assertEquals(3, this.locationsInsideRight.size());
        assertEquals(3, this.locationsInsideLeft.size());
        this.rectanglesInsideRight = locationToRectangle(this.locationsInsideRight);
        this.rectanglesInsideLeft = locationToRectangle(this.locationsInsideLeft);
        this.rectanglesOnBorder = locationToRectangle(this.locationsOnRightBorder);

        // build tree
        buildLocationTree(this.quadTreeLocation, this.locationsInsideLeft,
                this.locationsInsideRight, this.locationsOnRightBorder);
        buildLocationTree(this.rTreeLocation, this.locationsInsideLeft, this.locationsInsideRight,
                this.locationsOnRightBorder);
        buildRectangleTree(this.quadTreeRectangle, this.rectanglesInsideLeft,
                this.rectanglesInsideRight, this.rectanglesOnBorder);
        buildRectangleTree(this.rTreeRectangle, this.rectanglesInsideLeft,
                this.rectanglesInsideRight, this.rectanglesOnBorder);

        assertEquals(9, this.quadTreeLocation.size());
        assertEquals(9, this.quadTreeRectangle.size());
        assertEquals(9, this.rTreeLocation.size());
        assertEquals(9, this.rTreeRectangle.size());

        // rtree returns exact correct locations
        assertEquals(6, this.rTreeLocation.get(this.right).size());
        assertEquals(4, this.rTreeLocation.get(this.left).size());

        // quad tree returns the one close to border
        assertEquals(7, this.quadTreeLocation.get(this.right).size());
        assertEquals(4, this.quadTreeLocation.get(this.left).size());

        // rtree returns one more rectangle since rectangle is 1 km larger bounding box than
        // location
        assertEquals(7, this.rTreeRectangle.get(this.right).size());
        assertEquals(4, this.rTreeRectangle.get(this.left).size());

        // quad tree returns even more rectangles, looks like shouldn't use quad tree for rectangle
        assertEquals(8, this.quadTreeRectangle.get(this.right).size());
        assertEquals(7, this.quadTreeRectangle.get(this.left).size());
    }

    @Test
    public void testPerformance()
    {
        generateRandomTestCase();
        testBuild();
        testRead();
    }

    @SuppressWarnings("unchecked")
    private void buildLocationTree(final JtsSpatialIndex<Location> index,
            final List<Location>... lists)
    {
        for (final List<Location> list : lists)
        {
            list.forEach(location -> index.add(location.bounds(), location));
        }
    }

    @SuppressWarnings("unchecked")
    private void buildRectangleTree(final JtsSpatialIndex<Rectangle> index,
            final List<Rectangle>... lists)
    {
        for (final List<Rectangle> list : lists)
        {
            list.forEach(rectangle -> index.add(rectangle.bounds(), rectangle));
        }
    }

    private void generateRandomTestCase()
    {
        this.locationsInsideRight = randomLocations(this.right, CASE_NUMBER_FOR_EACH_BOUND);
        this.locationsInsideLeft = randomLocations(this.left, CASE_NUMBER_FOR_EACH_BOUND);
        this.rectanglesInsideRight = locationToRectangle(this.locationsInsideRight);
        this.rectanglesInsideLeft = locationToRectangle(this.locationsInsideLeft);
    }

    /**
     * @return Rectangle list which expand 1 km bounding box for each location
     */
    private List<Rectangle> locationToRectangle(final List<Location> loactions)
    {
        return loactions.stream().map(location -> location.boxAround(Distance.kilometers(1)))
                .collect(Collectors.toList());
    }

    private List<Location> randomLocations(final Rectangle bound, final int numberToGenerate)
    {
        final List<Location> list = new ArrayList<>(numberToGenerate);
        final List<Double> doubleLatitudes = new Random()
                .doubles(bound.lowerLeft().getLatitude().asDegrees(),
                        bound.upperRight().getLatitude().asDegrees())
                .limit(numberToGenerate).boxed().collect(Collectors.toList());
        final List<Double> doubleLongitudes = new Random()
                .doubles(bound.lowerLeft().getLongitude().asDegrees(),
                        bound.upperRight().getLongitude().asDegrees())
                .limit(numberToGenerate).boxed().collect(Collectors.toList());
        for (int i = 0; i < doubleLongitudes.size(); i++)
        {
            list.add(new Location(Latitude.degrees(doubleLatitudes.get(i)),
                    Longitude.degrees(doubleLongitudes.get(i))));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void testBuild()
    {
        logger.info("Building QuadTree and RTree indices");

        // Location QuadTree
        final CounterWithStatistic counter = new CounterWithStatistic(logger);
        buildLocationTree(this.quadTreeLocation, this.locationsInsideLeft,
                this.locationsInsideRight);
        logger.info("Location QuadTree size={}, depth={}, took {} to build",
                this.quadTreeLocation.size(), this.quadTreeLocation.depth(), counter.sinceStart());
        assertEquals(CASE_NUMBER_FOR_EACH_BOUND * 2, this.quadTreeLocation.size());

        this.quadTreeLocation.get(this.right).forEach(location -> counter.increment());
        logger.info("{} locations are within bounds", counter);
        assertTrue(counter.count() >= CASE_NUMBER_FOR_EACH_BOUND);

        // Rectangle QuadTree
        counter.clear();
        buildRectangleTree(this.quadTreeRectangle, this.rectanglesInsideRight,
                this.rectanglesInsideLeft);
        logger.info("Rectangle QuadTree size={}, depth={}, took {} to build",
                this.quadTreeRectangle.size(), this.quadTreeRectangle.depth(),
                counter.sinceStart());
        assertEquals(CASE_NUMBER_FOR_EACH_BOUND * 2, this.quadTreeRectangle.size());

        this.quadTreeRectangle.get(this.right).forEach(location -> counter.increment());
        logger.info("{} rectangles are within bounds", counter);
        assertTrue(counter.count() >= CASE_NUMBER_FOR_EACH_BOUND);

        // Location RTree
        counter.clear();
        buildLocationTree(this.rTreeLocation, this.locationsInsideLeft, this.locationsInsideRight);
        logger.info("Location RTree size={}, depth={}, took {} to build", this.rTreeLocation.size(),
                this.rTreeLocation.depth(), counter.sinceStart());
        assertEquals(CASE_NUMBER_FOR_EACH_BOUND * 2, this.rTreeLocation.size());

        this.rTreeLocation.get(this.right).forEach(location -> counter.increment());
        logger.info("{} locations are within bounds", counter);
        assertEquals(CASE_NUMBER_FOR_EACH_BOUND, counter.count());

        // Rectangle RTree
        counter.clear();
        buildRectangleTree(this.rTreeRectangle, this.rectanglesInsideRight,
                this.rectanglesInsideLeft);
        logger.info("Rectangle RTree size={}, depth={}, took {} to build",
                this.rTreeRectangle.size(), this.rTreeRectangle.depth(), counter.sinceStart());
        assertEquals(CASE_NUMBER_FOR_EACH_BOUND * 2, this.rTreeRectangle.size());

        this.rTreeRectangle.get(this.right).forEach(location -> counter.increment());
        logger.info("{} rectangles are within bounds", counter);
        assertTrue(counter.count() >= CASE_NUMBER_FOR_EACH_BOUND);
    }

    private void testRead()
    {
        logger.info("Reading QuadTree and RTree indices");

        final CounterWithStatistic counter = new CounterWithStatistic(logger);
        this.rectanglesInsideRight.stream().limit(CASE_NUMBER_FOR_READ_TEST)
                .forEach(rectangle -> assertTrue(
                        this.quadTreeLocation.get(rectangle.bounds()).iterator().hasNext()));
        logger.info("Location QuadTree read rectangles {} times took {}", CASE_NUMBER_FOR_READ_TEST,
                counter.sinceStart());

        counter.clear();
        this.locationsInsideRight.stream().limit(CASE_NUMBER_FOR_READ_TEST)
                .forEach(rectangle -> assertTrue(
                        this.quadTreeLocation.get(rectangle.bounds()).iterator().hasNext()));
        logger.info("Location QuadTree read Locations {} times took {}", CASE_NUMBER_FOR_READ_TEST,
                counter.sinceStart());

        counter.clear();
        this.rectanglesInsideRight.stream().limit(CASE_NUMBER_FOR_READ_TEST)
                .forEach(rectangle -> assertTrue(
                        this.quadTreeRectangle.get(rectangle.bounds()).iterator().hasNext()));
        logger.info("Rectangle QuadTree read rectangles {} times took {}",
                CASE_NUMBER_FOR_READ_TEST, counter.sinceStart());

        counter.clear();
        this.rectanglesInsideRight.stream().limit(CASE_NUMBER_FOR_READ_TEST)
                .forEach(rectangle -> assertTrue(
                        this.rTreeLocation.get(rectangle.bounds()).iterator().hasNext()));
        logger.info("Location RTree read rectangles {} times took {}", CASE_NUMBER_FOR_READ_TEST,
                counter.sinceStart());

        counter.clear();
        this.rectanglesInsideRight.stream().limit(CASE_NUMBER_FOR_READ_TEST)
                .forEach(rectangle -> assertTrue(
                        this.rTreeRectangle.get(rectangle.bounds()).iterator().hasNext()));
        logger.info("Rectangle RTree read rectangles {} times took {}", CASE_NUMBER_FOR_READ_TEST,
                counter.sinceStart());
    }
}
