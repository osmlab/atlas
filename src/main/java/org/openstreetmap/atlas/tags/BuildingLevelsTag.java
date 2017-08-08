package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM building:levels tag
 *
 * @author robert_stack
 */
@Tag(value = Validation.ORDINAL, taginfo = "http://taginfo.openstreetmap.org/keys/building:levels#values", osm = "http://wiki.openstreetmap.org/wiki/Key:building:levels")
public interface BuildingLevelsTag
{
    @TagKey
    String KEY = "building:levels";
}
