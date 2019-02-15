package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author Yazad Khambata
 */
public class ChangeBuilderTest
{

    public static final long TEST_IDENTIFIER = 123L;

    @Test
    public void fluency()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(TEST_IDENTIFIER, null, Maps.hashMap(), null));

        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new CompleteLine(TEST_IDENTIFIER, null, Maps.hashMap(), null));

        final ChangeBuilder verboseBuilder = new ChangeBuilder();
        verboseBuilder.add(featureChange1);
        verboseBuilder.add(featureChange2);
        final Change verboseChange = verboseBuilder.get();

        final Change fluentChange = ChangeBuilder.newInstance().add(featureChange1)
                .add(featureChange2).get();

        Assert.assertEquals(verboseChange, fluentChange);
    }
}
