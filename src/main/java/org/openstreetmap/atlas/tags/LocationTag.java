package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;



/**
 * OSM location tag
 *
 * @author mkalender
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/location#values", osm = "http://wiki.openstreetmap.org/wiki/Key:location")
public enum LocationTag
{
    INDOOR,
    KIOSK,
    OUTDOOR,
    OVERGROUND,
    OVERWATER,
    PLATFORM,
    ROOF,
    ROOFTOP,
    UNDERGROUND,
    UNDERWATER;

    @TagKey
    public static final String KEY = "location";

    public static Optional<LocationTag> get(final Taggable taggable)
    {
        return Validators.from(LocationTag.class, taggable);
    }
}
