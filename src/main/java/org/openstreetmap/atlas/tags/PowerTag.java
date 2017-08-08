package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM power tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/power#values", osm = "http://wiki.openstreetmap.org/wiki/Key:power")
public enum PowerTag
{
    TOWER,
    POLE,
    LINE,
    GENERATOR,
    MINOR_LINE,
    SUBSTATION,
    SUB_STATION,
    TRANSFORMER,
    STATION,
    SWITCH,
    CABLE_DISTRIBUTION_CABINET,
    BUSBAR,
    PORTAL,
    CABLE,
    HELIOSTAT,
    CATENARY_MAST,
    PLANT,
    INSULATOR,
    SWITCHGEAR,
    COMPENSATOR,
    TERMINAL;

    @TagKey
    public static final String KEY = "power";

    public static Optional<PowerTag> get(final Taggable taggable)
    {
        return Validators.from(PowerTag.class, taggable);
    }
}
