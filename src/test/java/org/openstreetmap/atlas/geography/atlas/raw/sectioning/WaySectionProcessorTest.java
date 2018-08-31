package org.openstreetmap.atlas.geography.atlas.raw.sectioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.LineAndPointSlicingTest;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.SyntheticInvalidWaySectionTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;

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
        COUNTRY_BOUNDARY_MAP = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> LineAndPointSlicingTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries_with_grid_index.txt.gz"))
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

        // Explicit check for expected identifiers
        Assert.assertTrue(finalAtlas.edge(317579533000001L) != null);
        Assert.assertTrue(finalAtlas.edge(-317579533000001L) != null);
        Assert.assertTrue(finalAtlas.edge(317579533000002L) != null);
        Assert.assertTrue(finalAtlas.edge(-317579533000002L) != null);
    }

    @Test
    public void testCutAtShardBoundary()
    {
        final Atlas slicedRawAtlas = this.setup.getRawAtlasSpanningOutsideBoundary();
        final Atlas finalAtlas = new WaySectionProcessor(SlippyTile.forName("8-123-123"),
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP),
                new SlippyTileSharding(8), shard -> Optional.of(slicedRawAtlas)).run();

        // Assert the number to make sure the edges outside the shard have been excluded
        Assert.assertEquals(4, finalAtlas.numberOfNodes());
        Assert.assertEquals(3, finalAtlas.numberOfEdges());

        // Nodes
        Assert.assertNotNull(finalAtlas.node(112428000000L));
        Assert.assertNotNull(finalAtlas.node(112430000000L));
        Assert.assertNotNull(finalAtlas.node(112441000000L));
        Assert.assertNotNull(finalAtlas.node(112427000000L));

        // Edges
        Assert.assertNotNull(finalAtlas.edge(112429000001L));
        Assert.assertNotNull(finalAtlas.edge(112429000002L));
        Assert.assertNotNull(finalAtlas.edge(112440000001L));
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

        // Explicit check for expected identifiers
        Assert.assertTrue(finalAtlas.edge(461101743000001L) != null);
        Assert.assertTrue(finalAtlas.edge(-461101743000001L) != null);
        Assert.assertTrue(finalAtlas.edge(461101743000002L) != null);
        Assert.assertTrue(finalAtlas.edge(-461101743000002L) != null);
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

        // Explicit check for expected identifiers
        Assert.assertTrue(finalAtlas.edge(460419987000001L) != null);
        Assert.assertTrue(finalAtlas.edge(-460419987000001L) != null);
        Assert.assertTrue(finalAtlas.edge(460419987000002L) != null);
        Assert.assertTrue(finalAtlas.edge(-460419987000002L) != null);
        Assert.assertTrue(finalAtlas.edge(460419987000003L) != null);
        Assert.assertTrue(finalAtlas.edge(-460419987000003L) != null);
    }

    @Test
    public void testLineWithLoopInMiddle()
    {
        // Loosely based on https://www.openstreetmap.org/way/460419987 - with a new added node
        final Atlas slicedRawAtlas = this.setup.getLineWithLoopInMiddleAtlas();

        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Four edges, each having a reverse counterpart", 8,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Four nodes", 4, finalAtlas.numberOfNodes());
        Assert.assertEquals(2, finalAtlas.node(4560902695000000L).connectedEdges().size());
        Assert.assertEquals(4, finalAtlas.node(4560902693000000L).connectedEdges().size());
        Assert.assertEquals(2, finalAtlas.node(4560902612000000L).connectedEdges().size());
        Assert.assertEquals(6, finalAtlas.node(4560902689000000L).connectedEdges().size());

        // Explicit check for expected identifiers
        Assert.assertTrue(finalAtlas.edge(460419987000001L) != null);
        Assert.assertTrue(finalAtlas.edge(-460419987000001L) != null);
        Assert.assertTrue(finalAtlas.edge(460419987000002L) != null);
        Assert.assertTrue(finalAtlas.edge(-460419987000003L) != null);
        Assert.assertTrue(finalAtlas.edge(460419987000003L) != null);
        Assert.assertTrue(finalAtlas.edge(-460419987000003L) != null);
        Assert.assertTrue(finalAtlas.edge(460419987000004L) != null);
        Assert.assertTrue(finalAtlas.edge(-460419987000004L) != null);
    }

    @Test
    public void testLineWithRepeatedLocation()
    {
        // Based on a prior version of https://www.openstreetmap.org/way/488453376
        final Atlas slicedRawAtlas = this.setup.getLineWithRepeatedLocationAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Four edges, each having a reverse counterpart", 4,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        Assert.assertTrue("This way got sectioned 4 times, with reverse edges", Iterables
                .size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 488453376L)) == 4);
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
    public void testLoopWithRepeatedLocation()
    {
        // Based on a prior version of https://www.openstreetmap.org/way/488453376 with a piece of
        // https://www.openstreetmap.org/way/386313688
        final Atlas slicedRawAtlas = this.setup.getLoopWithRepeatedLocationAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Four edges, each having a reverse counterpart", 4,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        Assert.assertTrue("This way got sectioned once, with a reverse edge", Iterables
                .size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 488453376L)) == 2);
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
                finalAtlas.edge(333112568000000L).asPolyLine().reversed());
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
    public void testSelfIntersectingLoop()
    {
        // Based on https://www.openstreetmap.org/way/373705334 and surrounding edge network
        final Atlas slicedRawAtlas = this.setup.getSelfIntersectingLoopAtlas();
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Ten edges, each having a reverse counterpart", 20,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Nine nodes", 9, finalAtlas.numberOfNodes());
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

    @Test
    public void testWayExceedingSectioningLimit()
    {
        // Based on https://www.openstreetmap.org/way/608903805 and
        // https://www.openstreetmap.org/way/608901269. These are stacked, duplicated ways that
        // extend for a long time, causing sectioning to occur more than the allowed 999 times
        final Atlas slicedRawAtlas = this.setup.getWayExceedingSectioningLimitAtlas();

        // Create a dummy country boundary map that contains these ways and call it Afghanistan
        final Set<String> countries = new HashSet<>();
        final String afghanistan = "AFG";
        countries.add(afghanistan);
        final Map<String, MultiPolygon> boundaries = new HashMap<>();
        final Polygon fakePolygon = new Polygon(Location.forString("34.15102284294,66.22764518738"),
                Location.forString("34.1515910819,66.53388908386"),
                Location.forString("33.99802783162,66.53045585632"),
                Location.forString("33.99632001003,66.22558525085"),
                Location.forString("34.15102284294,66.22764518738"));
        final MultiPolygon boundary = MultiPolygon.forPolygon(fakePolygon);
        boundaries.put(afghanistan, boundary);
        final CountryBoundaryMap countryBoundaryMap = CountryBoundaryMap
                .fromBoundaryMap(boundaries);

        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(countryBoundaryMap)).run();

        // Verify maximum number of sections for each edge
        Assert.assertEquals(999,
                Iterables.size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 608901269)));
        Assert.assertEquals(999,
                Iterables.size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 608903805)));

        // Verify tag presence
        Assert.assertEquals(SyntheticInvalidWaySectionTag.YES.name(),
                finalAtlas.edge(608901269000999L).tag(SyntheticInvalidWaySectionTag.KEY));
        Assert.assertEquals(SyntheticInvalidWaySectionTag.YES.name(),
                finalAtlas.edge(608903805000999L).tag(SyntheticInvalidWaySectionTag.KEY));
    }
}
