package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM building:height tag: https://taginfo.openstreetmap.org/keys/building%3Aheight#values. OSM
 * Wiki indicates that height is the standard key, but taginfo usage suggest prevalent use of
 * building:height as well
 *
 * @author isabellehillberg
 */
@Tag(value = Validation.LENGTH, taginfo = "https://taginfo.openstreetmap.org/keys/building%3Aheight#values", osm = "http://wiki.openstreetmap.org/wiki/Key:height")
public interface BuildingHeightTag
{
    @TagKey
    String KEY = "building:height";
}
