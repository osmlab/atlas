package org.openstreetmap.atlas.geography.atlas.delta;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.delta.Diff.DiffType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasDeltaTagTest
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDeltaTagTest.class);

    @Test
    public void testDifferentTags()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        final Map<String, String> baseTags = new HashMap<>(tags);
        baseTags.put("key", "value");
        final Map<String, String> alterTags = new HashMap<>(tags);
        alterTags.put("key", "values");
        baseBuilder.addLine(1, PolyLine.TEST_POLYLINE, baseTags);
        alterBuilder.addLine(1, PolyLine.TEST_POLYLINE, alterTags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        Assert.assertEquals(1, diffs.size());

        final Diff diff = diffs.first();
        logger.debug("testDifferentTags(): {}", Diff.toString(diffs));
        Assert.assertEquals(DiffType.CHANGED, diff.getDiffType());
    }

    @Test
    public void testSameTags()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);
        final Map<String, String> baseTags = new HashMap<>(tags);
        baseTags.put("key", "value");
        final Map<String, String> alterTags = new HashMap<>(tags);
        alterTags.put("key", "value");
        baseBuilder.addLine(1, PolyLine.TEST_POLYLINE, baseTags);
        alterBuilder.addLine(1, PolyLine.TEST_POLYLINE, alterTags);
        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();
        Assert.assertEquals(0, diffs.size());
    }
}
