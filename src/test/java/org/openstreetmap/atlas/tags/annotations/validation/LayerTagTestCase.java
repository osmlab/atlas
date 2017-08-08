package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.LayerTag;

/**
 * Test case for the LayerTag class
 *
 * @author cstaylor
 */
public class LayerTagTestCase extends BaseTagTestCase
{
    @Test
    public void layerNotZero()
    {
        Assert.assertFalse(validators().isValidFor(LayerTag.KEY, "0"));
    }

    @Test
    public void layersJustRight()
    {
        for (int loop = -5; loop < 0; loop++)
        {
            Assert.assertTrue(validators().isValidFor(LayerTag.KEY, String.valueOf(loop)));
        }

        for (int loop = 1; loop <= 5; loop++)
        {
            Assert.assertTrue(validators().isValidFor(LayerTag.KEY, String.valueOf(loop)));
        }
    }

    @Test
    public void layerTooHigh()
    {
        Assert.assertFalse(validators().isValidFor(LayerTag.KEY, "6"));
    }

    @Test
    public void layerTooLow()
    {
        Assert.assertFalse(validators().isValidFor(LayerTag.KEY, "-6"));
    }
}
