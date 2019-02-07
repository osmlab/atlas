package org.openstreetmap.atlas.geography.atlas.raw;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.ShardFileOverlapsPolygonTest;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator;
import org.openstreetmap.atlas.geography.atlas.raw.sectioning.WaySectionProcessor;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasCountrySlicer;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.sharding.DynamicTileSharding;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for creating, slicing and sectioning with the raw Atlas ingest flow.
 *
 * @author mgostintsev
 */
public class RawAtlasIntegrationTest
{
    private static CountryBoundaryMap COUNTRY_BOUNDARY_MAP;
    private static Set<String> COUNTRIES;

    private static final long LINE_OSM_IDENTIFIER_CROSSING_3_SHARDS = 541706;

    private static final Logger logger = LoggerFactory.getLogger(RawAtlasIntegrationTest.class);

    static
    {
        COUNTRIES = new HashSet<>();
        COUNTRIES.add("CIV");
        COUNTRIES.add("GIN");
        COUNTRIES.add("LBR");

        COUNTRY_BOUNDARY_MAP = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> RawAtlasIntegrationTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
    }

    @Rule
    public DynamicRawAtlasSectioningTestRule setup = new DynamicRawAtlasSectioningTestRule();

    @Test
    public void testPbfToSlicedAtlasWithExpansion()
    {
        // Create a simple store, populated with 3 shards and the corresponding atlases
        final Map<Shard, Atlas> store = prepareShardStore();
        final Function<Shard, Optional<Atlas>> rawAtlasFetcher = shard ->
        {
            if (store.containsKey(shard))
            {
                return Optional.of(store.get(shard));
            }
            else
            {
                return Optional.empty();
            }
        };

        // Create 3 atlas files, starting from each of the different shards
        final Atlas atlasFromz8x123y122 = generateSectionedAtlasStartingAtShard(
                new SlippyTile(123, 122, 8), rawAtlasFetcher);
        logger.info(atlasFromz8x123y122.summary());

        final Atlas atlasFromz8x123y123 = generateSectionedAtlasStartingAtShard(
                new SlippyTile(123, 123, 8), rawAtlasFetcher);
        logger.info(atlasFromz8x123y123.summary());

        final Atlas atlasFromz7x62y61 = generateSectionedAtlasStartingAtShard(
                new SlippyTile(62, 61, 7), rawAtlasFetcher);
        logger.info(atlasFromz7x62y61.summary());

        // Let's focus on the edge spanning all 3 shards and verify it got sectioned properly
        final Iterable<Edge> firstGroupOfEdges = atlasFromz8x123y122
                .edges(edge -> edge.getOsmIdentifier() == LINE_OSM_IDENTIFIER_CROSSING_3_SHARDS);
        final Iterable<Edge> secondGroupOfEdges = atlasFromz8x123y123
                .edges(edge -> edge.getOsmIdentifier() == LINE_OSM_IDENTIFIER_CROSSING_3_SHARDS);
        final Iterable<Edge> thirdGroupOfEdges = atlasFromz7x62y61
                .edges(edge -> edge.getOsmIdentifier() == LINE_OSM_IDENTIFIER_CROSSING_3_SHARDS);

        // First look at absolute counts. Each shard will have two forward and reverse edges
        Assert.assertEquals(4, Iterables.size(firstGroupOfEdges));
        Assert.assertEquals(4, Iterables.size(secondGroupOfEdges));
        Assert.assertEquals(4, Iterables.size(thirdGroupOfEdges));

        // Next, let's check identifier consistency
        final Set<Long> uniqueIdentifiers = new HashSet<>();
        Iterables.stream(firstGroupOfEdges)
                .forEach(edge -> uniqueIdentifiers.add(edge.getIdentifier()));
        Iterables.stream(secondGroupOfEdges)
                .forEach(edge -> uniqueIdentifiers.add(edge.getIdentifier()));
        Iterables.stream(thirdGroupOfEdges)
                .forEach(edge -> uniqueIdentifiers.add(edge.getIdentifier()));

        // There should be 4 pieces (each having a forward and reverse edge) total
        Assert.assertTrue(uniqueIdentifiers.size() == 8);

        // Validate the same edge identifiers built from different shards to test equality
        final Edge piece2from122 = atlasFromz8x123y122.edge(541706001002L);
        final Edge piece2from123 = atlasFromz8x123y123.edge(541706001002L);
        Assert.assertTrue(piece2from122.asPolyLine().equals(piece2from123.asPolyLine()));

        final Edge piece3from123 = atlasFromz8x123y123.edge(541706001003L);
        final Edge piece3from62 = atlasFromz7x62y61.edge(541706001003L);
        Assert.assertTrue(piece3from123.asPolyLine().equals(piece3from62.asPolyLine()));

        // Let's validate absolute number of edges in each shard
        Assert.assertEquals(12, atlasFromz8x123y122.numberOfEdges());
        Assert.assertEquals(16, atlasFromz8x123y123.numberOfEdges());
        Assert.assertEquals(20, atlasFromz7x62y61.numberOfEdges());
    }

