package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM landuse tag
 *
 * @author mgostintsev
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/landuse#values", osm = "http://wiki.openstreetmap.org/wiki/Landuse")
public enum LandUseTag
{
    ALLOTMENTS,
    AQUACULTURE,
    BASIN,
    BROWNFIELD,
    CEMETERY,
    COMMERCIAL,
    CONSTRUCTION,
    FARMLAND,
    FARMYARD,
    FOREST,
    GARAGES,
    GRASS,
    GREENFIELD,
    GREENHOUSE_HORTICULTURE,
    INDUSTRIAL,
    LANDFILL,
    MEADOW,
    MILITARY,
    ORCHARD,
    PEAT_CUTTING,
    PLANT_NURSERY,
    PORT,
    QUARRY,
    RAILWAY,
    RECREATION_GROUND,
    RESERVOIR,
    RESIDENTIAL,
    RETAIL,
    SALT_POND,
    VILLAGE_GREEN,
    VINEYARD,
    POND;

    @TagKey
    public static final String KEY = "landuse";
}
