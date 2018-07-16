package org.openstreetmap.atlas.geography.atlas.delta;

import java.util.Map;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.delta.Diff.DiffType;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasDeltaEdgeTest
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDeltaEdgeTest.class);

    @Test
    public void testAdded()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addNode(1, Location.TEST_1, tags);
        baseBuilder.addNode(2, Location.TEST_2, tags);
        alterBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(2, Location.TEST_2, tags);
        alterBuilder.addEdge(0, new Segment(Location.TEST_1, Location.TEST_2), tags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        logger.debug("testAdded(): {}", Diff.toString(diffs));
        logger.debug("testAdded(): {}", Diff.toDiffViewFriendlyString(diffs));
        Assert.assertEquals(3, diffs.size());

        boolean foundEdge = false;
        for (final Diff diff : diffs)
        {
            if (diff.getItemType() == ItemType.EDGE)
            {
                Assert.assertEquals(DiffType.ADDED, diff.getDiffType());
                foundEdge = true;
            }
        }
        if (!foundEdge)
        {
            Assert.fail("Did not find a diff on an Edge.");
        }
    }

    @Test
    public void testDifferentEndNode()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addNode(1, Location.TEST_1, tags);
        baseBuilder.addNode(2, Location.TEST_2, tags);
        baseBuilder.addNode(3, Location.TEST_7, tags);
        alterBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(2, Location.TEST_7, tags);
        alterBuilder.addNode(3, Location.TEST_2, tags);
        baseBuilder.addEdge(0, new Segment(Location.TEST_1, Location.TEST_2), tags);
        // The alter edge has the same polyLine, but the end node id is different
        alterBuilder.addEdge(0, new Segment(Location.TEST_1, Location.TEST_2), tags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        Assert.assertEquals(3, diffs.size());

        logger.debug("testDifferentEndNode(): {}", Diff.toString(diffs));
        boolean foundEdge = false;
        for (final Diff diff : diffs)
        {
            if (diff.getItemType() == ItemType.EDGE)
            {
                Assert.assertEquals(DiffType.CHANGED, diff.getDiffType());
                foundEdge = true;
            }
        }
        if (!foundEdge)
        {
            Assert.fail("Did not find a diff on an Edge.");
        }
    }

    @Test
    public void testDifferentGeometry()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addNode(1, Location.TEST_1, tags);
        baseBuilder.addNode(2, Location.TEST_2, tags);
        alterBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(2, Location.TEST_2, tags);
        baseBuilder.addEdge(0, new Segment(Location.TEST_1, Location.TEST_2), tags);
        alterBuilder.addEdge(0, new PolyLine(Location.TEST_1, Location.TEST_7, Location.TEST_2),
                tags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        Assert.assertEquals(1, diffs.size());

        final Diff diff = diffs.first();
        logger.debug("testDifferentGeometry(): {}", Diff.toString(diffs));
        Assert.assertEquals(DiffType.CHANGED, diff.getDiffType());
    }

    @Test
    public void testDifferentStartNode()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addNode(1, Location.TEST_1, tags);
        baseBuilder.addNode(2, Location.TEST_2, tags);
        baseBuilder.addNode(3, Location.TEST_7, tags);
        alterBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(2, Location.TEST_7, tags);
        alterBuilder.addNode(3, Location.TEST_2, tags);
        baseBuilder.addEdge(0, new Segment(Location.TEST_2, Location.TEST_1), tags);
        // The alter edge has the same polyLine, but the end node id is different
        alterBuilder.addEdge(0, new Segment(Location.TEST_2, Location.TEST_1), tags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        Assert.assertEquals(3, diffs.size());

        logger.debug("testDifferentStartNode(): {}", Diff.toString(diffs));
        boolean foundEdge = false;
        for (final Diff diff : diffs)
        {
            if (diff.getItemType() == ItemType.EDGE)
            {
                Assert.assertEquals(DiffType.CHANGED, diff.getDiffType());
                foundEdge = true;
            }
        }
        if (!foundEdge)
        {
            Assert.fail("Did not find a diff on an Edge.");
        }
    }

    @Test
    public void testRemoved()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addNode(1, Location.TEST_1, tags);
        baseBuilder.addNode(2, Location.TEST_2, tags);
        alterBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(2, Location.TEST_2, tags);
        baseBuilder.addEdge(0, new Segment(Location.TEST_1, Location.TEST_2), tags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        Assert.assertEquals(3, diffs.size());

        logger.debug("testRemoved(): {}", Diff.toString(diffs));
        boolean foundEdge = false;
        for (final Diff diff : diffs)
        {
            if (diff.getItemType() == ItemType.EDGE)
            {
                Assert.assertEquals(DiffType.REMOVED, diff.getDiffType());
                foundEdge = true;
            }
        }
        if (!foundEdge)
        {
            Assert.fail("Did not find a diff on an Edge.");
        }
    }

    @Test
    public void testWaySectionNoMatch()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder().withName("base");
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder().withName("alter");
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        baseBuilder.addNode(1, Location.TEST_1, tags);
        baseBuilder.addNode(3, Location.TEST_7, tags);
        alterBuilder.addNode(1, Location.TEST_1, tags);
        alterBuilder.addNode(2, Location.TEST_2, tags);
        alterBuilder.addNode(3, Location.TEST_7, tags);
        alterBuilder.addNode(4, Location.TEST_5, tags);
        baseBuilder.addEdge(1000000,
                new PolyLine(Location.TEST_1, Location.TEST_2, Location.TEST_7), tags);
        baseBuilder.addEdge(-1000000,
                new PolyLine(Location.TEST_7, Location.TEST_2, Location.TEST_1), tags);
        // The alter edge has the same polyLine, but the end node id is different
        alterBuilder.addEdge(1000001, new Segment(Location.TEST_1, Location.TEST_2), tags);
        alterBuilder.addEdge(-1000001, new Segment(Location.TEST_2, Location.TEST_1), tags);
        alterBuilder.addEdge(1000002, new Segment(Location.TEST_2, Location.TEST_7), tags);
        alterBuilder.addEdge(-1000002, new Segment(Location.TEST_7, Location.TEST_2), tags);
        alterBuilder.addEdge(2000000, new Segment(Location.TEST_2, Location.TEST_5), tags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter, true).generate().getDifferences();
        logger.debug("testWaySectionNoMatch(): {}", Diff.toString(diffs));

        Assert.assertEquals(3, diffs.size());

        boolean foundEdge = false;
        for (final Diff diff : diffs)
        {
            if (diff.getItemType() == ItemType.EDGE)
            {
                Assert.assertEquals(DiffType.ADDED, diff.getDiffType());
                foundEdge = true;
            }
            if (diff.getItemType() == ItemType.NODE)
            {
                Assert.assertEquals(DiffType.ADDED, diff.getDiffType());
                final long nodeIdentifier = diff.getAlterEntity().getIdentifier();
                if (nodeIdentifier != 2 && nodeIdentifier != 4)
                {
                    Assert.fail("Found an unexpected node added: " + nodeIdentifier);
                }
            }
        }
        if (!foundEdge)
        {
            Assert.fail("Did not find a diff on an Edge.");
        }
    }
}
