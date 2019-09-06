package org.openstreetmap.atlas.exception.change;

import org.junit.Assert;
import org.junit.Test;

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
}
