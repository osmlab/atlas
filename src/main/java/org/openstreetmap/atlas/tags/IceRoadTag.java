package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM ice_road tag.
 *
 * @author jacker
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/ice_road#values", osm = "https://wiki.openstreetmap.org/wiki/Key:ice_road")
public enum IceRoadTag
{
    YES,
    SNOWMOBILE,
    NO;

    @TagKey
    public static final String KEY = "ice_road";

    public static boolean isYes(final Taggable taggable)
    {
        return Validators.isOfType(taggable, IceRoadTag.class, IceRoadTag.YES,
                IceRoadTag.SNOWMOBILE);
    }
}
