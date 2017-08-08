package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.MaxHeightTag;

/**
 * Test cases for the maxheight tag
 *
 * @author cstaylor
 */
public class MaxHeightTagTestCase extends BaseTagTestCase
{
    @Test
    public void englishSystem()
    {
        Assert.assertTrue(validators().isValidFor(MaxHeightTag.KEY, "5\'9\""));
    }

    @Test
    public void garbage()
    {
        Assert.assertFalse(validators().isValidFor(MaxHeightTag.KEY, "nope"));
    }

    @Test
    public void justAFractionalNumber()
    {
        Assert.assertTrue(validators().isValidFor(MaxHeightTag.KEY, "3"));
    }

    @Test
    public void justANumber()
    {
        Assert.assertTrue(validators().isValidFor(MaxHeightTag.KEY, "3.5"));
    }

    @Test
    public void withMeters()
    {
        Assert.assertTrue(validators().isValidFor(MaxHeightTag.KEY, "3.5 m"));
    }
}
