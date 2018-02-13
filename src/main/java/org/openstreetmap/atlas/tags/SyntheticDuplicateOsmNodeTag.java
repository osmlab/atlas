package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Tag identifying an OSM Node that contains duplicate Node on top of it. This usually signifies a
 * data error. This is NOT an OSM tag.
 *
 * @author mgostintsev
 */
@Tag(synthetic = true)
public enum SyntheticDuplicateOsmNodeTag
{
    YES;

    @TagKey
    public static final String KEY = "synthetic_duplicate_osm_node";

    public static boolean isYes(final Taggable taggable)
    {
        return Validators.isOfType(taggable, SyntheticDuplicateOsmNodeTag.class, YES);
    }
}
