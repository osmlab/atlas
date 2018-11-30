package org.openstreetmap.atlas.geography.atlas.delta;

import java.util.Map;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.delta.Diff.DiffType;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasDeltaNodeTest
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDeltaNodeTest.class);

    @Test
    public void testAdded()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(2, Location.TEST_2, tags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        Assert.assertEquals(1, diffs.size());

        final Diff diff = diffs.first();
        logger.debug("testAdded(): {}", Diff.toString(diffs));
        Assert.assertEquals(DiffType.ADDED, diff.getDiffType());
        Assert.assertEquals(2, diff.getIdentifier());
    }

    @Test
    public void testDifferentGeometry()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(1, Location.TEST_6, tags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        Assert.assertEquals(1, diffs.size());

        final Diff diff = diffs.first();
        logger.debug("testDifferentGeometry(): {}", Diff.toString(diffs));
        Assert.assertEquals(DiffType.CHANGED, diff.getDiffType());
    }

    @Test
    public void testDifferentInOutEdges()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(1, Location.TEST_1, tags);
        baseBuilder.addNode(2, Location.TEST_6, tags);
        alterBuilder.addNode(2, Location.TEST_6, tags);
        baseBuilder.addNode(3, Location.TEST_2, tags);
        alterBuilder.addNode(3, Location.TEST_2, tags);

        baseBuilder.addEdge(1, new Segment(Location.TEST_6, Location.TEST_1), tags);
        alterBuilder.addEdge(1, new Segment(Location.TEST_6, Location.TEST_1), tags);
        baseBuilder.addEdge(2, new Segment(Location.TEST_2, Location.TEST_1), tags);

        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        Assert.assertEquals(3, diffs.size());

        logger.debug("testDifferentInOutEdges(): {}", Diff.toString(diffs));

        for (final Diff diff : diffs)
        {
            if (diff.getIdentifier() == 2)
            {
                Assert.assertEquals(DiffType.REMOVED, diff.getDiffType());
                Assert.assertEquals(ItemType.EDGE, diff.getItemType());
            }
            else
            {
                Assert.assertEquals(DiffType.CHANGED, diff.getDiffType());
                Assert.assertEquals(ItemType.NODE, diff.getItemType());
            }
        }
    }

    @Test
    public void testRandom()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = Maps.hashMap("tag1", "value1");
        final Map<String, String> tags2 = Maps.hashMap("tag1", "value1", "tag2", "value2");
        baseBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(1, Location.TEST_2, tags2);
        baseBuilder.addNode(3, Location.TEST_2, tags);
        alterBuilder.addNode(3, Location.TEST_2, tags);

        final SortedSet<Diff> diffs = new AtlasDelta(baseBuilder.get(), alterBuilder.get())
                .generate().getDifferences();

        for (final Diff diff : diffs)
        {
            logger.error("{}", diff.toDiffViewFriendlyString());
        }
    }

    @Test
    public void testRemoved()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(1, Location.TEST_1, tags);
        baseBuilder.addNode(2, Location.TEST_2, tags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        Assert.assertEquals(1, diffs.size());

        final Diff diff = diffs.first();
        logger.debug("testRemoved(): {}", Diff.toString(diffs));
        Assert.assertEquals(DiffType.REMOVED, diff.getDiffType());
        Assert.assertEquals(2, diff.getIdentifier());
    }
}
