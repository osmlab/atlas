package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Range;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM width tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.DOUBLE, range = @Range(min = 0, max = Integer.MAX_VALUE), taginfo = "http://taginfo.openstreetmap.org/keys/width#values", osm = "http://wiki.openstreetmap.org/wiki/Key:width")
public interface WidthTag
{
    @TagKey
    String KEY = "width";
}
