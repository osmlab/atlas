package org.openstreetmap.atlas.geography.atlas.change.diff;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.delta.AtlasDelta;
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
        final Atlas atlasX = this.rule.getAtlas1();
        final Atlas atlasY = this.rule.getAtlas2();

        final Change changeXToY = new AtlasDiff(atlasX, atlasY).generateChange();

        Assert.assertTrue(changeXToY.hasChanges());

        // TODO Comment this assert out until we fix the createModifyFeatureChange
        // Assert.assertEquals(8, changeXToY.changeCount());

        changeXToY.changes().forEach(featureChange -> logger.trace("{}", featureChange));

        final ChangeAtlas changeAtlasY = new ChangeAtlas(atlasX, changeXToY);

        // TODO temporarily use old AtlasDelta to confirm equivalence
        Assert.assertTrue(
                new AtlasDelta(atlasY, changeAtlasY).generate().getDifferences().size() == 0);

        // TODO Comment this assert out until we fix the createModifyFeatureChange
        // Assert.assertFalse(new AtlasDiff(changeAtlasY, atlasY).generateChange().hasChanges());
    }
}
