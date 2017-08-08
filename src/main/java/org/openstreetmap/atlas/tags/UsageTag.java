package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM usage tag
 *
 * @author jacker
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/usage#values", osm = "http://wiki.openstreetmap.org/wiki/Key:usage")
public enum UsageTag
{
    MAIN,
    BRANCH,
    INDUSTRIAL,
    FREIGHT,
    TOURISM,
    MILITARY,
    YARD,
    STOCK,
    DISTRIBUTION,
    FACILITY,
    TRANSMISSION,
    TEST;

    @TagKey
    public static final String KEY = "usage";
}
