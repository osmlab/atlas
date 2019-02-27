package org.openstreetmap.atlas.geography.atlas.change;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the merge feature of Change class.
 *
 * @author Yazad Khambata
 */
public class ChangeMergeTest extends AbstractChangeTest
{

    @Test
    public void testMergeEmptySelf()
    {
        final Change changeWithAreaAndLine1 = newChangeWithAreaAndLine();

        final Change merged = Change.merge(changeWithAreaAndLine1, changeWithAreaAndLine1);

        log.info("merged: {}", merged);
        log.info("changeWithAreaAndLine1: {}", changeWithAreaAndLine1);

        // Ensures order.
        Assert.assertEquals(changeWithAreaAndLine1, merged);
    }

    @Test
    public void testMergeSameEmpty()
    {
        final Change changeWithAreaAndLine1 = newChangeWithAreaAndLine();
        final Change changeWithAreaAndLine2 = newChangeWithAreaAndLine();

        final Change merged = Change.merge(changeWithAreaAndLine1, changeWithAreaAndLine2);

        Assert.assertEquals(changeWithAreaAndLine1, merged);
        Assert.assertEquals(changeWithAreaAndLine2, merged);
    }

    @Test
    public void testMergeSameChangeTypeAndItemType()
    {
        final int identifier1 = 1;

        final Change[] changes = IntStream.range(0, 3).boxed()
                .map(index -> newChangeWith2Areas(identifier1, 2, "access" + index, "private"))
                .toArray(Change[]::new);
        final Change mergedChange = Change.merge(changes);
        log.info("mergedChange: {}", mergedChange);
        Assert.assertNotNull(mergedChange);
        final FeatureChange[] mergedFeatureChanges = mergedChange.changes()
                .toArray(FeatureChange[]::new);
        Assert.assertEquals(2, mergedFeatureChanges.length);

        Assert.assertEquals(identifier1, mergedFeatureChanges[0].getIdentifier());
        Assert.assertEquals(3, mergedFeatureChanges[0].getTags().size());
    }
}
