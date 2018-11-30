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

        final Change changeXToYBloated = new AtlasDiff(atlasX, atlasY).useBloatedEntities(true)
                .generateChange();

        Assert.assertTrue(changeXToYBloated.hasChanges());
        Assert.assertEquals(8, changeXToYBloated.changeCount());

        changeXToYBloated.changes().forEach(featureChange -> logger.trace("{}", featureChange));

        final ChangeAtlas changeAtlasY = new ChangeAtlas(atlasX, changeXToYBloated);
        Assert.assertTrue(
                new AtlasDelta(atlasY, changeAtlasY).generate().getDifferences().size() == 0);
        Assert.assertFalse(new AtlasDiff(changeAtlasY, atlasY).generateChange().hasChanges());

        // Now test not-bloated
        final Change changeXToY = new AtlasDiff(atlasX, atlasY).useBloatedEntities(false)
                .generateChange();

        Assert.assertTrue(changeXToY.hasChanges());
        Assert.assertEquals(8, changeXToY.changeCount());

        final ChangeAtlas changeAtlasY2 = new ChangeAtlas(atlasX, changeXToY);
        Assert.assertTrue(
                new AtlasDelta(atlasY, changeAtlasY2).generate().getDifferences().size() == 0);
        Assert.assertFalse(new AtlasDiff(changeAtlasY2, atlasY).generateChange().hasChanges());
    }

    @Test
    public void testTagDiff()
    {
        final Atlas atlasX = this.rule.getAtlas3();
        final Atlas atlasY = this.rule.getAtlas4();

        final Change changeXToYBloated = new AtlasDiff(atlasX, atlasY).useBloatedEntities(true)
                .generateChange();

        Assert.assertTrue(changeXToYBloated.hasChanges());
        Assert.assertEquals(3, changeXToYBloated.changeCount());

        changeXToYBloated.changes().forEach(featureChange -> logger.trace("{}: {}", featureChange,
                featureChange.getReference().getTags()));

        final ChangeAtlas changeAtlasY = new ChangeAtlas(atlasX, changeXToYBloated);
        Assert.assertTrue(
                new AtlasDelta(atlasY, changeAtlasY).generate().getDifferences().size() == 0);
        Assert.assertFalse(new AtlasDiff(changeAtlasY, atlasY).generateChange().hasChanges());
    }
}
