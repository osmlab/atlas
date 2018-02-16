package org.openstreetmap.atlas.geography.atlas.raw.sectioning;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.LineAndPointSlicingTest;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;

import com.google.common.collect.Iterables;

/**
 * {@link WaySectionProcessor} unit tests
 *
 * @author mgostintsev
 */
public class WaySectionProcessorTest
{
    private static CountryBoundaryMap COUNTRY_BOUNDARY_MAP;

    static
    {
        COUNTRY_BOUNDARY_MAP = new CountryBoundaryMap(
                new InputStreamResource(() -> LineAndPointSlicingTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
    }

    @Rule
    public WaySectionProcessorTestRule setup = new WaySectionProcessorTestRule();

    @Test
    public void testBidirectionalRing()
    {
        // Based on https://www.openstreetmap.org/way/317579533
        final Atlas slicedRawAtlas = this.setup.getBidirectionalRingAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Two edges, each having a reverse counterpart", 4,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());

        Assert.assertTrue(finalAtlas.edge(317579533000001L) != null);
        Assert.assertTrue(finalAtlas.edge(-317579533000001L) != null);
        Assert.assertTrue(finalAtlas.edge(317579533000002L) != null);
        Assert.assertTrue(finalAtlas.edge(-317579533000002L) != null);
    }

    @Test
    public void testLineWithLoopAtEnd()
    {
        // Based on https://www.openstreetmap.org/way/461101743
        final Atlas slicedRawAtlas = this.setup.getLineWithLoopAtEndAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Two edges, each having a reverse counterpart", 4,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        Assert.assertEquals(4, finalAtlas.node(4566499618000000L).connectedEdges().size());
        Assert.assertEquals(2, finalAtlas.node(4566499619000000L).connectedEdges().size());
    }

    @Test
    public void testLineWithLoopAtStart()
    {
        // Based on https://www.openstreetmap.org/way/460419987
        final Atlas slicedRawAtlas = this.setup.getLineWithLoopAtStartAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Three edges, each having a reverse counterpart", 6,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Three nodes", 3, finalAtlas.numberOfNodes());
        Assert.assertEquals(4, finalAtlas.node(4560902689000000L).connectedEdges().size());
        Assert.assertEquals(2, finalAtlas.node(4560902695000000L).connectedEdges().size());
        Assert.assertEquals(4, finalAtlas.node(4560902693000000L).connectedEdges().size());
    }

    @Test
    public void testLoopingWayWithIntersection()
    {
        // Based on https://www.openstreetmap.org/way/310540517 and partial excerpt of
        // https://www.openstreetmap.org/way/310540519
        final Atlas slicedRawAtlas = this.setup.getLoopingWayWithIntersectionAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Four edges, each having a reverse counterpart", 8,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Four nodes", 4, finalAtlas.numberOfNodes());
        Assert.assertTrue("This way got sectioned 3 times, with reverse edges", Iterables
                .size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 310540517L)) == 6);
        Assert.assertTrue("This edge got sectioned once, with reverse edges", Iterables
                .size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 310540519L)) == 2);
    }

    @Test
    public void testOneWayRing()
    {
        // Based on https://www.openstreetmap.org/way/460257372
        final Atlas slicedRawAtlas = this.setup.getOneWayRingAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Two edges, the ring got sectioned in the middle", 2,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        finalAtlas.edges().forEach(
                edge -> Assert.assertFalse("No edge has a reverse edge", edge.hasReverseEdge()));
    }

    @Test
    public void testOneWaySimpleLine()
    {
        // Based on https://www.openstreetmap.org/way/109486264
        final Atlas slicedRawAtlas = this.setup.getOneWaySimpleLineAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("One edge", 1, finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        finalAtlas.edges().forEach(
                edge -> Assert.assertFalse("No edge has a reverse edge", edge.hasReverseEdge()));
    }

    @Test
    public void testReversedOneWayLine()
    {
        // Based on https://www.openstreetmap.org/way/333112568
        final Atlas slicedRawAtlas = this.setup.getReversedOneWayLineAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("One edge", 1, finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        Assert.assertEquals("Verify that the direction of the original line was reversed",
                slicedRawAtlas.line(333112568000000L).asPolyLine(),
                finalAtlas.edge(333112568000001L).asPolyLine().reversed());
    }

    @Test
    public void testRingWithSingleIntersection()
    {
        // Based on https://www.openstreetmap.org/way/460419995 and
        // https://www.openstreetmap.org/way/460419994
        final Atlas slicedRawAtlas = this.setup.getRingWithSingleIntersectionAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals(
                "Three edges - one for the one-way ring and two for the bi-directional edge leading to it",
                3, finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes - start/end of the edge leading up to the ring", 2,
                finalAtlas.numberOfNodes());
    }

    @Test
    public void testRoundAbout()
    {
        // Based on https://www.openstreetmap.org/way/426558829,
        // https://www.openstreetmap.org/way/426558827 and
        // https://www.openstreetmap.org/way/426558831
        final Atlas slicedRawAtlas = this.setup.getRoundAboutAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals(
                "The roundabout gets split into two edges, each of the respective roads leading into and out of it becomes an edge and a reverse edge",
                6, finalAtlas.numberOfEdges());
        Assert.assertEquals(
                "Four nodes - start/end of the two roads leading up to and away from the roundabout",
                4, finalAtlas.numberOfNodes());
    }

    @Test
    public void testSectioningAtBarrier()
    {
        // Based on https://www.openstreetmap.org/way/460419996 - has two barrier, one in the middle
        // and one at the end node
        final Atlas slicedRawAtlas = this.setup.getLineWithBarrierAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Two edges, with reverse counterparts", 4, finalAtlas.numberOfEdges());
        Assert.assertEquals("Three nodes, one in the middle at the barrier", 3,
                finalAtlas.numberOfNodes());
        Assert.assertEquals("Make sure that the two barriers are also represented as Points", 2,
                finalAtlas.numberOfPoints());
    }

    @Test
    public void testSimpleBiDirectionalLine()
    {
        // Based on https://www.openstreetmap.org/way/460834514
        final Atlas slicedRawAtlas = this.setup.getSimpleBiDirectionalLineAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("single edge and its counter part", 2, finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes - one at the start and end", 2, finalAtlas.numberOfNodes());
    }
}
