package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM aeroway tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/aeroway#values", osm = "http://wiki.openstreetmap.org/wiki/Aeroways")
public enum AerowayTag
{
    TAXIWAY,
    AERODROME,
    RUNWAY,
    HELIPAD,
    APRON,
    HANGAR,
    GATE,
    PARKING_POSITION,
    TERMINAL,
    HOLDING_POSITION,
    WINDSOCK,
    NAVIGATIONAID,
    MARKING;

    @TagKey
    public static final String KEY = "aeroway";

    public static Optional<AerowayTag> get(final Taggable taggable)
    {
        return Validators.from(AerowayTag.class, taggable);
    }
}
