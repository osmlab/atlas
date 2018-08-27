package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.routing.AStarRouter;
import org.openstreetmap.atlas.geography.atlas.routing.Router;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for {@link MultiAtlasOverlappingNodesFixer}
 *
 * @author matthieun
 * @author mkalender
 */
public class MultiAtlasOverlappingNodesFixerTest extends Command
{
    private static final Logger logger = LoggerFactory
            .getLogger(MultiAtlasOverlappingNodesFixerTest.class);

    public static final Switch<File> FOLDER = new Switch<>("folder",
            "The folder containing Atlas files for routing test", value -> new File(value),
            Optionality.REQUIRED);
    public static final Switch<Location> START = new Switch<>("start", "The routing start location",
            value -> Location.forString(value), Optionality.REQUIRED);
    public static final Switch<Location> END = new Switch<>("end", "The routing end location",
            value -> Location.forString(value), Optionality.REQUIRED);

    @Rule
    public MultiAtlasOverlappingNodesFixerTestRule setup = new MultiAtlasOverlappingNodesFixerTestRule();

    public static void main(final String[] args)
    {
        new MultiAtlasOverlappingNodesFixerTest().run(args);
    }

    @Test
    public void testOverlappingAtAntimeridian()
    {
        final Atlas subAtlas1 = this.setup.subAtlasOnAntimeridianEast();
        final Atlas subAtlas2 = this.setup.subAtlasOnAntimeridianWest();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        final Router router = AStarRouter.dijkstra(multiAtlas, Distance.meters(40));
        final Route route = router.route(
                Location.forString(MultiAtlasOverlappingNodesFixerTestRule.POINT_1_LOCATION),
                Location.forString(MultiAtlasOverlappingNodesFixerTestRule.POINT_4_LOCATION));
        Assert.assertEquals(2, route.size());
    }

    @Test
    public void testOverlappingNodes()
    {
        final Atlas subAtlas1 = this.setup.overlappingSubAtlas1();
        final Atlas subAtlas2 = this.setup.overlappingSubAtlas2();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        final Router router = AStarRouter.dijkstra(multiAtlas, Distance.meters(40));
        final Route route = router.route(
                Location.forString(MultiAtlasOverlappingNodesFixerTestRule.POINT_5_LOCATION),
                Location.forString(MultiAtlasOverlappingNodesFixerTestRule.POINT_8_LOCATION));
        Assert.assertEquals(2, route.size());
    }

    @Test
    public void testOverlappingNodesCrossingEdges()
    {
        final Atlas subAtlas1 = this.setup.overlappingAndCrossingSubAtlas1();
        final Atlas subAtlas2 = this.setup.overlappingAndCrossingSubAtlas2();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        final Router router = AStarRouter.dijkstra(multiAtlas, Distance.meters(40));
        final Route route1 = router.route(
                Location.forString(MultiAtlasOverlappingNodesFixerTestRule.POINT_5_LOCATION),
                Location.forString(MultiAtlasOverlappingNodesFixerTestRule.POINT_8_LOCATION));
        Assert.assertEquals(2, route1.size());

        final Route route2 = router.route(
                Location.forString(MultiAtlasOverlappingNodesFixerTestRule.POINT_5_LOCATION),
                Location.forString(MultiAtlasOverlappingNodesFixerTestRule.POINT_9_LOCATION));
        Assert.assertEquals(2, route2.size());
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final MultiAtlas atlas = MultiAtlas
                .loadFromPackedAtlas(((File) command.get(FOLDER)).listFilesRecursively().stream()
                        .filter(AtlasResourceLoader.IS_ATLAS).collect(Collectors.toList()));
        final Route route = AStarRouter.dijkstra(atlas, Distance.meters(100))
                .route((Location) command.get(START), (Location) command.get(END));
        logger.info("Route found: {}", route);
        logger.info("Route length: {}", route.length());
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(FOLDER, START, END);
    }
}
