package org.openstreetmap.atlas.geography.atlas.complete;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class CompleteEntityTest
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

        Assert.assertEquals(one + PrettifyStringFormat.TRUNCATE_ELLIPSES,
                new CompleteArea(1L, null, null, null).truncate(one));
        Assert.assertEquals(2100, two.length());
        Assert.assertEquals(
                PrettifyStringFormat.TRUNCATE_LENGTH
                        + PrettifyStringFormat.TRUNCATE_ELLIPSES.length(),
                new CompleteArea(1L, null, null, null).truncate(two).length());
    }
}
