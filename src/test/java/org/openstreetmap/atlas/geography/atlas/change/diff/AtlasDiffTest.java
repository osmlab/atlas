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
    public void testAggregateDiff()
    {
        Atlas atlasX = this.rule.simpleAtlas1();
        Atlas atlasY = this.rule.simpleAtlas2();
        int expectedNumberOfChanges = 8;

        assertChangeAtlasConsistency(atlasX, atlasY, expectedNumberOfChanges);

        atlasX = this.rule.simpleAtlas3();
        atlasY = this.rule.simpleAtlas4();
        expectedNumberOfChanges = 16;

        assertChangeAtlasConsistency(atlasX, atlasY, expectedNumberOfChanges);
    }

    @Test
    public void testNodeAndEdgePropertyDiff()
    {
        final Atlas atlasX = this.rule.differentNodeAndEdgeProperties1();
        final Atlas atlasY = this.rule.differentNodeAndEdgeProperties2();

        // 4 node changes, 2 edge changes as a result of the node change, 1 edge shape point change
        final int expectedNumberOfChanges = 7;

        assertChangeAtlasConsistency(atlasX, atlasY, expectedNumberOfChanges);
    }

    @Test
    public void testPointLineAreaPropertyDiff()
    {
        final Atlas atlasX = this.rule.differentPointLineArea1();
        final Atlas atlasY = this.rule.differentPointLineArea2();
        final int expectedNumberOfChanges = 4;

        assertChangeAtlasConsistency(atlasX, atlasY, expectedNumberOfChanges);
    }

    @Test
    public void testRelationsDiff()
    {
        Atlas atlasX = this.rule.differentRelations1();
        Atlas atlasY = this.rule.differentRelations2();
        int expectedNumberOfChanges = 7;

        assertChangeAtlasConsistency(atlasX, atlasY, expectedNumberOfChanges);

        atlasX = this.rule.differentRelations3();
        atlasY = this.rule.differentRelations4();
        expectedNumberOfChanges = 7;

        assertChangeAtlasConsistency(atlasX, atlasY, expectedNumberOfChanges);
    }

    @Test
    public void testTagDiff()
    {
        final Atlas atlasX = this.rule.differentTags1();
        final Atlas atlasY = this.rule.differentTags2();
        final int expectedNumberOfChanges = 3;

        assertChangeAtlasConsistency(atlasX, atlasY, expectedNumberOfChanges);
    }

    private void assertChangeAtlasConsistency(final Atlas atlasX, final Atlas atlasY,
            final int expectedNumberOfChanges)
    {
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
