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
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.ShardFileOverlapsPolygonTest;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoader;
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator;
import org.openstreetmap.atlas.geography.atlas.raw.sectioning.WaySectionProcessor;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasCountrySlicer;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.sharding.DynamicTileSharding;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests several Raw Atlas Flows: 1. OSM PBF to Sliced Raw Atlas 2. Sliced Raw Atlas to Sectioned
 * Raw Atlas 3. Multiple Sliced Raw Atlases to Sectioned Raw atlas
 *
 * @author mgostintsev
 */
public class OsmPbfToSlicedRawAtlasTest
{
    private static CountryBoundaryMap COUNTRY_BOUNDARY_MAP;
    private static Set<IsoCountry> COUNTRIES;

    private static final long LINE_OSM_IDENTIFIER_CROSSING_3_SHARDS = 541706;

    private static final Logger logger = LoggerFactory.getLogger(OsmPbfToSlicedRawAtlasTest.class);

    static
    {
        COUNTRIES = new HashSet<>();
        COUNTRIES.add(IsoCountry.forCountryCode("CIV").get());
        COUNTRIES.add(IsoCountry.forCountryCode("GIN").get());
        COUNTRIES.add(IsoCountry.forCountryCode("LBR").get());

        COUNTRY_BOUNDARY_MAP = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> OsmPbfToSlicedRawAtlasTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries_with_grid_index.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
    }

    @Rule
    public DynamicRawAtlasSectioningTestRule setup = new DynamicRawAtlasSectioningTestRule();

    @Test
    public void testPbfToSlicedAtlasWithExpansion()
    {
        // Create a simple store, populated with 3 shards and the corresponding atlases.
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

        // Let's focus on the edge spanning all 3 shards and verify it got sectioned properly.
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

        // There should be 4 pieces (each having a forward and reverse edge) total.
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
        final String path = OsmPbfToSlicedRawAtlasTest.class.getResource("8-122-122.osm.pbf")
                .getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        Assert.assertEquals(0, rawAtlas.numberOfNodes());
        Assert.assertEquals(0, rawAtlas.numberOfEdges());
        Assert.assertEquals(0, rawAtlas.numberOfAreas());
        Assert.assertEquals(646583, rawAtlas.numberOfPoints());
        Assert.assertEquals(57133, rawAtlas.numberOfLines());
        Assert.assertEquals(36, rawAtlas.numberOfRelations());

        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(COUNTRIES, COUNTRY_BOUNDARY_MAP)
                .slice(rawAtlas);

        Assert.assertEquals(0, slicedRawAtlas.numberOfNodes());
        Assert.assertEquals(0, slicedRawAtlas.numberOfEdges());
        Assert.assertEquals(0, slicedRawAtlas.numberOfAreas());
        Assert.assertEquals(648704, slicedRawAtlas.numberOfPoints());
        Assert.assertEquals(57643, slicedRawAtlas.numberOfLines());
        Assert.assertEquals(44, slicedRawAtlas.numberOfRelations());

        // Assert all Raw Atlas Entities have a country code
        slicedRawAtlas.lines().forEach(line ->
        {
            Assert.assertTrue(Validators.hasValuesFor(line, ISOCountryTag.class));
        });
        slicedRawAtlas.points().forEach(point ->
        {
            Assert.assertTrue(Validators.hasValuesFor(point, ISOCountryTag.class));
        });
        slicedRawAtlas.relations().forEach(point ->
        {
            Assert.assertTrue(Validators.hasValuesFor(point, ISOCountryTag.class));
        });

        // Previous PBF-to-Atlas Implementation - let's compare the two results
        final String pbfPath = OsmPbfToSlicedRawAtlasTest.class.getResource("8-122-122.osm.pbf")
                .getPath();
        final OsmPbfLoader loader = new OsmPbfLoader(new File(pbfPath), AtlasLoadingOption
                .createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP).setWaySectioning(false));
        final Atlas oldSlicedAtlas = loader.read();

        Assert.assertTrue(
                "The original Atlas counts of (Lines + Master Edges + Areas), without way-sectioning, should be"
                        + "equal to the total number of all Lines in the Raw Atlas (+1 for relation handling).",
                Iterables.size(Iterables.filter(oldSlicedAtlas.edges(), Edge::isMasterEdge))
                        + oldSlicedAtlas.numberOfAreas()
                        + oldSlicedAtlas.numberOfLines() == slicedRawAtlas.numberOfLines() - 1);
    }

    @Test
    public void testSectioningFromRawAtlas()
    {
        final String path = OsmPbfToSlicedRawAtlasTest.class.getResource("8-122-122.osm.pbf")
                .getPath();
        final RawAtlasGenerator rawAtlasGenerator = new RawAtlasGenerator(new File(path));
        final Atlas rawAtlas = rawAtlasGenerator.build();

        final Atlas slicedRawAtlas = new RawAtlasCountrySlicer(COUNTRIES, COUNTRY_BOUNDARY_MAP)
                .slice(rawAtlas);

        final Atlas finalAtlas = new WaySectionProcessor(slicedRawAtlas,
                AtlasLoadingOption.createOptionWithAllEnabled(COUNTRY_BOUNDARY_MAP)).run();

        logger.info(finalAtlas.summary());
    }

    @Test
    public void testSectioningFromShard()
    {
        final String path = OsmPbfToSlicedRawAtlasTest.class.getResource("8-122-122.osm.pbf")
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

        logger.info(finalAtlas.summary());
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
}
