package org.openstreetmap.atlas.tags;

import java.util.EnumSet;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM Ferry Tag
 *
 * @author isabellehillberg
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/ferry#values", osm = "https://wiki.openstreetmap.org/wiki/Key:ferry")
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
    PEDESTRIAN,
    TRACK;

    private static final EnumSet<FerryTag> CAV_NAVIGABLE = EnumSet.of(MOTORWAY, TRUNK, PRIMARY,
            SECONDARY, TERTIARY, UNCLASSIFIED, RESIDENTIAL, SERVICE);
    private static final EnumSet<FerryTag> PEDESTRIAN_NAVIGABLE = EnumSet.of(FOOTWAY, TRACK,
            PEDESTRIAN);

    @TagKey
    public static final String KEY = "ferry";

    public static boolean isCarNavigableFerry(final FerryTag tag)
    {
        return CAV_NAVIGABLE.contains(tag);
    }

    public static boolean isPedestrianNavigableFerry(final FerryTag tag)
    {
        return PEDESTRIAN_NAVIGABLE.contains(tag);
    }
}
