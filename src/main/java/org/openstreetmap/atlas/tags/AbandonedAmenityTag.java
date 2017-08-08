package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM abandoned:amenity tag
 *
 * @author cstaylor
 */
@Tag(with = {
        AmenityTag.class }, taginfo = "http://taginfo.openstreetmap.org/keys/abandoned%3Aamenity#values", osm = "https://wiki.openstreetmap.org/wiki/Key:abandoned:")
public interface AbandonedAmenityTag
{
    @TagKey
    String KEY = "abandoned:amenity";
}
