package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM Industrial Tag
 *
 * @author mgostintsev
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/industrial#values", osm = "http://wiki.openstreetmap.org/wiki/Key:industrial")
public enum IndustrialTag
{
    ALUMINUM_SMELTING,
    BAKERY,
    BREWERY,
    BRICKYARD,
    DEPOT,
    DISTRIBUTOR,
    FACTORY,
    FURNITURE,
    GAS,
    GRINDING_MILL,
    HEATING_STATION,
    ICE_FACTORY,
    MACHINE_SHOP,
    MINE,
    MOBILE_EQUIPMENT,
    OIL_MILL,
    OIL,
    PORT,
    SALT_POND,
    SAWMILL,
    SCRAP_YARD,
    SHIPYARD,
    SLAUGHTERHOUSE,
    STEELMAKING,
    WAREHOUSE,
    WELL_CLUSTER,
    WELLSITE;

    @TagKey
    public static final String KEY = "industrial";
}
