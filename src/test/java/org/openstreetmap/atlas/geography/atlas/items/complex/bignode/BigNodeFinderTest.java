package org.openstreetmap.atlas.geography.atlas.items.complex.bignode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasLoadingCommand;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode.Type;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sid
 * @author mgostintsev
 */
public class BigNodeFinderTest extends AtlasLoadingCommand
{
    private static final Logger logger = LoggerFactory.getLogger(BigNodeFinderTest.class);

    @Rule
    public BigNodeFinderTestCaseRule setup = new BigNodeFinderTestCaseRule();

    public static void main(final String[] args)
    {
        new BigNodeFinderTest().run(args);
    }

    @Test
    public void testBigNodeOverlap()
    {
        final Atlas atlas = this.setup.getOverlapAtlas();
        logger.info("Atlas: {}", atlas);
        final List<BigNode> bigNodes = Iterables.asList(new BigNodeFinder().find(atlas));
        bigNodes.forEach(complexEntity -> logger.info("{}", complexEntity.toString()));
        Assert.assertEquals("Expect to find 18 Big Nodes for overlap atlas", 18, bigNodes.size());

        final Set<BigNode> dualCarriageWayBigNodes = bigNodes.stream()
                .filter(bigNode -> bigNode.getType().equals(Type.DUAL_CARRIAGEWAY))
                .collect(Collectors.toSet());
        Assert.assertEquals("Number of Dual Carrriage Way Big Nodes", 1,
                dualCarriageWayBigNodes.size());

        final Long[] expectedDualCarriageWayNodeIdentifiers = { 4886012999000000L,
                4886012997000000L, 4886012998000000L, 1029583978000000L, 60382597000000L,
                4878996908000000L, 1029583896000000L, 60382598000000L, 4886062964000000L,
                4886062965000000L, 4878996907000000L, 4879025626000000L };

        final Set<Long> dualCarriageWayNodes = dualCarriageWayBigNodes.stream()
                .flatMap(bigNode -> bigNode.nodes().stream()).map(node -> node.getIdentifier())
                .collect(Collectors.toSet());
        Arrays.stream(expectedDualCarriageWayNodeIdentifiers)
                .forEach(nodeId -> Assert.assertTrue(dualCarriageWayNodes.contains(nodeId)));
    }

    @Test
    public void testDualCarriageWayMerging()
    {
        logger.info("Atlas: {}", this.setup.getAtlas());
        final List<BigNode> bigNodes = Iterables
                .asList(new BigNodeFinder().find(this.setup.getAtlas()));
        bigNodes.forEach(complexEntity -> logger.info("{}", complexEntity.toString()));
        logger.info("Total Number of big Nodes :{}", bigNodes.size());
        Assert.assertEquals("Total Number of big Nodes (Simple + Dual Carrriage Way)", 58,
                bigNodes.size());
        final Set<BigNode> dualCarriageWayBigNodes = bigNodes.stream()
                .filter(bigNode -> bigNode.getType().equals(Type.DUAL_CARRIAGEWAY))
                .collect(Collectors.toSet());
        Assert.assertEquals("Number of Dual Carrriage Way Big Nodes", 12,
                dualCarriageWayBigNodes.size());
        final Set<BigNode> simpleNodes = bigNodes.stream()
                .filter(bigNode -> bigNode.getType().equals(Type.SIMPLE))
                .collect(Collectors.toSet());

        final Long[] expectedDualCarriageWayNodeIdentifiers = { 2L, 4L, 5L, 6L, 13L, 14L, 15L, 20L,
                21L, 22L, 23L, 32L, 33L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 49L, 50L, 52L, 53L,
                56L, 57L, 59L, 60L, 79L, 80L };

        final Set<Long> dualCarriageWayNodes = dualCarriageWayBigNodes.stream()
                .flatMap(bigNode -> bigNode.nodes().stream()).map(node -> node.getIdentifier())
                .collect(Collectors.toSet());
        Arrays.stream(expectedDualCarriageWayNodeIdentifiers)
                .forEach(nodeId -> Assert.assertTrue(dualCarriageWayNodes.contains(nodeId)));

        // Node Identifiers where bigNodes shouldn't be added
        final Long[] expectedSimpleNodeIdentifiers = { 62L, 63L, 88L, 89L };
        final Set<Long> expectedSimpleNodeIdentifiersSet = new HashSet<>(
                Arrays.asList(expectedSimpleNodeIdentifiers));
        dualCarriageWayBigNodes.forEach(bigNode -> bigNode.nodes().forEach(node -> Assert
                .assertFalse(expectedSimpleNodeIdentifiersSet.contains(node.getIdentifier()))));

        // Node Identifiers where not even simple nodes shouldn't be added
        final Long[] expectedPierNodeIdentifiers = { 66L, 67L, 68L, 69L };
        final Set<Long> expectedPierNodeIdentifiersSet = new HashSet<>(
                Arrays.asList(expectedPierNodeIdentifiers));
        dualCarriageWayBigNodes.forEach(bigNode -> bigNode.nodes().forEach(node -> Assert
                .assertFalse(expectedPierNodeIdentifiersSet.contains(node.getIdentifier()))));
        simpleNodes.forEach(bigNode -> bigNode.nodes().forEach(node -> Assert
                .assertFalse(expectedPierNodeIdentifiersSet.contains(node.getIdentifier()))));
    }

