package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM minspeed tag
 *
 * @author mgostintsev
 */
@Tag(value = Validation.SPEED, taginfo = "http://taginfo.openstreetmap.org/keys/minspeed#values", osm = "http://wiki.openstreetmap.org/wiki/Key:minspeed")
public interface MinSpeedTag
{
    @TagKey
    String KEY = "minspeed";

    static boolean hasMinSpeed(final Taggable taggable)
    {
        return taggable.getTag(KEY).isPresent();
    }

}
