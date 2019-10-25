package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.Taggable;

/**
 * Unit tests for {@link LayerExtractor}.
 *
 * @author bbreithaupt
 */
public class LayerExtractorTest
{
    @Test
    public void invalidLayerTest()
    {
        Assert.assertFalse(
                LayerExtractor.validateAndExtract(Taggable.with("layer", "one")).isPresent());
    }

    @Test
    public void invalidTooLargeLayerTest()
    {
        Assert.assertFalse(
                LayerExtractor.validateAndExtract(Taggable.with("layer", "6")).isPresent());
    }

    @Test
    public void validBridgeTest()
    {
        Assert.assertEquals(Optional.of(1L),
                LayerExtractor.validateAndExtract(Taggable.with("bridge", "yes")));
    }

    @Test
    public void validDefaultTest()
    {
        Assert.assertEquals(Optional.of(0L), LayerExtractor.validateAndExtract(Taggable.with()));
    }

    @Test
    public void validLayerBridgeTunnelTest()
    {
        Assert.assertEquals(Optional.of(3L), LayerExtractor
                .validateAndExtract(Taggable.with("layer", "3", "bridge", "yes", "tunnel", "yes")));
    }

    @Test
    public void validLayerTest()
    {
        Assert.assertEquals(Optional.of(3L),
                LayerExtractor.validateAndExtract(Taggable.with("layer", "3")));
    }

    @Test
    public void validTunnelTest()
    {
        Assert.assertEquals(Optional.of(-1L),
                LayerExtractor.validateAndExtract(Taggable.with("tunnel", "yes")));
    }
}
