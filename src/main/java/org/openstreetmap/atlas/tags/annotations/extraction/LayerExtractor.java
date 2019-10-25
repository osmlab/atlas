package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.Optional;

import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.TunnelTag;

/**
 * Extracts layer values.
 *
 * @author bbreithaupt
 */
public final class LayerExtractor
{
    /**
     * Extracts layer values as {@link Optional} {@link Long}s based on the {@link LayerTag},
     * {@link BridgeTag}, and {@link TunnelTag}s. Defaults to 0 if no layer value is found.
     *
     * @param taggable
     *            {@link Taggable} to get a value for
     * @return {@link Optional} {@link Long} layer value. Will return empty if taggable has a
     *         {@link LayerTag} that is invalid.
     */
    public static Optional<Long> validateAndExtract(final Taggable taggable)
    {
        final Optional<Long> layerValue = LayerTag.getTaggedValue(taggable);

        // Return empty if there is a layer tag but it has a bad value
        if (!layerValue.isPresent() && taggable.getTag(LayerTag.KEY).isPresent())
        {
            return Optional.empty();
        }
        // Else return the layer tag if there is one
        if (layerValue.isPresent())
        {
            return layerValue;
        }
        // Else return 1 if taggable is a bridge
        if (BridgeTag.isBridge(taggable))
        {
            return Optional.of(1L);
        }
        // Else return -1 if taggable is a tunnel
        if (TunnelTag.isTunnel(taggable))
        {
            return Optional.of(-1L);
        }
        // Else return the default layer value of 0
        return Optional.of(0L);
    }

    private LayerExtractor()
    {
    }
}
