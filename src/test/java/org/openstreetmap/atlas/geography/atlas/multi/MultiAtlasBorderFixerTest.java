package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.routing.AStarRouter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Tests for {@link MultiAtlasBorderFixer}
 *
 * @author mkalender
 */
public class MultiAtlasBorderFixerTest
{
    @Rule
    public final MultiAtlasBorderFixerTestRule setup = new MultiAtlasBorderFixerTestRule();

    private static void verifyEdge(final Edge edge, final String... locations)
    {
        Assert.assertEquals(edge.asPolyLine(),
                new PolyLine(StreamSupport.stream(Iterables.from(locations).spliterator(), false)
                        .map(Location::forString).collect(Collectors.toList())));
    }

    @Test
    public void oneWaySubAtlas1From1To4WithInnerLocations()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1From1To4WithInnerLocations();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2From1To4WithInnerLocations();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_4_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_4_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_5_LOCATION);
    }

    @Test
    public void testOneWaySubAtlasTwoWayInnerConnection()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1From1To3();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2From1To4WithTwoWayInnerConnection();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify connections
        // POINT_1_LOCATION
        Assert.assertTrue(
                multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).inEdges().isEmpty());
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120001L)));

        // POINT_2_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120001L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).inEdges()
                .contains(multiAtlas.edge(223456789120000L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120002L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).outEdges()
                .contains(multiAtlas.edge(-223456789120000L)));

        // POINT_3_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120002L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).outEdges()
                .isEmpty());

        // POINT_4
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_4_ID_LONG).inEdges()
                .contains(multiAtlas.edge(-223456789120000L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_4_ID_LONG).outEdges()
                .contains(multiAtlas.edge(223456789120000L)));
    }

    @Test
    public void testOneWaySubAtlasWithFourNodes()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1From1To4();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2From1To4();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify third edge
        verifyEdge(multiAtlas.edge(123456789120003L),
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_4_LOCATION);

        // Verify connections
        // POINT_1_LOCATION
        Assert.assertTrue(
                multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).inEdges().isEmpty());
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120001L)));

        // POINT_2_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120001L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120002L)));

        // POINT_3_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120002L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120003L)));

        // POINT_4_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_4_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120003L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_4_ID_LONG).outEdges()
                .isEmpty());
    }

    @Test
    public void testOneWaySubAtlasWithInconsistentIds()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1From1To3WithInconsistentIds();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2From1To3WithInconsistentIds();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify connections
        // POINT_1_LOCATION
        Assert.assertTrue(
                multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).inEdges().isEmpty());
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120001L)));

        // POINT_2_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120001L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120002L)));

        // POINT_3_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120002L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).outEdges()
                .isEmpty());
    }

    @Test
    public void testOneWaySubAtlasWithInconsistentRoads()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1From1To3WithInconsistentRoads();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2From1To3WithInconsistentRoads();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify edges are NOT gone even though they were forming an inconsistent road
        Assert.assertNotNull(multiAtlas.edge(123456789120000L));
        Assert.assertNotNull(multiAtlas.edge(123456789120001L));
        Assert.assertNotNull(multiAtlas.edge(123456789120002L));

        // Verify fixed edges
        verifyEdge(multiAtlas.edge(223456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_5_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_6_LOCATION);
        verifyEdge(multiAtlas.edge(223456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_6_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_7_LOCATION);
    }

    @Test
    public void testOneWaySubAtlasWithInconsistentRoadsAndARelation()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1WithInconsistentRoadsAndARelation();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2WithInconsistentRoadsAndARelation();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify edges are NOT gone even though they were forming an inconsistent road
        Assert.assertNotNull(multiAtlas.edge(123456789120000L));
        Assert.assertNotNull(multiAtlas.edge(123456789120001L));
        Assert.assertNotNull(multiAtlas.edge(123456789120002L));

        // Verify that relation has members
        Assert.assertEquals(multiAtlas.relation(987654321L).members().size(), 2);
        Assert.assertEquals(multiAtlas.relation(887654321L).members().size(), 1);

        // Verify fixed edges
        verifyEdge(multiAtlas.edge(223456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_5_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_6_LOCATION);
        verifyEdge(multiAtlas.edge(223456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_6_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_7_LOCATION);
    }

    @Test
    public void testOneWaySubAtlasWithOneWayInnerConnection()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1From1To3WithOneWayInnerConnection();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2From1To4WithOneWayInnerConnection();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify connections
        // POINT_1_LOCATION
        Assert.assertTrue(
                multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).inEdges().isEmpty());
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120001L)));

        // POINT_2_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120001L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).inEdges()
                .contains(multiAtlas.edge(223456789120000L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120002L)));

        // POINT_3_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120002L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).outEdges()
                .isEmpty());

        // POINT_4
        Assert.assertTrue(
                multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_4_ID_LONG).inEdges().isEmpty());
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_4_ID_LONG).outEdges()
                .contains(multiAtlas.edge(223456789120000L)));
    }

    @Test
    public void testOneWaySubAtlasWithOuterConnections()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1From1To5WithOuterConnections();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2From1To5WithOuterConnections();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify connections
        // POINT_4
        Assert.assertTrue(
                multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_4_ID_LONG).inEdges().isEmpty());
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_4_ID_LONG).outEdges()
                .contains(multiAtlas.edge(223456789120000L)));

        // POINT_1_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).inEdges()
                .contains(multiAtlas.edge(223456789120000L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120001L)));

        // POINT_2_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120001L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120002L)));

        // POINT_3_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120002L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).outEdges()
                .contains(multiAtlas.edge(323456789120000L)));

        // POINT_5
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_5_ID_LONG).inEdges()
                .contains(multiAtlas.edge(323456789120000L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_5_ID_LONG).outEdges()
                .isEmpty());
    }

    @Test
    public void testOneWaySubAtlasWithRelations()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1From1To3WithRelations();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2From1To3WithRelations();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify relations
        Assert.assertEquals(multiAtlas.edge(123456789120001L).relations().size(), 2);
        Assert.assertEquals(multiAtlas.edge(123456789120002L).relations().size(), 2);

        // Verify that relation has members
        Assert.assertEquals(multiAtlas.relation(987654321L).members().size(), 2);
        Assert.assertEquals(multiAtlas.relation(887654321L).members().size(), 2);
    }

    @Test
    public void testOneWaySubAtlasWithTags()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1From1To3WithTags();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2From1To3WithTags();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify tags
        subAtlas1.edge(123456789120001L).getTags().forEach((key, value) ->
        {
            Assert.assertTrue(multiAtlas.edge(123456789120001L).getTags().containsKey(key));
            Assert.assertEquals(multiAtlas.edge(123456789120001L).getTags().get(key), value);
            Assert.assertTrue(multiAtlas.edge(123456789120002L).getTags().containsKey(key));
            Assert.assertEquals(multiAtlas.edge(123456789120002L).getTags().get(key), value);
        });

        subAtlas2.edge(123456789120001L).getTags().forEach((key, value) ->
        {
            Assert.assertTrue(multiAtlas.edge(123456789120001L).getTags().containsKey(key));
            Assert.assertEquals(multiAtlas.edge(123456789120001L).getTags().get(key), value);
            Assert.assertTrue(multiAtlas.edge(123456789120002L).getTags().containsKey(key));
            Assert.assertEquals(multiAtlas.edge(123456789120002L).getTags().get(key), value);
        });

        subAtlas2.edge(123456789120002L).getTags().forEach((key, value) ->
        {
            Assert.assertTrue(multiAtlas.edge(123456789120001L).getTags().containsKey(key));
            Assert.assertEquals(multiAtlas.edge(123456789120001L).getTags().get(key), value);
            Assert.assertTrue(multiAtlas.edge(123456789120002L).getTags().containsKey(key));
            Assert.assertEquals(multiAtlas.edge(123456789120002L).getTags().get(key), value);
        });
    }

    @Test
    public void testOneWaySubAtlasWithThreeNodes()
    {
        final Atlas subAtlas1 = this.setup.oneWaySubAtlas1From1To3();
        final Atlas subAtlas2 = this.setup.oneWaySubAtlas2From1To3();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify connections
        // POINT_1_LOCATION
        Assert.assertTrue(
                multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).inEdges().isEmpty());
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_1_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120001L)));

        // POINT_2_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120001L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_2_ID_LONG).outEdges()
                .contains(multiAtlas.edge(123456789120002L)));

        // POINT_3_LOCATION
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).inEdges()
                .contains(multiAtlas.edge(123456789120002L)));
        Assert.assertTrue(multiAtlas.node(MultiAtlasBorderFixerTestRule.POINT_3_ID_LONG).outEdges()
                .isEmpty());
    }

    @Test
    public void testOneWayWithDuplicateNode()
    {
        final Atlas subAtlas1 = this.setup.oneWayWithDuplicateNodeSubAtlas1();
        final Atlas subAtlas2 = this.setup.oneWayWithDuplicateNodeSubAtlas2();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_4_LOCATION);

        // Verify third edge
        verifyEdge(multiAtlas.edge(123456789120003L),
                MultiAtlasBorderFixerTestRule.POINT_4_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_5_LOCATION);
    }

    @Test
    public void testThreeSubAtlas()
    {
        final Atlas subAtlas1 = this.setup.aSubAtlas();
        final Atlas subAtlas2 = this.setup.anotherSubAtlas();
        final Atlas subAtlas3 = this.setup.aThirdSubAtlas();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2, subAtlas3);

        // Verify route
        final Route route = AStarRouter.dijkstra(multiAtlas, Distance.TEN_MILES).route(
                Location.forString(MultiAtlasBorderFixerTestRule.POINT_1_LOCATION),
                Location.forString(MultiAtlasBorderFixerTestRule.POINT_5_LOCATION));
        Assert.assertEquals(4, route.size());

        // Verify this inconsistent edge is gone
        Assert.assertNull(multiAtlas.edge(223456789120000L));
    }

    @Test
    public void testThreeWaySubAtlas()
    {
        // In this test, fix couldn't be applied and edges from first atlas will be used
        final Atlas subAtlas1 = this.setup.threeWaySubAtlas1();
        final Atlas subAtlas2 = this.setup.threeWaySubAtlas2();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify these edges exist
        Assert.assertNotNull(multiAtlas.edge(123456789120001L));
        Assert.assertNotNull(multiAtlas.edge(123456789120002L));
        Assert.assertNotNull(multiAtlas.edge(123456789120004L));
        Assert.assertNotNull(multiAtlas.edge(123456789120005L));

        // Verify this edge does NOT exist
        Assert.assertNull(multiAtlas.edge(123456789120003L));
    }

    @Test
    public void testThreeWayWithLoopSubAtlas()
    {
        final Atlas subAtlas1 = this.setup.threeWayWithLoopSubAtlas1();
        final Atlas subAtlas2 = this.setup.threeWayWithLoopSubAtlas2();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify third edge
        verifyEdge(multiAtlas.edge(123456789120003L),
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_4_LOCATION);

        // Verify fourth edge
        verifyEdge(multiAtlas.edge(123456789120004L),
                MultiAtlasBorderFixerTestRule.POINT_4_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify fifth edge
        verifyEdge(multiAtlas.edge(123456789120005L),
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_5_LOCATION);
    }

    @Test
    public void testTwoWaySubAtlasWithThreeNodes()
    {
        final Atlas subAtlas1 = this.setup.twoWaySubAtlas1From1To3();
        final Atlas subAtlas2 = this.setup.twoWaySubAtlas2From1To3();
        final MultiAtlas multiAtlas = new MultiAtlas(subAtlas1, subAtlas2);

        // Verify first edge
        verifyEdge(multiAtlas.edge(123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);

        // Verify first reverse edge
        verifyEdge(multiAtlas.edge(-123456789120001L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_1_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION);

        // Verify second edge
        verifyEdge(multiAtlas.edge(-123456789120002L),
                MultiAtlasBorderFixerTestRule.POINT_3_LOCATION,
                MultiAtlasBorderFixerTestRule.POINT_2_LOCATION);
    }
}
