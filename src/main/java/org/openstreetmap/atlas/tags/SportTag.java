package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM sports tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/sport#values")
public enum SportTag
{
    TENNIS,
    GOLF,
    SKI,
    SOCCER,
    BILLIARD,
    MULTI,
    BADMINTON,
    EQUESTRIAN,
    RUNNING,
    ATHLETICS,
    MODEL_AERODROME,
    MOTOR,
    FISHING,
    WINGSTUN,
    HOCKEY,
    SWIMMING;

    @TagKey
    public static final String KEY = "sport";

    public static Optional<SportTag> get(final Taggable taggable)
    {
        return Validators.from(SportTag.class, taggable);
    }
}
