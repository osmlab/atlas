package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM foot tag
 *
 * @author robert_stack
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/foot#values", osm = "http://wiki.openstreetmap.org/wiki/Key:foot")
public enum FootTag
{
    YES,
    NO,
    DESIGNATED,
    OFFICIAL,
    PRIVATE,
    PERMISSIVE,
    DESTINATION,
    USE_SIDEPATH,
    CUSTOMERS,
    UNKNOWN;

    private static final EnumSet<FootTag> PRIVATE_ACCESS = EnumSet.of(NO, PRIVATE);
    private static final EnumSet<FootTag> PEDESTRIAN_ACCESS = EnumSet.of(YES, DESIGNATED, OFFICIAL,
            PERMISSIVE, DESTINATION);

    @TagKey
    public static final String KEY = "foot";

    public static boolean isPedestrianAccessible(final Taggable taggable)
    {
        final Optional<FootTag> foot = Validators.from(FootTag.class, taggable);
        return foot.isPresent() && PEDESTRIAN_ACCESS.contains(foot.get());
    }

    public static boolean isPrivate(final Taggable taggable)
    {
        final Optional<FootTag> foot = Validators.from(FootTag.class, taggable);
        return foot.isPresent() && PRIVATE_ACCESS.contains(foot.get());
    }
}
