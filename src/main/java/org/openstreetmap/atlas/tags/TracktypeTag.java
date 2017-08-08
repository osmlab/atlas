package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM tracktype tag
 *
 * @author robert_stack
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/tracktype#values", osm = "http://wiki.openstreetmap.org/wiki/Key:tracktype")
public enum TracktypeTag
{
    GRADE1,
    GRADE2,
    GRADE3,
    GRADE4,
    GRADE5;

    @TagKey
    public static final String KEY = "tracktype";
}
