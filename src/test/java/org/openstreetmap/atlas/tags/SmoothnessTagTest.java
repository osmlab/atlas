package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link SmoothnessTag}.
 *
 * @author bbreithaupt
 */
public class SmoothnessTagTest
{

    @Test
    public void isLessImportantThanOrEqualToTest()
    {
        Assert.assertTrue(SmoothnessTag.BAD.isLessImportantThanOrEqualTo(SmoothnessTag.BAD));
        Assert.assertTrue(SmoothnessTag.BAD.isLessImportantThanOrEqualTo(SmoothnessTag.GOOD));
        Assert.assertFalse(SmoothnessTag.GOOD.isLessImportantThanOrEqualTo(SmoothnessTag.BAD));
    }

    @Test
    public void isLessImportantThanTest()
    {
        Assert.assertTrue(SmoothnessTag.BAD.isLessImportantThan(SmoothnessTag.GOOD));
        Assert.assertFalse(SmoothnessTag.GOOD.isLessImportantThan(SmoothnessTag.BAD));
    }

    @Test
    public void isMoreImportantThanOrEqualToTest()
    {
        Assert.assertTrue(SmoothnessTag.BAD.isMoreImportantThanOrEqualTo(SmoothnessTag.BAD));
        Assert.assertFalse(SmoothnessTag.BAD.isMoreImportantThanOrEqualTo(SmoothnessTag.GOOD));
        Assert.assertTrue(SmoothnessTag.GOOD.isMoreImportantThanOrEqualTo(SmoothnessTag.BAD));
    }

    @Test
    public void isMoreImportantThanTest()
    {
        Assert.assertFalse(SmoothnessTag.BAD.isMoreImportantThan(SmoothnessTag.GOOD));
        Assert.assertTrue(SmoothnessTag.GOOD.isMoreImportantThan(SmoothnessTag.BAD));
    }
}
