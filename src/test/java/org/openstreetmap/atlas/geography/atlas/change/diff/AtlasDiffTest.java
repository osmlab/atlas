package org.openstreetmap.atlas.geography.atlas.change.diff;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.Change;
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
        final Atlas atlasX = this.rule.simpleAtlas1();
        final Atlas atlasY = this.rule.simpleAtlas2();
        final int expectedNumberOfChanges = 8;

        // First, test with bloated entities
        final Change changeXToYBloated = new AtlasDiff(atlasX, atlasY).useBloatedEntities(true)
                .generateChange();
        Assert.assertEquals(expectedNumberOfChanges, changeXToYBloated.changeCount());
        final ChangeAtlas changeAtlasY = new ChangeAtlas(atlasX, changeXToYBloated);
        Assert.assertFalse(new AtlasDiff(changeAtlasY, atlasY).generateChange().hasChanges());
        Assert.assertEquals(atlasY, changeAtlasY);

        // Now test not-bloated entities
        final Change changeXToY = new AtlasDiff(atlasX, atlasY).useBloatedEntities(false)
                .generateChange();
        Assert.assertEquals(expectedNumberOfChanges, changeXToY.changeCount());
        final ChangeAtlas changeAtlasY_2 = new ChangeAtlas(atlasX, changeXToY);
        Assert.assertFalse(new AtlasDiff(changeAtlasY_2, atlasY).generateChange().hasChanges());
        Assert.assertEquals(atlasY, changeAtlasY_2);
    }

    @Test
    public void testNodeLocationsDiff()
    {
        final Atlas atlasX = this.rule.differentNodeLocations1();
        final Atlas atlasY = this.rule.differentNodeLocations2();
        final int expectedNumberOfChanges = 1;

        final Change changeXToYBloated = new AtlasDiff(atlasX, atlasY).useBloatedEntities(true)
                .generateChange();
        Assert.assertEquals(expectedNumberOfChanges, changeXToYBloated.changeCount());
        final ChangeAtlas changeAtlasY = new ChangeAtlas(atlasX, changeXToYBloated);
        Assert.assertFalse(new AtlasDiff(changeAtlasY, atlasY).generateChange().hasChanges());
        Assert.assertEquals(atlasY, changeAtlasY);
    }

    @Test
    public void testParentRelationsDiff()
    {
        // TODO actually fill in this test once full relation support
        final Atlas atlasX = this.rule.differentParentRelations1();
        final Atlas atlasY = this.rule.differentParentRelations2();
        final int expectedNumberOfChanges = 1;

        final Change changeXToYBloated = new AtlasDiff(atlasX, atlasY).useBloatedEntities(true)
                .saveAllGeometries(true).generateChange();
        changeXToYBloated.changes()
                .forEach(change -> logger.trace("{}: {}", change, change.getReference()));
    }

    @Test
    public void testTagDiff()
    {
        final Atlas atlasX = this.rule.differentTags1();
        final Atlas atlasY = this.rule.differentTags2();
        final int expectedNumberOfChanges = 3;

        // First test with bloated entities
        final Change changeXToYBloated = new AtlasDiff(atlasX, atlasY).useBloatedEntities(true)
                .generateChange();
        changeXToYBloated.changes()
                .forEach(change -> logger.trace("{}: {}", change, change.getReference()));
        Assert.assertEquals(expectedNumberOfChanges, changeXToYBloated.changeCount());
        final ChangeAtlas changeAtlasY = new ChangeAtlas(atlasX, changeXToYBloated);
        Assert.assertFalse(new AtlasDiff(changeAtlasY, atlasY).generateChange().hasChanges());
        Assert.assertEquals(atlasY, changeAtlasY);

        // Now test with non-bloated
        final Change changeXToY = new AtlasDiff(atlasX, atlasY).useBloatedEntities(false)
                .generateChange();
        Assert.assertEquals(expectedNumberOfChanges, changeXToY.changeCount());
        final ChangeAtlas changeAtlasY_2 = new ChangeAtlas(atlasX, changeXToY);
        Assert.assertFalse(new AtlasDiff(changeAtlasY_2, atlasY).generateChange().hasChanges());
        Assert.assertEquals(atlasY, changeAtlasY_2);
    }
}
