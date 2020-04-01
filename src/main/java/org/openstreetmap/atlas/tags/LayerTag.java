package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Range;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.extraction.LongExtractor;

/**
 * OSM layer tag
 *
 * @author cstaylor
 * @author brian_l_davis
 * @author bbreithaupt
 */
@Tag(value = Validation.LONG, range = @Range(min = -5, max = 5, exclude = {
        0 }), taginfo = "http://taginfo.openstreetmap.org/keys/layer#values", osm = "http://wiki.openstreetmap.org/wiki/Layer")
public interface LayerTag
{
    Long ZERO = 0L;
    @TagKey
    String KEY = "layer";

    /**
     * Checks if two Taggable objects are on the same layer or not. According to OSM wiki, objects
     * with no explicit LayerTag are assumed to have layer 0.
     *
     * @param taggableOne
     *            first object to compare
     * @param taggableTwo
     *            second object to compare
     * @return true if the two objects have same layer tag, false otherwise
     */
    static boolean areOnSameLayer(final Taggable taggableOne, final Taggable taggableTwo)
    {
        return LayerTag.getTaggedOrImpliedValue(taggableOne, ZERO)
                .equals(LayerTag.getTaggedOrImpliedValue(taggableTwo, ZERO));
    }

    static long getMaxValue()
    {
        return LayerTag.class.getDeclaredAnnotation(Tag.class).range().max();
    }

    static long getMinValue()
    {
        return LayerTag.class.getDeclaredAnnotation(Tag.class).range().min();
    }

    static Long getTaggedOrImpliedValue(final Taggable taggable, final Long impliedValue)
    {
        final Optional<Long> taggedValue = getTaggedValue(taggable);
        // Return the layer tag if there is one
        if (taggedValue.isPresent())
        {
            return taggedValue.get();
        }
        // Else return 1 if taggable is a bridge
        if (BridgeTag.isBridge(taggable))
        {
            return 1L;
        }
        // Else return -1 if taggable is a tunnel
        if (TunnelTag.isTunnel(taggable))
        {
            return -1L;
        }
        // Else return the implied value
        return impliedValue;
    }

    static Optional<Long> getTaggedValue(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            final LongExtractor extractor = new LongExtractor();
            return extractor.validateAndExtract(tagValue.get(),
                    LayerTag.class.getDeclaredAnnotation(Tag.class));
        }
        return Optional.empty();
    }
}
