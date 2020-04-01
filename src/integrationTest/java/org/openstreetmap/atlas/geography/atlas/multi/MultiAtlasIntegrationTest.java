package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasIntegrationTest;
import org.openstreetmap.atlas.geography.atlas.packed.RandomPackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class MultiAtlasIntegrationTest extends AtlasIntegrationTest
{
    private static final Logger logger = LoggerFactory.getLogger(MultiAtlasIntegrationTest.class);

    private MultiAtlas multi;

    public static void main(final String[] args)
    {
        new MultiAtlasIntegrationTest().loadTest(Boolean.valueOf(args[0]), Integer.valueOf(args[1]),
                Long.valueOf(args[2]));
    }

    public MultiAtlas largeMultiAtlas(final int count, final long eachSize)
    {
        final List<Atlas> atlases = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            final long startIdentifier = i > 0 ? i * eachSize - eachSize / 100 : 0;
            final long size = i > 0 ? eachSize + eachSize / 100 : eachSize;
            logger.info("Generating sub-atlas with identifiers starting at {} and ending at {}",
                    startIdentifier, startIdentifier + size - 1);
            atlases.add(RandomPackedAtlasBuilder.generate(size, startIdentifier));
        }
        return new MultiAtlas(atlases, false);
    }

    public void loadTest(final boolean overWrite, final int count, final long eachSize)
    {
        final File atlasFile = new File(
                System.getProperty("user.home") + "/projects/data/unitTest/multiatlas.atlas");
        final MultiAtlas large;
        Time start;
        final Rectangle queryBounds = Location.TEST_5.boxAround(Distance.miles(1));
        if (!overWrite && atlasFile.getFile().exists())
        {
            start = Time.now();
            large = MultiAtlas.load(atlasFile);
            logger.info("Finished Loading from atlas file {} in {}", atlasFile,
                    start.elapsedSince());
        }
        else
        {
            start = Time.now();
            large = largeMultiAtlas(count, eachSize);
            logger.info("Created MultiAtlas in {}", start.elapsedSince());

            start = Time.now();
            large.save(atlasFile);
            logger.info("Finished writing to multiatlas file {} in {}", atlasFile,
                    start.elapsedSince());
        }

        // Edges
        start = Time.now();
        final long edgesSize = Iterables.size(large.edgesIntersecting(queryBounds));
        logger.info("Spatial queried and counted {} edges in {}", edgesSize, start.elapsedSince());

        start = Time.now();
        final long edgesSize2 = Iterables.size(large.edges());
        logger.info("Total: Counted {} edges in {}", edgesSize2, start.elapsedSince());

        // Nodes
        start = Time.now();
        final long nodesSize = Iterables.size(large.nodesWithin(queryBounds));
        logger.info("Spatial queried and counted {} nodes in {}", nodesSize, start.elapsedSince());

        start = Time.now();
        final long nodesSize2 = Iterables.size(large.nodes());
        logger.info("Total: Counted {} nodes in {}", nodesSize2, start.elapsedSince());
    }

    @Test
    public void testPolygonRetrieval()
    {
        final Rectangle bound = new Location(Latitude.degrees(25.0288172),
                Longitude.degrees(-77.5420233)).boxAround(Distance.miles(1));
        final Atlas atlas1 = loadBahamas(bound);
        final Rectangle overlapBound = new Location(Latitude.degrees(25.0213741),
                Longitude.degrees(-77.5237397)).boxAround(Distance.miles(1));
        final Atlas atlas2 = loadBahamas(overlapBound);
        final Rectangle noOverlapBound = new Location(Latitude.degrees(24.3973491),
                Longitude.degrees(-77.8862342)).boxAround(Distance.miles(1));
        final Atlas atlas3 = loadBahamas(noOverlapBound);
        this.multi = new MultiAtlas(atlas1, atlas2, atlas3);
        final Polygon polygon = this.multi.area(24601488000000L).asPolygon();

        Assert.assertEquals(40, Iterables.size(this.multi.edgesIntersecting(polygon)));
        Assert.assertEquals(7, Iterables.size(this.multi.areasIntersecting(polygon)));
    }
}
