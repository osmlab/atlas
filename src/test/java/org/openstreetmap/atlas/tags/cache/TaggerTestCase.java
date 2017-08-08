package org.openstreetmap.atlas.tags.cache;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.OneWayTag;

/**
 * Tagger tests.
 *
 * @author gpogulsky
 */
public class TaggerTestCase
{
    @Rule
    public final TaggerTestRule rule = new TaggerTestRule();

    @Test
    public void testTag()
    {
        final Tagger<HighwayTag> highwayTagger = new Tagger<>(HighwayTag.class);
        final Tagger<OneWayTag> onewayTagger = new Tagger<>(OneWayTag.class);

        for (final Edge edge : this.rule.getAtlas().edges())
        {
            final Optional<HighwayTag> tag = highwayTagger.getTag(edge);
            Assert.assertTrue(tag.isPresent());
            Assert.assertEquals(HighwayTag.SECONDARY, tag.get());

            final Optional<OneWayTag> otag = onewayTagger.getTag(edge);
            if (edge.getIdentifier() == 12000000)
            {
                Assert.assertFalse(otag.isPresent());
            }
            else
            {
                Assert.assertTrue(otag.isPresent());
                Assert.assertEquals(OneWayTag.YES, otag.get());
            }
        }

    }

}
