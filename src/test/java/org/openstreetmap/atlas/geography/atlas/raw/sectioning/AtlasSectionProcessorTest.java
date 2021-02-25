package org.openstreetmap.atlas.geography.atlas.raw.sectioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasSlicerTest;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.BarrierTag;
import org.openstreetmap.atlas.tags.SyntheticInvalidWaySectionTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * {@link AtlasSectionProcessor} unit tests
 *
 * @author mgostintsev
 */
public class AtlasSectionProcessorTest
{
    private static final CountryBoundaryMap COUNTRY_BOUNDARY_MAP;

    static
    {
        COUNTRY_BOUNDARY_MAP = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> RawAtlasSlicerTest.class
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
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Two edges, each having a reverse counterpart", 4,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());

        // Explicit check for expected identifiers
        Assert.assertNotNull(finalAtlas.edge(317579533000001L));
        Assert.assertNotNull(finalAtlas.edge(-317579533000001L));
        Assert.assertNotNull(finalAtlas.edge(317579533000002L));
        Assert.assertNotNull(finalAtlas.edge(-317579533000002L));
    }

    @Test
    public void testCutAtShardBoundary()
    {
        final Atlas slicedRawAtlas = this.setup.getRawAtlasSpanningOutsideBoundary();
        final Atlas finalAtlas = new AtlasSectionProcessor(SlippyTile.forName("8-123-123"),
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
    public void testLineWithLessThanTwoNodesDueToRepeatedLocationAtEndOfLine()
    {
        // Based on a prior version of https://www.openstreetmap.org/way/488453376
        final Atlas slicedRawAtlas = this.setup
                .getLineWithLessThanTwoNodesDueToRepeatedLocationAtEndOfLineAtlas();
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertNotNull("Line has been converted to an Edge",
                finalAtlas.edge(488453376000000L));
        Assert.assertNull("Line has not been converted to an Edge",
                finalAtlas.line(488453376000000L));
        final Map<String, String> originalTags = slicedRawAtlas.line(488453376000000L).getTags();
        final Map<String, String> finalTags = finalAtlas.edge(488453376000000L).getTags();
        Assert.assertEquals("Edge has no other tag changes", originalTags, finalTags);
    }

    @Test
    public void testLineWithLoopAtEnd()
    {
        // Based on https://www.openstreetmap.org/way/461101743
        final Atlas slicedRawAtlas = this.setup.getLineWithLoopAtEndAtlas();
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Two edges, each having a reverse counterpart", 4,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        Assert.assertEquals(4, finalAtlas.node(4566499618000000L).connectedEdges().size());
        Assert.assertEquals(2, finalAtlas.node(4566499619000000L).connectedEdges().size());

        // Explicit check for expected identifiers
        Assert.assertNotNull(finalAtlas.edge(461101743000001L));
        Assert.assertNotNull(finalAtlas.edge(-461101743000001L));
        Assert.assertNotNull(finalAtlas.edge(461101743000002L));
        Assert.assertNotNull(finalAtlas.edge(-461101743000002L));
    }

    @Test
    public void testLineWithLoopAtStart()
    {
        // Based on https://www.openstreetmap.org/way/460419987
        final Atlas slicedRawAtlas = this.setup.getLineWithLoopAtStartAtlas();
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Three edges, each having a reverse counterpart", 6,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Three nodes", 3, finalAtlas.numberOfNodes());
        Assert.assertEquals(4, finalAtlas.node(4560902689000000L).connectedEdges().size());
        Assert.assertEquals(2, finalAtlas.node(4560902695000000L).connectedEdges().size());
        Assert.assertEquals(4, finalAtlas.node(4560902693000000L).connectedEdges().size());

        // Explicit check for expected identifiers
        Assert.assertNotNull(finalAtlas.edge(460419987000001L));
        Assert.assertNotNull(finalAtlas.edge(-460419987000001L));
        Assert.assertNotNull(finalAtlas.edge(460419987000002L));
        Assert.assertNotNull(finalAtlas.edge(-460419987000002L));
        Assert.assertNotNull(finalAtlas.edge(460419987000003L));
        Assert.assertNotNull(finalAtlas.edge(-460419987000003L));
    }

    @Test
    public void testLineWithLoopInMiddle()
    {
        // Loosely based on https://www.openstreetmap.org/way/460419987 - with a new added node
        final Atlas slicedRawAtlas = this.setup.getLineWithLoopInMiddleAtlas();

        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Four edges, each having a reverse counterpart", 8,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Four nodes", 4, finalAtlas.numberOfNodes());
        Assert.assertEquals(2, finalAtlas.node(4560902695000000L).connectedEdges().size());
        Assert.assertEquals(4, finalAtlas.node(4560902693000000L).connectedEdges().size());
        Assert.assertEquals(2, finalAtlas.node(4560902612000000L).connectedEdges().size());
        Assert.assertEquals(6, finalAtlas.node(4560902689000000L).connectedEdges().size());

        // Explicit check for expected identifiers
        Assert.assertNotNull(finalAtlas.edge(460419987000001L));
        Assert.assertNotNull(finalAtlas.edge(-460419987000001L));
        Assert.assertNotNull(finalAtlas.edge(460419987000002L));
        Assert.assertNotNull(finalAtlas.edge(-460419987000003L));
        Assert.assertNotNull(finalAtlas.edge(460419987000003L));
        Assert.assertNotNull(finalAtlas.edge(-460419987000003L));
        Assert.assertNotNull(finalAtlas.edge(460419987000004L));
        Assert.assertNotNull(finalAtlas.edge(-460419987000004L));
    }