    @Test
    public void testBigNodeExpansion()
    {
        final Atlas atlas = this.setup.getExpandBigNodeAtlas();
        final List<BigNode> bigNodes = Iterables.asList(new BigNodeFinder().find(atlas));
        bigNodes.forEach(complexEntity -> logger.info("{}", complexEntity.toString()));
        logger.info("Total Number of big Nodes :{}", bigNodes.size());
        final Set<BigNode> dualCarriageWayBigNodes = bigNodes.stream()
                .filter(bigNode -> bigNode.getType().equals(Type.DUAL_CARRIAGEWAY))
                .collect(Collectors.toSet());
        Assert.assertEquals("Number of Dual Carrriage Way Big Nodes", 1,
                dualCarriageWayBigNodes.size());
        final Long[] expectedDualCarriageWayNodeIdentifiers = { 97800560000000L, 268842854000000L,
                97800351000000L, 268842855000000L };
        final Set<Long> dualCarriageWayNodes = dualCarriageWayBigNodes.stream()
                .flatMap(bigNode -> bigNode.nodes().stream()).map(node -> node.getIdentifier())
                .collect(Collectors.toSet());
        Arrays.stream(expectedDualCarriageWayNodeIdentifiers)
                .forEach(nodeId -> Assert.assertTrue(dualCarriageWayNodes.contains(nodeId)));
    }

    @Test
    public void testPathsThroughComplexJunction()
    {
        final Atlas atlas = this.setup.getComplexJunctionAtlas();
        logger.info("Atlas: {}", atlas);
        final List<BigNode> bigNodes = Iterables.asList(new BigNodeFinder().find(atlas));
        bigNodes.forEach(complexEntity -> logger.info("{}", complexEntity.toString()));
        Assert.assertEquals("Expect to find 7 Big Nodes for this atlas", 7, bigNodes.size());

        Time timeNow = Time.now();
        final Set<Route> shortestRoutes = new HashSet<>();
        bigNodes.forEach(bigNode -> shortestRoutes.addAll(bigNode.shortestPaths()));
        logger.info("Took {} to find {} shortest big node routes", timeNow.elapsedSince(),
                shortestRoutes.size());

        logger.info("Big Node Shortest Routes: {} ", shortestRoutes);
        Assert.assertEquals("Expect to find 18 shortest paths through these Big Nodes", 18,
                shortestRoutes.size());

        timeNow = Time.now();
        final Set<Route> allRoutes = new HashSet<>();

        bigNodes.forEach(bigNode -> allRoutes.addAll(bigNode.allPaths()));
        logger.info("Took {} to find all {} big node routes", timeNow.elapsedSince(),
                allRoutes.size());

        logger.info("Big Node All Routes: {} ", allRoutes);
        Assert.assertEquals("Expect to find 26 total paths through these Big Nodes", 26,
                allRoutes.size());

        Assert.assertTrue("Make sure the shortest routes are a subset of allRoutes",
                allRoutes.containsAll(shortestRoutes));

        final Route nonShortestValidRoute = Route.forEdges(atlas.edge(-3), atlas.edge(-2),
                atlas.edge(2), atlas.edge(3));
        Assert.assertTrue("Valid route should be absent from the shortest path set",
                !shortestRoutes.contains(nonShortestValidRoute));
        Assert.assertTrue("Valid route should be present in the total path set",
                allRoutes.contains(nonShortestValidRoute));
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File folder = (File) command.get(INPUT_FOLDER);
        final Atlas atlas = loadAtlas(command);
        final File bigNodesFile = folder.child("improved.bigNodes.geojson");
        final JsonWriter writer = new JsonWriter(bigNodesFile);
        writer.write(new GeoJsonBuilder().create(
                Iterables.translate(new BigNodeFinder().find(atlas), BigNode::asGeoJsonBigNode))
                .jsonObject());
        writer.close();
        return 0;
    }
}
