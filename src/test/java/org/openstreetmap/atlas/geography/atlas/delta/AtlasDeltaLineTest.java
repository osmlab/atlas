package org.openstreetmap.atlas.geography.atlas.delta;

import java.util.Map;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasDeltaLineTest
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDeltaLineTest.class);

    @Test
    public void testDifferentGeometry()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);

        baseBuilder.addLine(1, PolyLine.TEST_POLYLINE, tags);
        alterBuilder.addLine(1,
                new PolyLine(new MultiIterable<>(PolyLine.TEST_POLYLINE, Location.TEST_6)), tags);

        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();

        Assert.assertEquals(1, diffs.size());
        logger.debug("testDifferentGeometry(): {}", Diff.toString(diffs));
    }

    @Test
    public void testSame()
    {
        final PackedAtlasBuilder baseBuilder = new PackedAtlasBuilder();
        final PackedAtlasBuilder alterBuilder = new PackedAtlasBuilder();
        final Map<String, String> tags = RandomTagsSupplier.randomTags(5);

        baseBuilder.addLine(1, PolyLine.TEST_POLYLINE, tags);
        alterBuilder.addLine(1, PolyLine.TEST_POLYLINE, tags);

        final Atlas base = baseBuilder.get();
        final Atlas alter = alterBuilder.get();

        final SortedSet<Diff> diffs = new AtlasDelta(base, alter).generate().getDifferences();

        Assert.assertEquals(0, diffs.size());
    }
}
