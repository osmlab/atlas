package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

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

    public static boolean isCarNavigableFerry(final Taggable taggable)
    {
        final Optional<FerryTag> ferry = Validators.from(FerryTag.class, taggable);
        return ferry.isPresent() && CAV_NAVIGABLE.contains(ferry);
    }

    public static boolean isPedestrianNavigableFerry(final Taggable taggable)
    {
        final Optional<FerryTag> ferry = Validators.from(FerryTag.class, taggable);
        return ferry.isPresent() && PEDESTRIAN_NAVIGABLE.contains(ferry);
    }
}
