package org.openstreetmap.atlas.exception.change;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author matthieun
 */
public class FeatureChangeMergeExceptionTest
{
    @Test
    public void testTruncate()
    {
        final String one = "abc";
        final StringBuilder twoBuilder = new StringBuilder();
        for (int index = 0; index < 2100; index++)
        {
            twoBuilder.append("a");
        }
        final String two = twoBuilder.toString();

        Assert.assertEquals(one, FeatureChangeMergeException.truncate(one));
        Assert.assertEquals(2100, two.length());
        Assert.assertEquals(FeatureChangeMergeException.MAXIMUM_MESSAGE_SIZE,
                FeatureChangeMergeException.truncate(two).length());
    }

    @Test
    public void testTruncateInConstructor()
    {
        final String one = "abc{}d";
        final StringBuilder twoBuilder = new StringBuilder();
        for (int index = 0; index < 2100; index++)
        {
            twoBuilder.append("a");
        }
        final String two = twoBuilder.toString();

        final FeatureChangeMergeException fcme = new FeatureChangeMergeException(
                MergeFailureType.HIGHEST_LEVEL_MERGE_FAILURE, one, two,
                new CoreException("I am the cause"));
        Assert.assertEquals(FeatureChangeMergeException.MAXIMUM_MESSAGE_SIZE,
                fcme.getMessage().length());
        Assert.assertEquals(CoreException.class, fcme.getCause().getClass());
    }
}
