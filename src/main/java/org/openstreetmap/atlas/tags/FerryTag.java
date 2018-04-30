package org.openstreetmap.atlas.tags;

import java.util.EnumSet;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM Ferry Tag
 *
 * @author isabellehillberg
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/ferry#values")
public enum FerryTag
{
    YES,
    NO,
    MOTORWAY,
    TRUNK,
    PRIMARY,
    SECONDARY,
    TERTIARY,
    UNCLASSIFIED,
    RESIDENTIAL,
    SERVICE,
    FOOTWAY,
    PEDESTRIAN;

    private static final EnumSet<FerryTag> CAV_NAVIGABLE = EnumSet.of(MOTORWAY, TRUNK, PRIMARY,
            SECONDARY, TERTIARY, UNCLASSIFIED, RESIDENTIAL, SERVICE);

    @TagKey
    public static final String KEY = "ferry";

    public static boolean isCarNavigableHighway(final HighwayTag tag)
    {
        return CAV_NAVIGABLE.contains(tag);
    }
}
