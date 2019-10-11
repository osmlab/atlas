package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.Taggable;

/**
 * Test case for the LayerTag class
 *
 * @author cstaylor
 */
public class LayerTagTestCase extends BaseTagTestCase
{
    @Test
    public void layerMaxValue()
    {
        Assert.assertEquals(LayerTag.getMaxValue(), 5);
        Assert.assertEquals(LayerTag.getMinValue(), -5);
    }

    @Test
    public void layerNotZero()
    {
        Assert.assertFalse(validators().isValidFor(LayerTag.KEY, "0"));
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
    public void taggablesOnDifferentLayer()
    {
        final Taggable taggableOne = Taggable.with("layer", "1");
        final Taggable taggableTwo = Taggable.with("layer", "2");
        Assert.assertFalse(LayerTag.areOnSameLayer(taggableOne, taggableTwo));
    }

    @Test
    public void taggablesOnSameLayer()
    {
        final Taggable taggableOne = Taggable.with("layer", "1");
        final Taggable taggableTwo = Taggable.with("layer", "2");
        final Taggable taggableThree = Taggable.with("layer", "0");
        final Taggable taggableFour = Taggable.with("highway", "primary");
        Assert.assertFalse(LayerTag.areOnSameLayer(taggableOne, taggableTwo));
        Assert.assertTrue(LayerTag.areOnSameLayer(taggableThree, taggableFour));
    }
}
