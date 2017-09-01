package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM elevation tag. This tag is used to express height above sea level of a point in meters.
 *
 * @author mgostintsev
 */
@Tag(value = Validation.DOUBLE, taginfo = "https://taginfo.openstreetmap.org/keys/ele#values", osm = "http://wiki.openstreetmap.org/wiki/Key:ele")
public interface ElevationTag
{
    @TagKey
    String KEY = "ele";
}
