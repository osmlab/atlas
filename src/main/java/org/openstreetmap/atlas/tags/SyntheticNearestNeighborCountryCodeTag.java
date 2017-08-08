package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Tag identifying when nearest-neighbor logic was used for country-code assignment. The presence of
 * the tag implies nearest-neighbor logic was used, while absence is the default case where it
 * wasn't. This is not an OSM tag.
 *
 * @author mgostintsev
 */
@Tag(synthetic = true)
public enum SyntheticNearestNeighborCountryCodeTag
{
    YES;

    @TagKey
    public static final String KEY = "nearest_neighbor_country_code";

    public static boolean isYes(final Taggable taggable)
    {
        return Validators.isOfType(taggable, SyntheticNearestNeighborCountryCodeTag.class, YES);
    }
}
