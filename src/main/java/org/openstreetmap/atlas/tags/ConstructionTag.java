package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM Construction tag. Inherits values from Bridge, Highway, LandUse and Railway tags.
 *
 * @author bbreithaupt
 */
@Tag(with = { BuildingTag.class, HighwayTag.class, LandUseTag.class,
        RailwayTag.class }, taginfo = "https://taginfo.openstreetmap.org/keys/construction", osm = "https://wiki.openstreetmap.org/wiki/Key:construction")
@SuppressWarnings("squid:S1214")
public interface ConstructionTag
{
    @TagKey
    String KEY = "construction";
}
