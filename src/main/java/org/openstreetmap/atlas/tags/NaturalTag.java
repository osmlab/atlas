package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM natural tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/natural#values", osm = "http://wiki.openstreetmap.org/wiki/Natural")
public enum NaturalTag
{
    WATER,
    TREE,
    WOOD,
    WETLAND,
    SCRUB,
    COASTLINE,
    PEAK,
    CLIFF,
    GRASSLAND,
    TREE_ROW,
    HEATH,
    ROCK,
    BARE_ROCK,
    BEACH,
    SAND,
    SPRING,
    HOT_SPRING,
    GEYSER,
    LAND,
    BAY,
    SCREE,
    RIDGE,
    GLACIER,
    CAVE_ENTRANCE,
    SADDLE,
    MARSH,
    FELL,
    REEF,
    MUD,
    STONE,
    LANDFORM,
    SHINGLE,
    VALLEY,
    CAPE,
    VOLCANO,
    CREVASSE,
    SINKHOLE;

    @TagKey
    public static final String KEY = "natural";

    public static Optional<NaturalTag> get(final Taggable taggable)
    {
        return Validators.from(NaturalTag.class, taggable);
    }
}
