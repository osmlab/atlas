package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValueAs;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM's surface tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/surface#values", osm = "http://wiki.openstreetmap.org/wiki/Key:surface")
public enum SurfaceTag
{
    ASPHALT,
    UNPAVED,
    PAVED,
    GRAVEL,
    GROUND,
    DIRT,
    GRASS,
    CONCRETE,
    PAVING_STONES,
    SAND,
    COMPACTED,
    COBBLESTONE,
    WOOD,
    FINE_GRAVEL,
    EARTH,
    PEBBLESTONE,
    SETT,
    MUD,
    GRASS_PAVER,
    METAL,
    GRAVEL_TURF,
    ICE,
    SALT,
    SNOW,
    WOODCHIPS,
    TARTAN,
    ARTIFICIAL_TURF,
    DECOTURF,
    CLAY,
    METAL_GRID,
    @TagValueAs("cobblestone:flattened")
    COBBLESTONE_FLATTENED,
    @TagValueAs("concrete:lanes")
    CONCRETE_LANES,
    @TagValueAs("concrete:plates")
    CONCRETE_PLATES;

    @TagKey
    public static final String KEY = "surface";

    public static Optional<SurfaceTag> get(final Taggable taggable)
    {
        return Validators.from(SurfaceTag.class, taggable);
    }
}