    @Test
    public void testPbfToSlicedRawAtlas()
    {
        // This PBF file contains really interesting data. 1. MultiPolygon with multiple inners and
        // outers spanning 3 countries (http://www.openstreetmap.org/relation/3638082) 2. Multiple
        // nested relations (http://www.openstreetmap.org/relation/3314886)
        final String pbfPath = RawAtlasIntegrationTest.class
                .getResource("8-122-122-trimmed.osm.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(pbfPath));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        Assert.assertEquals(0, rawAtlas.numberOfNodes());
        Assert.assertEquals(0, rawAtlas.numberOfEdges());
        Assert.assertEquals(0, rawAtlas.numberOfAreas());
        Assert.assertEquals(74342, rawAtlas.numberOfPoints());

        Assert.assertEquals(7818, rawAtlas.numberOfLines());
        Assert.assertEquals(17, rawAtlas.numberOfRelations());

        final Atlas slicedRawAtlas = sliceRawAtlas(rawAtlas, COUNTRIES);

        Assert.assertEquals(0, slicedRawAtlas.numberOfNodes());
        Assert.assertEquals(0, slicedRawAtlas.numberOfEdges());
        Assert.assertEquals(0, slicedRawAtlas.numberOfAreas());
        Assert.assertEquals(74850, slicedRawAtlas.numberOfPoints());
        Assert.assertEquals(8087, slicedRawAtlas.numberOfLines());
        Assert.assertEquals(23, slicedRawAtlas.numberOfRelations());

        // Assert all raw Atlas entities have a country code
        assertAllEntitiesHaveCountryCode(slicedRawAtlas);

        // Try only with Ivory Coast now!
        final Set<String> onlyIvoryCoast = new HashSet<>();
        onlyIvoryCoast.add("CIV");
        final Atlas ivoryCoast = sliceRawAtlas(rawAtlas, onlyIvoryCoast);

        Assert.assertEquals(0, ivoryCoast.numberOfNodes());
        Assert.assertEquals(0, ivoryCoast.numberOfEdges());
        Assert.assertEquals(0, ivoryCoast.numberOfAreas());
        Assert.assertEquals(34960, ivoryCoast.numberOfPoints());
        Assert.assertEquals(3637, ivoryCoast.numberOfLines());
        Assert.assertEquals(12, ivoryCoast.numberOfRelations());

        // Assert all raw Atlas entities have a country code
        assertAllEntitiesHaveCountryCode(ivoryCoast);

        // Test sectioning!
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals(5011, finalAtlas.numberOfNodes());
        Assert.assertEquals(9764, finalAtlas.numberOfEdges());
        Assert.assertEquals(5133, finalAtlas.numberOfAreas());
        Assert.assertEquals(184, finalAtlas.numberOfPoints());
        Assert.assertEquals(326, finalAtlas.numberOfLines());
        Assert.assertEquals(23, finalAtlas.numberOfRelations());
    }

    @Test
    public void testSectioningFromShard()
    {
        final String path = RawAtlasIntegrationTest.class.getResource("8-122-122-trimmed.osm.pbf")
                .getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas rawAtlas = rawAtlasGenerator.build();
        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(COUNTRIES, COUNTRY_BOUNDARY_MAP)
                .slice(rawAtlas);

        // Simple fetcher that returns the atlas from above for the corresponding shard
        final Map<Shard, Atlas> store = new HashMap<>();
        store.put(new SlippyTile(122, 122, 8), slicedRawAtlas);
        final Function<Shard, Optional<Atlas>> rawAtlasFetcher = shard ->
        {
            if (store.containsKey(shard))
            {
                return Optional.of(store.get(shard));
            }
            else
            {
                return Optional.empty();
            }
        };

        final Atlas finalAtlas = new WaySectionProcessor(new SlippyTile(122, 122, 8),
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP),
                new DynamicTileSharding(new File(ShardFileOverlapsPolygonTest.class
                        .getResource(
                                "/org/openstreetmap/atlas/geography/boundary/tree-6-14-100000.txt.gz")
                        .getFile())),
                rawAtlasFetcher).run();

        Assert.assertEquals(5009, finalAtlas.numberOfNodes());
        Assert.assertEquals(9760, finalAtlas.numberOfEdges());
        Assert.assertEquals(5128, finalAtlas.numberOfAreas());
        Assert.assertEquals(184, finalAtlas.numberOfPoints());
        Assert.assertEquals(271, finalAtlas.numberOfLines());
        Assert.assertEquals(23, finalAtlas.numberOfRelations());
    }

