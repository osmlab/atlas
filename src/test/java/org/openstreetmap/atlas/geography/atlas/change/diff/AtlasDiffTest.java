package org.openstreetmap.atlas.geography.atlas.change.diff;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class AtlasDiffTest
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDiffTest.class);

    @Rule
    public AtlasDiffTestRule rule = new AtlasDiffTestRule();

    @Test
    public void simpleTest()
    {
        final Atlas atlas1 = this.rule.getAtlas1();
        final Atlas atlas2 = this.rule.getAtlas2();

        Assert.assertTrue(new AtlasDiff(atlas1, atlas2).generateChange().hasChanges());

        final ChangeAtlas changeAtlas = new ChangeAtlas(atlas1,
                new AtlasDiff(atlas1, atlas2).generateChange());
        Assert.assertFalse(new AtlasDiff(changeAtlas, atlas2).generateChange().hasChanges());
    }
}
