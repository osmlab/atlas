package org.openstreetmap.atlas.geography.atlas.complete;

import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertEquals(one, new CompleteArea(1L, null, null, null).truncate(one));
        Assert.assertEquals(2100, two.length());
        Assert.assertEquals(CompleteEntity.TRUNCATE_LENGTH,
                new CompleteArea(1L, null, null, null).truncate(two).length());
    }
}
