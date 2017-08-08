package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM toll tag
 *
 * @author robert_stack
 * @author pmi
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/?key=toll#values", osm = "http://wiki.openstreetmap.org/wiki/Key:toll")
public enum TollTag
{
    YES,
    NO,
    SNOWMOBILE;

    @TagKey
    public static final String KEY = "toll";

    public static boolean isYes(final Taggable taggable)
    {
        final Taggable checkMe = taggable;
        return Validators.isOfType(checkMe, TollTag.class, TollTag.YES);
    }
}
