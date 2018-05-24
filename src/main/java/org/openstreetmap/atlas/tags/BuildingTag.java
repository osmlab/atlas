package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM building tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/building#values", osm = "http://wiki.openstreetmap.org/wiki/Buildings")
public enum BuildingTag
{
    YES,
    RESIDENTIAL,
    COMMERCIAL,
    SHOP,
    HOUSE,
    GARAGE,
    APARTMENTS,
    HUT,
    INDUSTRIAL,
    DETACHED,
    ROOF,
    SHED,
    TERRACE,
    SCHOOL,
    RETAIL,
    FARM_AUXILIARY,
    CHURCH,
    BARN,
    CONSTRUCTION,
    GREENHOUSE,
    SERVICE,
    MANUFACTURE,
    CABIN,
    FARM,
    WAREHOUSE,
    CIVIC,
    COLLAPSED,
    OFFICE,
    NO,
    UNIVERSITY,
    HOTEL,
    DORMITORY,
    BUNGALOW,
    STATIC_CARAVAN,
    CHAPEL,
    MOSQUE,
    KINDERGARTEN,
    HOSPITAL,
    STADIUM,
    TRAIN_STATION,
    TRANSPORTATION,
    PUBLIC,
    BUNKER,
    GARAGES,
    HANGAR,
    STABLE,
    TRANSFORMER_TOWER,
    RUINS;

    private static EnumSet<BuildingTag> VALID_BUILDINGS = EnumSet.of(YES, RESIDENTIAL, COMMERCIAL,
            SHOP, HOUSE, GARAGE, APARTMENTS, HUT, INDUSTRIAL, DETACHED, ROOF, SHED, TERRACE, SCHOOL,
            RETAIL, FARM_AUXILIARY, CHURCH, BARN, CONSTRUCTION, GREENHOUSE, SERVICE, MANUFACTURE,
            CABIN, FARM, WAREHOUSE, CIVIC, COLLAPSED, OFFICE, UNIVERSITY, HOTEL, DORMITORY,
            BUNGALOW, STATIC_CARAVAN, CHAPEL, MOSQUE, KINDERGARTEN, HOSPITAL, STADIUM,
            TRAIN_STATION, TRANSPORTATION, PUBLIC, BUNKER, GARAGES, HANGAR, STABLE,
            TRANSFORMER_TOWER, RUINS);

    @TagKey
    public static final String KEY = "building";

    public static final String BUILDING_ROLE_OUTLINE = "outline";

    public static final String BUILDING_ROLE_PART = "part";

    public static boolean isBuilding(final String value)
    {
        return isBuilding(taggable -> Optional.of(value));
    }

    public static boolean isBuilding(final Taggable taggable)
    {
        final Optional<BuildingTag> building = Validators.from(BuildingTag.class, taggable);
        return building.isPresent() && VALID_BUILDINGS.contains(building.get());
    }
}
