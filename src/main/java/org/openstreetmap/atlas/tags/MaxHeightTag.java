package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValue;
import org.openstreetmap.atlas.tags.annotations.TagValue.ValueType;

/**
 * OSM maxheight tag: http://taginfo.openstreetmap.org/keys/maxheight#values
 *
 * @author cstaylor
 */
@Tag(value = Validation.DOUBLE, taginfo = "http://taginfo.openstreetmap.org/keys/maxheight#values", osm = "http://wiki.openstreetmap.org/wiki/Key:maxheight")
public interface MaxHeightTag
{
    @TagKey
    String KEY = "maxheight";

    @TagValue
    String DEFAULT = "default";

    @TagValue
    String NONE = "none";

    @TagValue(ValueType.REGEX)
    String METERS = "(\\d+(\\.\\d+)?|\\.\\d+)(\\sm)?";

    @TagValue(ValueType.REGEX)
    String FEET = "\\d'\\d\"";
}