    @Test
    public void testStandAloneNodeIngest()
    {
        // This is an OSM node that doesn't have any tags, is not a member of a relation or part of
        // a way. It should end up as a point in the final atlas.
        final String antarctica = "ATA";

        // Create a fake boundary as a bounding box around the target point
        final Map<String, MultiPolygon> boundaries = new HashMap<>();
        final Location targetPoint = Location.forString("-81.2022146, 51.6408578");
        final MultiPolygon antarcticaBoundary = MultiPolygon
                .forPolygon(targetPoint.boxAround(Distance.meters(1)));
        boundaries.put(antarctica, antarcticaBoundary);

        // Create a country boundary map with the fake Antarctica country boundary
        final CountryBoundaryMap countryBoundaryMap = CountryBoundaryMap
                .fromBoundaryMap(boundaries);

        // Create a raw atlas, slice and section it
        final String pbfPath = RawAtlasIntegrationTest.class.getResource("node-4353689487.pbf")
                .getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(pbfPath));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(antarctica, countryBoundaryMap)
                .slice(rawAtlas);
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(countryBoundaryMap)).run();

        // Verify only a single point exists
        Assert.assertEquals(0, finalAtlas.numberOfNodes());
        Assert.assertEquals(0, finalAtlas.numberOfEdges());
        Assert.assertEquals(0, finalAtlas.numberOfAreas());
        Assert.assertEquals(1, finalAtlas.numberOfPoints());
        Assert.assertEquals(0, finalAtlas.numberOfLines());
        Assert.assertEquals(0, finalAtlas.numberOfRelations());
    }

    @Test
    public void testTwoWaysWithDifferentLayersIntersectingAtEnd()
    {
        // Based on https://www.openstreetmap.org/way/26071941 and
        // https://www.openstreetmap.org/way/405246856 having two different layer tag values and
        // having a shared node (https://www.openstreetmap.org/node/281526976) at which one of the
        // ways ends. This is a fairly common OSM use-case, where two roads (often ramps or links)
        // having different layer tags should be connected.
        final Location intersection = Location.forString("55.0480165, 82.9406646");
        final String path = RawAtlasIntegrationTest.class
                .getResource("twoWaysWithDifferentLayersIntersectingAtEnd.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        // Prepare the boundary
        final CountryBoundaryMap boundaryMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> RawAtlasIntegrationTest.class
                        .getResourceAsStream("layerIntersectionAtEndBoundaryMap.txt")));

        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer("RUS", boundaryMap).slice(rawAtlas);
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(boundaryMap)).run();

        // Make sure there are exactly three edges created. Both ways are one-way and one of them
        // gets way-sectioned into two edges.
        Assert.assertEquals(3, finalAtlas.numberOfEdges());

        // Make sure there are exactly 4 nodes
        Assert.assertEquals(4, finalAtlas.numberOfNodes());

        // Explicitly check for a single node at the intersection location
        Assert.assertEquals(1, Iterables.size(finalAtlas.nodesAt(intersection)));

        // Explicitly check that the layer=0 link is connected to both the layer=-1 trunk edges
        Assert.assertEquals(2, finalAtlas.edge(26071941000000L).connectedEdges().size());
    }

    @Test
    public void testTwoWaysWithDifferentLayersIntersectingAtStart()
    {
        // Based on https://www.openstreetmap.org/way/551411163 and partial piece of
        // https://www.openstreetmap.org/way/67803311 having two different layer tag values and
        // having a shared node (https://www.openstreetmap.org/node/5325270497) at which one of the
        // ways ends. This is a fairly common OSM use-case, where two roads (often ramps or links)
        // having different layer tags should be connected. In this case, we also check that the
        // trunk link is connected to the trunk at both the start and end nodes.
        final Location intersection = Location.forString("52.4819691, 38.7603042");
        final String path = RawAtlasIntegrationTest.class
                .getResource("twoWaysWithDifferentLayersIntersectingAtStart.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        // Prepare the boundary
        final CountryBoundaryMap boundaryMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> RawAtlasIntegrationTest.class
                        .getResourceAsStream("layerIntersectionAtStartBoundaryMap.txt")));

        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer("RUS", boundaryMap).slice(rawAtlas);
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(boundaryMap)).run();

        // Make sure there are exactly six edges created. The trunk link (551411163) is
        // way-sectioned into 2 pieces - at an intermediate crossing, while the trunk (67803311) is
        // sectioned into 4 pieces - once at the start of the link, once at an intermediate crossing
        // and again at the end of the link.
        Assert.assertEquals(6, finalAtlas.numberOfEdges());

        // Make sure there are exactly 6 nodes
        Assert.assertEquals(6, finalAtlas.numberOfNodes());

        // Explicitly check for a single node at the intersection location
        Assert.assertEquals(1, Iterables.size(finalAtlas.nodesAt(intersection)));

        // Explicitly check that the layer=0 link is connected to both the layer=1 trunk edges and
        // its own sectioned edge
        Assert.assertEquals(3, finalAtlas.edge(551411163000001L).connectedEdges().size());
        Assert.assertEquals(3, finalAtlas.edge(551411163000002L).connectedEdges().size());
    }

    @Test
    public void testTwoWaysWithDifferentLayersIntersectingInMiddle()
    {
        // Based on https://www.openstreetmap.org/way/467880095 and
        // https://www.openstreetmap.org/way/28247094 having two different layer tag values and
        // having overlapping nodes (https://www.openstreetmap.org/node/4661272336 and
        // https://www.openstreetmap.org/node/5501637097) that should not be merged.
        final Location overlappingLocation = Location.forString("1.3248985,103.6452864");
        final String path = RawAtlasIntegrationTest.class
                .getResource("twoWaysWithDifferentLayersIntersectingInMiddle.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        // Verify both points made it into the raw atlas
        Assert.assertTrue(Iterables.size(rawAtlas.pointsAt(overlappingLocation)) == 2);

        // Prepare the boundary
        final CountryBoundaryMap boundaryMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> RawAtlasIntegrationTest.class
                        .getResourceAsStream("layerIntersectionInMiddleBoundaryMap.txt")));

        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer("SGP", boundaryMap).slice(rawAtlas);
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(boundaryMap)).run();

        // Make sure there is no sectioning happening between the two ways with different layer tag
        // values. There is a one-way overpass and a bi-directional residential street, resulting in
        // 3 total edges and 4 nodes (one on both ends of the two segments)
        Assert.assertEquals(3, finalAtlas.numberOfEdges());
        Assert.assertEquals(4, finalAtlas.numberOfNodes());

        // Again, verify there is no node at the duplicated location
        Assert.assertTrue(Iterables.size(finalAtlas.nodesAt(overlappingLocation)) == 0);
        Assert.assertEquals(0, finalAtlas.numberOfPoints());
    }

    private void assertAllEntitiesHaveCountryCode(final Atlas atlas)
    {
        atlas.lines().forEach(line ->
        {
            Assert.assertTrue(Validators.hasValuesFor(line, ISOCountryTag.class));
        });
        atlas.points().forEach(point ->
        {
            Assert.assertTrue(Validators.hasValuesFor(point, ISOCountryTag.class));
        });
        atlas.relations().forEach(point ->
        {
            Assert.assertTrue(Validators.hasValuesFor(point, ISOCountryTag.class));
        });
    }

    private Atlas generateSectionedAtlasStartingAtShard(final Shard shard,
            final Function<Shard, Optional<Atlas>> rawAtlasFetcher)
    {
        return new WaySectionProcessor(shard,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP),
                new DynamicTileSharding(new File(ShardFileOverlapsPolygonTest.class
                        .getResource(
                                "/org/openstreetmap/atlas/geography/boundary/tree-6-14-100000.txt.gz")
                        .getFile())),
                rawAtlasFetcher).run();
    }

    private Map<Shard, Atlas> prepareShardStore()
    {
        final Map<Shard, Atlas> store = new HashMap<>();
        store.put(new SlippyTile(62, 61, 7), this.setup.getAtlasz7x62y61());
        store.put(new SlippyTile(123, 123, 8), this.setup.getAtlasz8x123y123());
        store.put(new SlippyTile(123, 122, 8), this.setup.getAtlasz8x123y122());
        return store;
    }

    private Atlas sliceRawAtlas(final Atlas rawAtlas, final Set<String> countries)
    {
        return new RawAtlasCountrySlicer(countries, COUNTRY_BOUNDARY_MAP).slice(rawAtlas);
    }
}
