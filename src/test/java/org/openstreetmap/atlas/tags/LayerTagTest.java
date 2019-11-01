package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link LayerTag}.
 *
 * @author bbreithaupt
 */
public class LayerTagTest
{
    @Test
    public void validBridgeTest()
    {
        Assert.assertEquals((Long) 1L,
                LayerTag.getTaggedOrImpliedValue(Taggable.with("bridge", "yes"), LayerTag.ZERO));
    }

    @Test
    public void validDefaultTest()
    {
        Assert.assertEquals((Long) 0L,
                LayerTag.getTaggedOrImpliedValue(Taggable.with(), LayerTag.ZERO));
    }

    @Test
    public void validLayerBridgeTunnelTest()
    {
        Assert.assertEquals((Long) 3L, LayerTag.getTaggedOrImpliedValue(
                Taggable.with("layer", "3", "bridge", "yes", "tunnel", "yes"), LayerTag.ZERO));
    }

    @Test
    public void validLayerTest()
    {
        Assert.assertEquals((Long) 3L,
                LayerTag.getTaggedOrImpliedValue(Taggable.with("layer", "3"), LayerTag.ZERO));
    }

    @Test
    public void validTunnelTest()
    {
        Assert.assertEquals((Long) (-1L),
                LayerTag.getTaggedOrImpliedValue(Taggable.with("tunnel", "yes"), LayerTag.ZERO));
    }
}