    @Test
    public void testLineWithRepeatedLocation()
    {
        // Based on a prior version of https://www.openstreetmap.org/way/488453376
        final Atlas slicedRawAtlas = this.setup.getLineWithRepeatedLocationAtlas();
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Four edges, each having a reverse counterpart", 4,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        Assert.assertEquals("No points", 0, finalAtlas.numberOfPoints());
        Assert.assertEquals("This way got sectioned 4 times, with reverse edges", 4,
                Iterables.size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 488453376L)));
    }

    @Test
    public void testLineWithRepeatedLocationKeepAll()
    {
        // Based on a prior version of https://www.openstreetmap.org/way/488453376
        final Atlas slicedRawAtlas = this.setup.getLineWithRepeatedLocationAtlas();
        // setKeepAll is not set in createOptionWithAllEnabled since it may break downstream users.
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas, AtlasLoadingOption
                .createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP).setKeepAll(true)).run();

        Assert.assertEquals("Four edges, each having a reverse counterpart", 4,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        Assert.assertEquals("Eight points", 8, finalAtlas.numberOfPoints());
        Assert.assertEquals("This way got sectioned 4 times, with reverse edges", 4,
                Iterables.size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 488453376L)));
    }

    @Test
    public void testLoopWithIntersection()
    {
        // Based on https://www.openstreetmap.org/way/310540517 and partial excerpt of
        // https://www.openstreetmap.org/way/310540519
        final Atlas slicedRawAtlas = this.setup.getLoopingWayWithIntersectionAtlas();
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Four edges, each having a reverse counterpart", 8,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Four nodes", 4, finalAtlas.numberOfNodes());
        Assert.assertEquals("This way got sectioned 3 times, with reverse edges", 6,
                Iterables.size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 310540517L)));
        Assert.assertEquals("This edge got sectioned once, with reverse edges", 2,
                Iterables.size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 310540519L)));
    }

    @Test
    public void testLoopWithRepeatedLocation()
    {
        // Based on a prior version of https://www.openstreetmap.org/way/488453376 with a piece of
        // https://www.openstreetmap.org/way/386313688
        final Atlas slicedRawAtlas = this.setup.getLoopWithRepeatedLocationAtlas();
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Four edges, each having a reverse counterpart", 4,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        Assert.assertEquals("This way got sectioned once, with a reverse edge", 2,
                Iterables.size(finalAtlas.edges(edge -> edge.getOsmIdentifier() == 488453376L)));
    }

    @Test
    public void testMalformedPolyLine()
    {
        // Based on a prior version of https://www.openstreetmap.org/way/621043891
        final Atlas slicedRawAtlas = this.setup.getMalformedPolyLineAtlas();
        final CountryBoundaryMap boundaryMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> AtlasSectionProcessorTest.class
                        .getResourceAsStream("malformedPolyLineBoundaryMap.txt")));
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(boundaryMap)).run();

        Assert.assertEquals("Six edges, each having a reverse counterpart", 12,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Four nodes", 4, finalAtlas.numberOfNodes());
    }

    @Test
    public void testOneWayRing()
    {
        // Based on https://www.openstreetmap.org/way/460257372
        final Atlas slicedRawAtlas = this.setup.getOneWayRingAtlas();
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
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
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("One edge", 1, finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        finalAtlas.edges().forEach(
                edge -> Assert.assertFalse("No edge has a reverse edge", edge.hasReverseEdge()));
    }

    @Test
    public void testPedestrianRing()
    {
        // Based on https://www.openstreetmap.org/way/460257372
        final Atlas slicedRawAtlas = this.setup.getPedestrianRingAtlas();
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Two edges, the ring got sectioned in the middle", 2,
                finalAtlas.numberOfEdges());
        Assert.assertEquals("Two nodes", 2, finalAtlas.numberOfNodes());
        finalAtlas.edges().forEach(
                edge -> Assert.assertFalse("No edge has a reverse edge", edge.hasReverseEdge()));
    }

    @Test
    public void testRelationMemberLocationItemInclusion()
    {
        // Based on https://www.openstreetmap.org/relation/578254 - the Node in the Relation gets
        // created as both an Atlas point and node. Let's verify that the Relation consists of both.
        final Atlas slicedRawAtlas = this.setup.getNodeAndPointAsRelationMemberAtlas();

        final CountryBoundaryMap boundaryMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> AtlasSectionProcessorTest.class
                        .getResourceAsStream("nodeAndPointRelationMemberBoundaryMap.txt")));
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(boundaryMap)).run();

        final RelationMemberList members = finalAtlas.relation(578254000000L).members();
        Assert.assertEquals("Six members - 2 pairs of reverse edges, 1 node and 1 point", 6,
                members.size());
        Assert.assertEquals("Single point", 1, members.stream()
                .filter(member -> member.getEntity().getType() == ItemType.POINT).count());
        Assert.assertEquals("Single node", 1, members.stream()
                .filter(member -> member.getEntity().getType() == ItemType.NODE).count());
    }

    @Test
    public void testReversedOneWayLine()
    {
        // Based on https://www.openstreetmap.org/way/333112568
        final Atlas slicedRawAtlas = this.setup.getReversedOneWayLineAtlas();
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
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
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
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
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
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
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals("Two edges, with reverse counterparts", 4, finalAtlas.numberOfEdges());
        Assert.assertEquals("Three nodes, one in the middle at the barrier", 3,
                finalAtlas.numberOfNodes());
        Assert.assertEquals("Make sure that the two barriers are also represented as Points", 2,
                Iterables.size(finalAtlas.points(point -> BarrierTag.isBarrier(point))));
    }

    @Test
    public void testSelfIntersectingLoop()
    {
        // Based on https://www.openstreetmap.org/way/373705334 and surrounding edge network
        final Atlas slicedRawAtlas = this.setup.getSelfIntersectingLoopAtlas();
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
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
        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
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
        final Map<String, List<org.locationtech.jts.geom.Polygon>> boundaries = new HashMap<>();
        final Polygon fakePolygon = new Polygon(Location.forString("34.15102284294,66.22764518738"),
                Location.forString("34.1515910819,66.53388908386"),
                Location.forString("33.99802783162,66.53045585632"),
                Location.forString("33.99632001003,66.22558525085"),
                Location.forString("34.15102284294,66.22764518738"));
        final List<org.locationtech.jts.geom.Polygon> fakeBoundaries = new ArrayList<>();
        fakeBoundaries.add(new JtsPolygonConverter().convert(fakePolygon));
        boundaries.put(afghanistan, fakeBoundaries);
        final CountryBoundaryMap countryBoundaryMap = CountryBoundaryMap
                .fromBoundaryMap(boundaries);

        final Atlas finalAtlas = new AtlasSectionProcessor(slicedRawAtlas,
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
