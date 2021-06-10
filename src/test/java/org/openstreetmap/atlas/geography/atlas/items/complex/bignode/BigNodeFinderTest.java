package org.openstreetmap.atlas.geography.atlas.items.complex.bignode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasLoadingCommand;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode.Type;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
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
    public void testBigNodeCandidateComaprator()
    {
        final Atlas atlas = this.setup.getAtlas();
        final BigNodeFinder.NodeComparator comparator = new BigNodeFinder.NodeComparator();
        final Node node1 = atlas.node(1);
        final Node node2 = atlas.node(2);
        final Node node3 = atlas.node(3);
        final Set<Node> set1 = new TreeSet<>(comparator);
        set1.add(node1);
        set1.add(node2);
        final Set<Node> set2 = new TreeSet<>(comparator);
        set2.add(node2);
        set2.add(node1);
        final Set<Node> set3 = new TreeSet<>(comparator);
        set3.add(node3);
        set3.add(node1);
        final BigNodeFinder.BigNodeCandidate candidate1 = BigNodeFinder.BigNodeCandidate.from(set1);
        final BigNodeFinder.BigNodeCandidate candidate2 = BigNodeFinder.BigNodeCandidate.from(set2);
        final BigNodeFinder.BigNodeCandidate candidate3 = BigNodeFinder.BigNodeCandidate.from(set3);
        Assert.assertEquals(0, candidate1.compareTo(candidate2));
        Assert.assertEquals(-1, candidate2.compareTo(candidate3));
        Assert.assertEquals(1, candidate3.compareTo(candidate1));
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
    public void testBigNodeOverlap()
    {
        final Atlas atlas = this.setup.getOverlapAtlas();
        logger.info("Atlas: {}", atlas);
        final List<BigNode> bigNodes = Iterables.asList(new BigNodeFinder().find(atlas));
        bigNodes.forEach(complexEntity -> logger.info("{}", complexEntity.toString()));
        Assert.assertEquals("Expect to find 20 Big Nodes for overlap atlas", 20, bigNodes.size());

        final Set<BigNode> dualCarriageWayBigNodes = bigNodes.stream()
                .filter(bigNode -> bigNode.getType().equals(Type.DUAL_CARRIAGEWAY))
                .collect(Collectors.toSet());
        Assert.assertEquals("Number of Dual Carrriage Way Big Nodes", 1,
                dualCarriageWayBigNodes.size());

        final Long[] expectedDualCarriageWayNodeIdentifiers = { 4886012997000000L,
                4886012998000000L, 1029583978000000L, 60382597000000L, 1029583896000000L,
                60382598000000L, 4886062964000000L, 4886062965000000L, 4878996907000000L,
                4879025626000000L };

        final Set<Long> dualCarriageWayNodes = dualCarriageWayBigNodes.stream()
                .flatMap(bigNode -> bigNode.nodes().stream()).map(node -> node.getIdentifier())
                .collect(Collectors.toSet());
        Arrays.stream(expectedDualCarriageWayNodeIdentifiers)
                .forEach(nodeId -> Assert.assertTrue(dualCarriageWayNodes.contains(nodeId)));
    }

    @Test
    public void testConfigurableSearchRadius()
    {
        final Map<String, Distance> configurableRadius = new HashMap<>();
        configurableRadius.put(HighwayTag.MOTORWAY.name().toLowerCase(), Distance.meters(10));
        configurableRadius.put(HighwayTag.TRUNK.name().toLowerCase(), Distance.meters(10));
        configurableRadius.put(HighwayTag.PRIMARY.name().toLowerCase(), Distance.meters(10));
        configurableRadius.put(HighwayTag.SECONDARY.name().toLowerCase(), Distance.meters(10));
        configurableRadius.put(HighwayTag.TERTIARY.name().toLowerCase(), Distance.meters(10));
        configurableRadius.put(HighwayTag.RESIDENTIAL.name().toLowerCase(), Distance.meters(5));

        final Atlas atlas = this.setup.getOverMergeAtlas();
        logger.info("Atlas: {}", atlas);
        final List<BigNode> bigNodes = Iterables
                .asList(new BigNodeFinder(configurableRadius, null).find(atlas));
        final Set<BigNode> dualCarriageWayBigNodes = bigNodes.stream()
                .filter(bigNode -> bigNode.getType().equals(Type.DUAL_CARRIAGEWAY))
                .collect(Collectors.toSet());
        Assert.assertEquals("Expect to find 2 Dual Carriageway Big Node for this atlas", 2,
                dualCarriageWayBigNodes.size());
        final Set<Long> dualCarriageWayNodes = dualCarriageWayBigNodes.stream()
                .flatMap(bigNode -> bigNode.nodes().stream()).map(node -> node.getIdentifier())
                .collect(Collectors.toSet());
        Assert.assertEquals("Expect to find 7 Dual Carriageway Sub Nodes", 7,
                dualCarriageWayNodes.size());
    }

    @Test
    public void testHasJunctionEdgeTags()
    {
        final Map<String, Distance> configurableRadius = new HashMap<>();
        configurableRadius.put(HighwayTag.MOTORWAY.name().toLowerCase(), Distance.meters(10));
        configurableRadius.put(HighwayTag.TRUNK.name().toLowerCase(), Distance.meters(10));
        configurableRadius.put(HighwayTag.PRIMARY.name().toLowerCase(), Distance.meters(10));
        configurableRadius.put(HighwayTag.SECONDARY.name().toLowerCase(), Distance.meters(10));
        configurableRadius.put(HighwayTag.TERTIARY.name().toLowerCase(), Distance.meters(10));
        configurableRadius.put(HighwayTag.RESIDENTIAL.name().toLowerCase(), Distance.meters(5));

        final Map<String, String> nonJunctionEdgeTagMap = new HashMap<>();
        nonJunctionEdgeTagMap.put("test.way", "CROSSWALK");

        final Atlas atlas = this.setup.getOverMergeAtlas();
        logger.info("Atlas: {}", atlas);
        final List<BigNode> bigNodes = Iterables
                .asList(new BigNodeFinder(configurableRadius, nonJunctionEdgeTagMap).find(atlas));
        final Set<BigNode> dualCarriageWayBigNodes = bigNodes.stream()
                .filter(bigNode -> bigNode.getType().equals(Type.DUAL_CARRIAGEWAY))
                .collect(Collectors.toSet());
        Assert.assertEquals("Expect to find 1 Dual Carriageway Big Node for this atlas", 1,
                dualCarriageWayBigNodes.size());
        final Set<Long> dualCarriageWayNodes = dualCarriageWayBigNodes.stream()
                .flatMap(bigNode -> bigNode.nodes().stream()).map(node -> node.getIdentifier())
                .collect(Collectors.toSet());
        Assert.assertEquals("Expect to find 4 Dual Carriageway Sub Nodes", 4,
                dualCarriageWayNodes.size());
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
    public void testExcludeLinkRoadAsDualCarriageWayDNK()
    {
        final Atlas atlas = this.setup.getDNKAtlasToTestExcludeLinkRoadAsDualCarriageWay();
        logger.info("Atlas: {}", atlas);
        final List<BigNode> bigNodes = Iterables.asList(new BigNodeFinder().find(atlas));
        final Set<BigNode> dualCarriageWayBigNodes = bigNodes.stream()
                .filter(bigNode -> bigNode.getType().equals(Type.DUAL_CARRIAGEWAY))
                .collect(Collectors.toSet());
        dualCarriageWayBigNodes.forEach(bigNode -> logger.info("{}", bigNode.toString()));
        Assert.assertEquals("Expect to find 1 Dual Carriageway Big Node for this atlas", 1,
                dualCarriageWayBigNodes.size());
        final Set<Long> dualCarriageWayNodes = dualCarriageWayBigNodes.stream()
                .flatMap(bigNode -> bigNode.nodes().stream()).map(node -> node.getIdentifier())
                .collect(Collectors.toSet());
        Assert.assertEquals("Expect to find 6 Dual Carriageway Sub Nodes", 6,
                dualCarriageWayNodes.size());
        final long[] nodeIdentfiers = { 29971835000000L, 29971833000000L, 6378459073000000L,
                6378459072000000L, 6378459074000000L, 6378459067000000L };
        Arrays.asList(nodeIdentfiers).forEach(nodeIdentifier -> Assert
                .assertFalse(dualCarriageWayNodes.contains(nodeIdentifier)));
    }

    @Test
    public void testExcludeLinkRoadAsDualCarriageWayUKR()
    {
        final Atlas atlas = this.setup.getUKRAtlasToTestExcludeLinkRoadAsDualCarriageWay();
        logger.info("Atlas: {}", atlas);
        final List<BigNode> bigNodes = Iterables.asList(new BigNodeFinder().find(atlas));
        final Set<BigNode> dualCarriageWayBigNodes = bigNodes.stream()
                .filter(bigNode -> bigNode.getType().equals(Type.DUAL_CARRIAGEWAY))
                .collect(Collectors.toSet());
        dualCarriageWayBigNodes.forEach(bigNode -> logger.info("{}", bigNode.toString()));
        Assert.assertEquals("Expect to find 0 Dual Carriageway Big Node for this atlas", 0,
                dualCarriageWayBigNodes.size());
        Assert.assertEquals("Expect to find 31 simple big nodes", 31, bigNodes.size());
    }

    @Test
    public void testOverMergeBigNodes()
    {
        final Atlas atlas = this.setup.getOverMergeAtlas();
        logger.info("Atlas: {}", atlas);
        final List<BigNode> bigNodes = Iterables.asList(new BigNodeFinder().find(atlas));
        final Set<BigNode> dualCarriageWayBigNodes = bigNodes.stream()
                .filter(bigNode -> bigNode.getType().equals(Type.DUAL_CARRIAGEWAY))
                .collect(Collectors.toSet());
        dualCarriageWayBigNodes.forEach(bigNode -> logger.info("{}", bigNode.toString()));
        Assert.assertEquals("Expect to find 1 Dual Carriageway Big Node for this atlas", 1,
                dualCarriageWayBigNodes.size());
        final Set<Long> dualCarriageWayNodes = dualCarriageWayBigNodes.stream()
                .flatMap(bigNode -> bigNode.nodes().stream()).map(node -> node.getIdentifier())
                .collect(Collectors.toSet());
        Assert.assertEquals("Expect to find 13 Dual Carriageway Sub Nodes", 13,
                dualCarriageWayNodes.size());
        final Long[] overMergeNodeIdentifiers = { 4862123767000000L, 1186028310000000L,
                1951545347000000L, 4931063979000000L };
        Arrays.asList(overMergeNodeIdentifiers).forEach(nodeIdentifier -> Assert
                .assertFalse(dualCarriageWayNodes.contains(nodeIdentifier)));
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

    @Test
    public void testUTurnShapeEdgeAsJunctionEdge()
    {
        final Atlas atlas = this.setup.getuTurnShapeEdgeAtlas();
        final Edge edge1 = atlas.edge(798542598000001L);
        final Edge edge2 = atlas.edge(798542598000002L);
        final BigNodeFinder finder = new BigNodeFinder();
        Assert.assertFalse(finder.startAndEndNodesConnectedToSameEdge(edge1));
        Assert.assertTrue(finder.startAndEndNodesConnectedToSameEdge(edge2));
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
