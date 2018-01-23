package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM Public Service Vehicles (psv) tag
 *
 * @author mgostintsev
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/psv#values", osm = "http://wiki.openstreetmap.org/wiki/Key:psv")
public enum PublicServiceVehiclesTag
{
    YES,
    NO,
    BUS,
    OFFICIAL,
    DESIGNATED;

    @TagKey
    public static final String KEY = "psv";
}
