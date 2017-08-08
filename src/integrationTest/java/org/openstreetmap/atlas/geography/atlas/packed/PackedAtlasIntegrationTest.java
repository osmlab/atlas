package org.openstreetmap.atlas.geography.atlas.packed;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasIntegrationTest;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class PackedAtlasIntegrationTest extends AtlasIntegrationTest
{
    private static final Logger logger = LoggerFactory.getLogger(PackedAtlasIntegrationTest.class);

    public static void main(final String[] args)
    {
        new PackedAtlasIntegrationTest().loadTest(false, 10000);
    }

    public void loadTest(final boolean overWrite, final long size)
    {
        final File atlasFile = new File(
                System.getProperty("user.home") + "/projects/data/unitTest/atlas.atlas");
        final PackedAtlas large;
        Time start = Time.now();
        final Rectangle queryBoundsEdges = Location.TEST_5.boxAround(Distance.miles(1));
        final Rectangle queryBoundsNodes = Location.TEST_5.boxAround(Distance.miles(10));
        if (!overWrite && atlasFile.getFile().exists())
        {
            start = Time.now();
            large = PackedAtlas.load(atlasFile);
            logger.info("Finished Loading from atlas file {} in {}", atlasFile,
                    start.elapsedSince());
        }
        else
        {
            large = RandomPackedAtlasBuilder.generate(size, 1);
            start = Time.now();
            large.save(atlasFile);
            logger.info("Finished writing to atlas file {} in {}", atlasFile, start.elapsedSince());
        }

        // Queries
        start = Time.now();
        final long edgesSize = Iterables.size(large.edgesIntersecting(queryBoundsEdges));
        logger.info("Spatial queried and counted {} edges in {}", edgesSize, start.elapsedSince());

        start = Time.now();
        final long nodesSize = Iterables.size(large.nodesWithin(queryBoundsNodes));
        logger.info("Spatial queried and counted {} nodes in {}", nodesSize, start.elapsedSince());

        start = Time.now();
        final long nodes = Iterables.size(large.nodes());
        logger.info("Total: Counted {} nodes in {}", nodes, start.elapsedSince());

        start = Time.now();
        final long edgeShapePoints = Iterables.count(large.edgesIntersecting(queryBoundsEdges),
                edge -> (long) edge.asPolyLine().size());
        logger.info("Spatial queried and counted {} edge shape points in {}", edgeShapePoints,
                start.elapsedSince());

        start = Time.now();
        final long edgesSize2 = Iterables.size(large.edges());
        logger.info("Total: Counted {} edges in {}", edgesSize2, start.elapsedSince());

        start = Time.now();
        final long relationsSize = Iterables.size(large.relations());
        logger.info("Total: Counted {} relations in {}", relationsSize, start.elapsedSince());
    }

    @Test
    public void testPolygonRetrieval()
    {
        final Rectangle largerBound = new Location(Latitude.degrees(25.0288172),
                Longitude.degrees(-77.5420233)).boxAround(Distance.miles(1));
        final Atlas atlas = loadBahamas(largerBound);

        final Polygon polygon = atlas.area(24601488000000L).asPolygon();

        Assert.assertEquals(6, Iterables.size(atlas.edgesIntersecting(polygon)));
        Assert.assertEquals(7, Iterables.size(atlas.areasIntersecting(polygon)));
    }

    @Test
    public void testSerialization()
    {
        // Raw
        final ByteArrayResource resource = new ByteArrayResource()
                .withName("testSerializationByteArray");
        final Atlas atlas = RandomPackedAtlasBuilder.generate(1000, 0);
        atlas.save(resource);
        final PackedAtlas deserialized = PackedAtlas.load(resource);
        Assert.assertTrue(atlas.equals(deserialized));

        // With compression
        final ByteArrayResource compressedResource = new ByteArrayResource()
                .withName("testSerializationByteArrayCompressed");
        compressedResource.setCompressor(Compressor.GZIP);
        compressedResource.setDecompressor(Decompressor.GZIP);
        atlas.save(compressedResource);
        final PackedAtlas compressedeserialized = PackedAtlas.load(compressedResource);
        Assert.assertTrue(atlas.equals(compressedeserialized));
    }
}
