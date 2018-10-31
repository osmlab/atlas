package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM Ford Tag.
 *
 * @author sayas01
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/ford#values", osm = "https://wiki.openstreetmap.org/wiki/Key:ford")
public enum FordTag
{
    YES,
    STEPPING_STONES,
    SEASONAL,
    NO,
    STREAM,
    LEVEL_CROSSING,
    BOAT;

    @TagKey
    public static final String KEY = "ford";

    public static boolean isYes(final Taggable taggable)
    {
        return Validators.isOfType(taggable, FordTag.class, FordTag.YES);
    }
}
