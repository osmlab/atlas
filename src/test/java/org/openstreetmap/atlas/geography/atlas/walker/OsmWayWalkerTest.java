package org.openstreetmap.atlas.geography.atlas.walker;

import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.EdgeWalker.EdgeHandler;

/**
 * @author brian_l_davis
 */
public class OsmWayWalkerTest
{
    /**
     * @author brian_l_davis
     */
    public static class MockEdgeHandler implements EdgeHandler
    {
        private long edges = 0;
        private long boundaries = 0;

        public long getBoundaries()
        {
            return this.boundaries;
        }

        public long getEdges()
        {
            return this.edges;
        }

        @Override
        public void handleBoundaryEdge(final Edge edge)
        {
            this.boundaries++;
        }

        @Override
        public void handleEdge(final Edge edge)
        {
            this.edges++;
        }
    }

    @Rule
    public final OsmWayWalkerTestRule setup = new OsmWayWalkerTestRule();

    @Test
    public void testCircularPath()
    {
        final Edge startingEdge = this.setup.getRoundAbout().edge(169884263000003L);
        final Set<Edge> way = new OsmWayWalker(startingEdge).collectEdges();

        Assert.assertEquals("Wrong number of edges found", 4, way.size());
        Assert.assertArrayEquals(way.stream().map(AtlasObject::getIdentifier).toArray(),
                new Long[] { 169884263000001L, 169884263000002L, 169884263000003L,
                        169884263000004L });
    }

    @Test
    public void testNonWaySectioned()
    {
        final Edge startingEdge = this.setup.getSimpleNetwork().edge(30647522000000L);
        final Set<Edge> way = new OsmWayWalker(startingEdge).collectEdges();

        Assert.assertEquals("Wrong number of edges found", 1, way.size());
        Assert.assertArrayEquals(way.stream().map(AtlasObject::getIdentifier).toArray(),
                new Long[] { 30647522000000L });
    }

    @Test
    public void testStatisticsBeginningReverseStart()
    {
        final MockEdgeHandler statisticsHandler = new MockEdgeHandler();

        final Edge startingEdge = this.setup.getIsolatedEdge().edge(-1000001L);
        new OsmWayWalker(startingEdge, statisticsHandler).collectEdges();

        Assert.assertEquals("Wrong number of edges counted", 4, statisticsHandler.getEdges());
        Assert.assertEquals("Wrong number of boundaries counted", 2,
                statisticsHandler.getBoundaries());
    }

    @Test
    public void testStatisticsBeginningStart()
    {
        final MockEdgeHandler statisticsHandler = new MockEdgeHandler();

        final Edge startingEdge = this.setup.getIsolatedEdge().edge(1000001L);
        new OsmWayWalker(startingEdge, statisticsHandler).collectEdges();

        Assert.assertEquals("Wrong number of edges counted", 4, statisticsHandler.getEdges());
        Assert.assertEquals("Wrong number of boundaries counted", 2,
                statisticsHandler.getBoundaries());
    }

    @Test
    public void testStatisticsEndStart()
    {
        final MockEdgeHandler statisticsHandler = new MockEdgeHandler();

        final Edge startingEdge = this.setup.getIsolatedEdge().edge(1000004L);
        new OsmWayWalker(startingEdge, statisticsHandler).collectEdges();

        Assert.assertEquals("Wrong number of edges counted", 4, statisticsHandler.getEdges());
        Assert.assertEquals("Wrong number of boundaries counted", 2,
                statisticsHandler.getBoundaries());
    }

    @Test
    public void testStatisticsMiddleStart()
    {
        final MockEdgeHandler statisticsHandler = new MockEdgeHandler();

        final Edge startingEdge = this.setup.getIsolatedEdge().edge(1000003L);
        new OsmWayWalker(startingEdge, statisticsHandler).collectEdges();

        Assert.assertEquals("Wrong number of edges counted", 4, statisticsHandler.getEdges());
        Assert.assertEquals("Wrong number of boundaries counted", 2,
                statisticsHandler.getBoundaries());
    }

    @Test
    public void testWaySectioned()
    {
        final Edge startingEdge = this.setup.getSimpleNetwork().edge(30647513000003L);
        final Set<Edge> way = new OsmWayWalker(startingEdge).collectEdges();

        Assert.assertEquals("Wrong number of edges found", 5, way.size());
        Assert.assertArrayEquals(way.stream().map(AtlasObject::getIdentifier).toArray(),
                new Long[] { 30647513000001L, 30647513000002L, 30647513000003L, 30647513000004L,
                        30647513000005L });
    }
}
