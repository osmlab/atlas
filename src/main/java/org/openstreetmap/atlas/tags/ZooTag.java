package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM zoo tag
 *
 * @author stephencerqueira
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/zoo#values", osm = "http://wiki.openstreetmap.org/wiki/Key%zoo")
public enum ZooTag
{
    AVIARY,
    BIRDS,
    ENCLOSURE,
    FALCONRY,
    PETTING_ZOO,
    REPTILE,
    SAFARI_PARK,
    WILDLIFE_PARK;

    @TagKey
    public static final String KEY = "zoo";
}
