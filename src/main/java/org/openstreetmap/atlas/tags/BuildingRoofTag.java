package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValueAs;

/**
 * OSM building:roof tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/building%3Aroof#values", osm = "http://wiki.openstreetmap.org/wiki/Proposed_features/Building_attributes")
public enum BuildingRoofTag
{
    TILE,
    TRADITIONAL,
    FLAT,
    CONCRETE,
    PERMANENT,
    TIN,
    METAL,
    TILES,
    SLATE,
    @TagValueAs("semi-permanent")
    SEMI_PERMANENT,
    ASBESTOS,
    PITCHED,
    IRONSHEETS,
    HIPPED,
    GABLED;

    @TagKey
    public static final String KEY = "building:roof";
}
