package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValue;
import org.openstreetmap.atlas.tags.annotations.TagValue.ValueType;

/**
 * OSM maxwidth tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/maxwidth#values", osm = "http://wiki.openstreetmap.org/wiki/Key:maxwidth")
public interface MaxWidthTag
{
    @TagKey
    String KEY = "maxwidth";

    @TagValue(ValueType.REGEX)
    String METERS = "(\\d+(\\.\\d+)?|\\.\\d+)(\\sm)?";

    @TagValue(ValueType.REGEX)
    String FEET = "\\d'\\d\"";
}
