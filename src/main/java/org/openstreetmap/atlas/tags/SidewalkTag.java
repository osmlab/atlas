package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM sidewalk tag
 *
 * @author isabellehillberg
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/sidewalk#values", osm = "http://wiki.openstreetmap.org/wiki/Key:sidewalk")
public enum SidewalkTag
{
    BOTH,
    NONE,
    NO,
    RIGHT,
    LEFT,
    SEPARATE,
    YES;

    @TagKey
    public static final String KEY = "sidewalk";

    public static boolean isNo(final Taggable taggable)
    {
        return Validators.isOfType(taggable, SidewalkTag.class, SidewalkTag.NO, SidewalkTag.NONE);
    }

    public static boolean isYes(final Taggable taggable)
    {
        return Validators.isOfType(taggable, SidewalkTag.class, SidewalkTag.BOTH, SidewalkTag.RIGHT,
                SidewalkTag.LEFT, SidewalkTag.SEPARATE, SidewalkTag.YES);
    }
}
