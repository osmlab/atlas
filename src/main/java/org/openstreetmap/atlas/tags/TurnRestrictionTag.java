package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Tag for a turn restriction relation
 *
 * @author matthieun
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/restriction#values", osm = "http://wiki.openstreetmap.org/wiki/Relation:restriction")
public enum TurnRestrictionTag
{
    NO_RIGHT_TURN,
    NO_LEFT_TURN,
    NO_U_TURN,
    NO_STRAIGHT_ON,
    ONLY_RIGHT_TURN,
    ONLY_LEFT_TURN,
    ONLY_STRAIGHT_ON,
    NO_ENTRY,
    NO_EXIT;

    @TagKey
    public static final String KEY = "restriction";

    private static final EnumSet<TurnRestrictionTag> NO_PATH_RESTRICTIONS = EnumSet
            .of(NO_RIGHT_TURN, NO_LEFT_TURN, NO_U_TURN, NO_STRAIGHT_ON);
    private static final EnumSet<TurnRestrictionTag> ONLY_PATH_RESTRICTIONS = EnumSet
            .of(ONLY_RIGHT_TURN, ONLY_LEFT_TURN, ONLY_STRAIGHT_ON);

    public static boolean isNoPathRestriction(final Taggable taggable)
    {
        final Optional<TurnRestrictionTag> turnRestriction = Validators
                .from(TurnRestrictionTag.class, taggable);
        return turnRestriction.isPresent() && NO_PATH_RESTRICTIONS.contains(turnRestriction.get());
    }

    public static boolean isOnlyPathRestriction(final Taggable taggable)
    {
        final Optional<TurnRestrictionTag> turnRestriction = Validators
                .from(TurnRestrictionTag.class, taggable);
        return turnRestriction.isPresent()
                && ONLY_PATH_RESTRICTIONS.contains(turnRestriction.get());
    }

    public static boolean isRestriction(final Taggable taggable)
    {
        final Optional<TurnRestrictionTag> turnRestriction = Validators
                .from(TurnRestrictionTag.class, taggable);
        return turnRestriction.isPresent();
    }
}
