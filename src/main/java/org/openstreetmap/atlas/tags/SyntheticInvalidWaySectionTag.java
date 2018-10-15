package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Tag identifying an Atlas Edge that was the remnant of way-sectioning that exceeded the maximum
 * 999 slices. As a result, this edge contains the rest of the un-sectioned OSM Way. This usually
 * indicates a data error and is NOT an OSM tag.
 *
 * @author mgostintsev
 */
@Tag(synthetic = true)
public enum SyntheticInvalidWaySectionTag
{
    YES;

    @TagKey
    public static final String KEY = "synthetic_invalid_way_section";

    public static boolean isYes(final Taggable taggable)
    {
        return Validators.isOfType(taggable, SyntheticInvalidWaySectionTag.class, YES);
    }
}
