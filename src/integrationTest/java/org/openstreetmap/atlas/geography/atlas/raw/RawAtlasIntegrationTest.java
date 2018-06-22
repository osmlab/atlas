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
 * Integration tests for creating raw atlases, slicing raw atlases and sectioning sliced raw
 * atlases.
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
    public void testOverlappingNodesWithUniqueLayerTags()
    {
        // Based on https://www.openstreetmap.org/way/467880095 and
        // https://www.openstreetmap.org/way/28247094 having two different layer tag values and
        // having overlapping nodes (https://www.openstreetmap.org/node/4661272336 and
        // https://www.openstreetmap.org/node/5501637097) that should not be merged.
        final Location overlappingLocation = Location.forString("1.3248985,103.6452864");
        final String path = RawAtlasIntegrationTest.class.getResource("layerTagTestCase.pbf")
                .getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        // Verify both points made it into the raw atlas
        Assert.assertTrue(Iterables.size(rawAtlas.pointsAt(overlappingLocation)) == 2);

        // Prepare the country and boundary
        final Set<String> singaporeCountry = new HashSet<>();
        singaporeCountry.add("SGP");
        final CountryBoundaryMap boundaryMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> RawAtlasIntegrationTest.class
                        .getResourceAsStream("testNodesWithDifferentLayerTagsBoundaryMap.txt")));

        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(singaporeCountry, boundaryMap)
                .slice(rawAtlas);
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
        Assert.assertTrue(Iterables.size(firstGroupOfEdges) == 4);
        Assert.assertTrue(Iterables.size(secondGroupOfEdges) == 4);
        Assert.assertTrue(Iterables.size(thirdGroupOfEdges) == 4);

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
        Assert.assertTrue(atlasFromz8x123y122.numberOfEdges() == 12);
        Assert.assertTrue(atlasFromz8x123y123.numberOfEdges() == 16);
        Assert.assertTrue(atlasFromz7x62y61.numberOfEdges() == 20);
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
        Assert.assertEquals(74311, rawAtlas.numberOfPoints());
        Assert.assertEquals(7817, rawAtlas.numberOfLines());
        Assert.assertEquals(11, rawAtlas.numberOfRelations());

        final Atlas slicedRawAtlas = sliceRawAtlas(rawAtlas, COUNTRIES);

        Assert.assertEquals(0, slicedRawAtlas.numberOfNodes());
        Assert.assertEquals(0, slicedRawAtlas.numberOfEdges());
        Assert.assertEquals(0, slicedRawAtlas.numberOfAreas());
        Assert.assertEquals(74640, slicedRawAtlas.numberOfPoints());
        Assert.assertEquals(8082, slicedRawAtlas.numberOfLines());
        Assert.assertEquals(16, slicedRawAtlas.numberOfRelations());

        // Assert all raw Atlas entities have a country code
        assertAllEntitiesHaveCountryCode(slicedRawAtlas);
    }

    @Test
    public void testPbfToSlicedRawAtlasFilterByCountry()
    {
        // This test is identical to test PbfToSlicedRawAtlas, with added country filter
        final String pbfPath = RawAtlasIntegrationTest.class
                .getResource("8-122-122-trimmed.osm.pbf").getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(pbfPath));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        // We're only interested in CIV country
        final Set<String> onlyIvoryCoast = new HashSet<>();
        onlyIvoryCoast.add("CIV");
        final Atlas slicedRawAtlas = sliceRawAtlas(rawAtlas, onlyIvoryCoast);

        Assert.assertEquals(0, slicedRawAtlas.numberOfNodes());
        Assert.assertEquals(0, slicedRawAtlas.numberOfEdges());
        Assert.assertEquals(0, slicedRawAtlas.numberOfAreas());
        Assert.assertEquals(34784, slicedRawAtlas.numberOfPoints());
        Assert.assertEquals(3636, slicedRawAtlas.numberOfLines());
        Assert.assertEquals(6, slicedRawAtlas.numberOfRelations());

        // Assert all raw Atlas entities have a country code
        assertAllEntitiesHaveCountryCode(slicedRawAtlas);
    }

    @Test
    public void testSectioningFromRawAtlas()
    {
        final String path = RawAtlasIntegrationTest.class.getResource("8-122-122-trimmed.osm.pbf")
                .getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas rawAtlas = rawAtlasGenerator.build();
        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(COUNTRIES, COUNTRY_BOUNDARY_MAP)
                .slice(rawAtlas);
        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        Assert.assertEquals(5011, finalAtlas.numberOfNodes());
        Assert.assertEquals(9764, finalAtlas.numberOfEdges());
        Assert.assertEquals(5128, finalAtlas.numberOfAreas());
        Assert.assertEquals(184, finalAtlas.numberOfPoints());
        Assert.assertEquals(326, finalAtlas.numberOfLines());
        Assert.assertEquals(16, finalAtlas.numberOfRelations());
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

        Assert.assertEquals(5011, finalAtlas.numberOfNodes());
        Assert.assertEquals(9764, finalAtlas.numberOfEdges());
        Assert.assertEquals(5128, finalAtlas.numberOfAreas());
        Assert.assertEquals(184, finalAtlas.numberOfPoints());
        Assert.assertEquals(326, finalAtlas.numberOfLines());
        Assert.assertEquals(14, finalAtlas.numberOfRelations());
    }

    @Test
    public void testStandAloneNodeIngest()
    {
        // This is an OSM node that doesn't have any tags, is not a member of a relation or part of
        // a way. It should end up as a point in the final atlas.

        // Create an Antarctica country
        final Set<String> countries = new HashSet<>();
        final String antarctica = "ATA";
        countries.add(antarctica);

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

        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(countries, countryBoundaryMap)
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
