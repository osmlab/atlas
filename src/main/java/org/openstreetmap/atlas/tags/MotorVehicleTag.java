package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM motor vehicle tag
 *
 * @author pmi
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/motor_vehicle#values", osm = "http://wiki.openstreetmap.org/wiki/Key:motor_vehicle")
public enum MotorVehicleTag
{
    YES,
    NO,
    PRIVATE,
    AGRICULTURAL,
    PERMISSIVE,
    DESTINATION,
    DESIGNATED,
    DELIVERY,
    FORESTRY,
    OFFICIAL,
    CUSTOMERS,
    EMERGENCY,
    BUS,
    SERVICE,
    UNKNOWN;

    @TagKey
    public static final String KEY = "motor_vehicle";
}
