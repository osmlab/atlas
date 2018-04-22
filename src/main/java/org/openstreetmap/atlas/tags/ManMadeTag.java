package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM man_made tag
 *
 * @author mgostintsev
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/man_made#values", osm = "http://wiki.openstreetmap.org/wiki/Key:man_made")
public enum ManMadeTag
{
    ADIT,
    BEACON,
    BREAKWATER,
    BRIDGE,
    BUNKER_SILO,
    CAMPANILE,
    CHIMNEY,
    COMMUNICATIONS_TOWER,
    CRANE,
    CROSS,
    CUTLINE,
    CLEARCUT,
    EMBANKMENT,
    DYKE,
    FLAGPOLE,
    GASOMETER,
    GROYNE,
    HOT_WATER_TANK,
    KILN,
    LIGHTHOUSE,
    MAST,
    MINESHAFT,
    MONITORING_STATION,
    OBSERVATORY,
    OFFSHORE_PLATFORM,
    PETROLEUM_WELL,
    PIER,
    PIPELINE,
    PUMPING_STATION,
    RESERVOIR_COVERED,
    SILO,
    SNOW_FENCE,
    SNOW_NET,
    STORAGE_TANK,
    STREET_CABINET,
    SURVEILLANCE,
    SURVEY_POINT,
    TELESCOPE,
    TOWER,
    WASTEWATER_PLANT,
    WATERMILL,
    WATER_TOWER,
    WATER_WELL,
    WATER_TAP,
    WATER_WORKS,
    WILDLIFE_CROSSING,
    WINDMILL,
    WORKS;

    @TagKey
    public static final String KEY = "man_made";

    public static boolean isPier(final Taggable taggable)
    {
        return Validators.isOfType(taggable, ManMadeTag.class, PIER);
    }
}
