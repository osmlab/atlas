package org.openstreetmap.atlas.utilities.collections;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author tony
 * @author mgostintsev
 */
public class StringListTest
{
    @Test
    public void testSplit()
    {
        final String withTwoEquals = "key1=value1&key2=value2";
        final StringList fullySplit = StringList.split(withTwoEquals, "=");
        final StringList partialSplit1 = StringList.split(withTwoEquals, "=", 1);
        final StringList partialSplit2 = StringList.split(withTwoEquals, "=", 2);
        final StringList partialSplit3 = StringList.split(withTwoEquals, "=", 3);
        final StringList partialSplit4 = StringList.split(withTwoEquals, "=", 4);

        Assert.assertEquals(3, fullySplit.size());
        Assert.assertEquals(1, partialSplit1.size());
        Assert.assertEquals(withTwoEquals, partialSplit1.get(0));
        Assert.assertEquals(2, partialSplit2.size());
        Assert.assertEquals("value1&key2=value2", partialSplit2.get(1));
        Assert.assertEquals(3, partialSplit3.size());
        Assert.assertEquals(3, partialSplit4.size());
    }

    @Test
    public void testSplittingWhiteSpace()
    {
        final String withSpaces = "separated by spaces";
        final String withTabs = "separated  by  tabs";

        final StringList spacesSplitByRegex = StringList.splitByRegex(withSpaces, "\\s", 0);
        final StringList spacesFullySplit = StringList.split(withSpaces, "\\s", 0);
        final StringList tabsSplitByRegex = StringList.splitByRegex(withTabs, "\\s+", 0);
        final StringList tabsFullySplit = StringList.split(withTabs, "\\s+", 0);

        Assert.assertEquals(3, spacesSplitByRegex.size());
        Assert.assertEquals(1, spacesFullySplit.size());
        Assert.assertEquals(3, tabsSplitByRegex.size());
        Assert.assertEquals(1, tabsFullySplit.size());
    }

}
