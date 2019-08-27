package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * This tag denotes roads that are only navigable by vehicles with Four wheel drive
 * 
 * @author kkonishi2
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/?key=4wd_only", osm = "https://wiki.openstreetmap.org/wiki/Key:4wd_only")
public enum FourWheelDriveOnlyTag
{
    YES,
    NO;

    @TagKey
    public static final String KEY = "4wd_only";
}
