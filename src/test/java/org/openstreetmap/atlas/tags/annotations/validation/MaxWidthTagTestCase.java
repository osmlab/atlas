package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.MaxWidthTag;

/**
 * Test cases for the maxwidth tag
 *
 * @author cstaylor
 */
public class MaxWidthTagTestCase extends BaseTagTestCase
{
    @Test
    public void englishSystem()
    {
        Assert.assertTrue(validators().isValidFor(MaxWidthTag.KEY, "5\'9\""));
    }

    @Test
    public void garbage()
    {
        Assert.assertFalse(validators().isValidFor(MaxWidthTag.KEY, "nope"));
    }

    @Test
    public void justAFractionalNumber()
    {
        Assert.assertTrue(validators().isValidFor(MaxWidthTag.KEY, "3"));
    }

    @Test
    public void justANumber()
    {
        Assert.assertTrue(validators().isValidFor(MaxWidthTag.KEY, "3.5"));
    }

    @Test
    public void withMeters()
    {
        Assert.assertTrue(validators().isValidFor(MaxWidthTag.KEY, "3.5 m"));
    }
}
