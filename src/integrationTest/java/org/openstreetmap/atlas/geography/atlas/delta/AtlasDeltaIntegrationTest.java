package org.openstreetmap.atlas.geography.atlas.delta;

import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasDeltaIntegrationTest
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDeltaIntegrationTest.class);

    @Test
    public void testDiff()
    {
        final Atlas before = new TextAtlasBuilder()
                .read(new InputStreamResource(() -> AtlasDeltaIntegrationTest.class
                        .getResourceAsStream("DMA_9-168-233-base.txt.gz"))
                                .withDecompressor(Decompressor.GZIP)
                                .withName("DMA_9-168-233-base.txt.gz"));
        final Atlas after = new TextAtlasBuilder()
                .read(new InputStreamResource(() -> AtlasDeltaIntegrationTest.class
                        .getResourceAsStream("DMA_9-168-233-alter.txt.gz"))
                                .withDecompressor(Decompressor.GZIP)
                                .withName("DMA_9-168-233-alter.txt.gz"));
        final AtlasDelta delta = new AtlasDelta(before, after, true).generate();
        final SortedSet<Diff> differences = delta.getDifferences();
        final long size = differences.size();
        final long sizeNodes = differences.stream()
                .filter(diff -> diff.getItemType() == ItemType.NODE).count();
        final long sizeEdges = differences.stream()
                .filter(diff -> diff.getItemType() == ItemType.EDGE).count();
        final long sizeAreas = differences.stream()
                .filter(diff -> diff.getItemType() == ItemType.AREA).count();
        final long sizeLines = differences.stream()
                .filter(diff -> diff.getItemType() == ItemType.LINE).count();
        final long sizePoints = differences.stream()
                .filter(diff -> diff.getItemType() == ItemType.POINT).count();
        final long sizeRelations = differences.stream()
                .filter(diff -> diff.getItemType() == ItemType.RELATION).count();
        logger.info("Size of the Delta: {}", size);
        logger.info("Size of the Delta Nodes: {}", sizeNodes);
        logger.info("Size of the Delta Edges: {}", sizeEdges);
        logger.info("Size of the Delta Areas: {}", sizeAreas);
        logger.info("Size of the Delta Lines: {}", sizeLines);
        logger.info("Size of the Delta Points: {}", sizePoints);
        logger.info("Size of the Delta Relations: {}", sizeRelations);
        Assert.assertEquals(33475, size);
        Assert.assertEquals(3519, sizeNodes);
        Assert.assertEquals(16239, sizeEdges);
        Assert.assertEquals(13285, sizeAreas);
        Assert.assertEquals(94, sizeLines);
        Assert.assertEquals(324, sizePoints);
        Assert.assertEquals(14, sizeRelations);
        Assert.assertEquals(size,
                sizeNodes + sizeEdges + sizeAreas + sizeLines + sizePoints + sizeRelations);
    }
}
