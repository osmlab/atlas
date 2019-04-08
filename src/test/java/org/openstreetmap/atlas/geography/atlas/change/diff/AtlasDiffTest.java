package org.openstreetmap.atlas.geography.atlas.change.diff;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class AtlasDiffTest
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDiffTest.class);

    private static final int IGNORE_EXPECTED_NUMBER_CHANGES = -1;

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
        expectedNumberOfChanges = 15;

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
    public void testRelationMemberRemoval()
    {
        final Atlas atlasX = this.rule.removeRelationMember1();
        final Atlas atlasY = this.rule.removeRelationMember2();
        final int expectedNumberOfChanges = 2;

        assertChangeAtlasConsistency(atlasX, atlasY, expectedNumberOfChanges);
    }

    @Test
    public void testRelationsDiff()
    {
        Atlas atlasX = this.rule.differentRelations1();
        Atlas atlasY = this.rule.differentRelations2();
        int expectedNumberOfChanges = 9;

        assertChangeAtlasConsistency(atlasX, atlasY, expectedNumberOfChanges);

        atlasX = this.rule.differentRelations3();
        atlasY = this.rule.differentRelations4();
        expectedNumberOfChanges = 5;

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

    private void assertChangeAtlasConsistency(final Atlas beforeAtlas, final Atlas afterAtlas,
            final int expectedNumberOfChanges)
    {
        final Change changeBeforeToAfter = new AtlasDiff(beforeAtlas, afterAtlas).generateChange()
                .orElseThrow(() -> new CoreException(
                        "This Change should never be empty. The unit test may be broken."));
        changeBeforeToAfter.changes().forEach(change -> logger.trace("{}:\n{} ->\n{}", change,
                change.getBeforeView(), change.getAfterView()));

        if (expectedNumberOfChanges != IGNORE_EXPECTED_NUMBER_CHANGES)
        {
            Assert.assertEquals(expectedNumberOfChanges, changeBeforeToAfter.changeCount());
        }
        final ChangeAtlas changeAfterAtlas = new ChangeAtlas(beforeAtlas, changeBeforeToAfter);

        // test both ways to catch any slip-ups in the code
        Assert.assertFalse(
                new AtlasDiff(changeAfterAtlas, afterAtlas).generateChange().isPresent());
        Assert.assertFalse(
                new AtlasDiff(afterAtlas, changeAfterAtlas).generateChange().isPresent());

        Assert.assertEquals(afterAtlas, changeAfterAtlas);

        // Now test that PackedAtlas cloning is consistent. This is guaranteed by AtlasDiff so we
        // must ensure it holds.
        Assert.assertEquals(new PackedAtlasCloner().cloneFrom(afterAtlas),
                new PackedAtlasCloner().cloneFrom(changeAfterAtlas));
    }
}
