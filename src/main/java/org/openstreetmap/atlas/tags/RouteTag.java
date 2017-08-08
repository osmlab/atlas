package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM route tag
 *
 * @author robert_stack
 * @author matthieun
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/route#values", osm = "http://wiki.openstreetmap.org/wiki/Relation:route")
public enum RouteTag
{
    BICYCLE,
    BUS,
    CANOE,
    DETOUR,
    FERRY,
    FITNESS_TRAIL,
    HIKING,
    HORSE,
    INLINE_SKATES,
    LIGHT_RAIL,
    MTB,
    NORDIC_WALKING,
    PIPELINE,
    PISTE,
    POWER,
    RAILWAY,
    ROAD,
    RUNNING,
    SKI,
    TRAIN,
    TRAM;

    @TagKey
    public static final String KEY = "route";

    public static boolean isFerry(final Taggable taggable)
    {
        return Validators.isOfType(taggable, RouteTag.class, FERRY);
    }
}
