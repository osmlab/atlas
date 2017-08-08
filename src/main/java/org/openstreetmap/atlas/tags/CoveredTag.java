package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM Covered Tag
 *
 * @author cuthbertm
 */
@Tag(value = Tag.Validation.NON_EMPTY_STRING, taginfo = "https://taginfo.openstreetmap.org/keys/covered", osm = "http://wiki.openstreetmap.org/wiki/Key:covered")
public enum CoveredTag
{
    YES,
    NO,
    ARCADE,
    COLONNADE;

    @TagKey
    public static final String KEY = "covered";
}
