package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM area tag
 *
 * @author ihillberg
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/area#values", osm = "http://wiki.openstreetmap.org/wiki/Key:area")
public enum AreaTag
{
    YES,
    NO;

    @TagKey
    public static final String KEY = "area";
}
