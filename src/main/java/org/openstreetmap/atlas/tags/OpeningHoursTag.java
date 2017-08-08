package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValue;
import org.openstreetmap.atlas.tags.annotations.TagValue.ValueType;

/**
 * OSM opening_hours tag
 *
 * @author mcuthbert
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/opening_hours#values", osm = "http://wiki.openstreetmap.org/wiki/Key:opening_hours")
public interface OpeningHoursTag
{
    @TagKey
    String KEY = "opening_hours";

    @TagValue
    String ALL = "24/7";

    @TagValue(ValueType.REGEX)
    String TWENTY_FOUR_SEVEN = "24[/x]7";

    @TagValue(ValueType.REGEX)
    String DATE_AND_TIME = "(((Mo|Tu|We|Th|Fr|Sa|Su)(-?|,{0,6})){1,2}[_ ]?(([0-9]{1,2}:[0-9]{2}_?-_?[0-9]{1,2}:[0-9]{2}),? ?)+,?)+";

    @TagValue(ValueType.REGEX)
    String TIME = "(([0-9]{1,2}:[0-9]{2}_?-_?[0-9]{1,2}:[0-9]{2}),? ?)+";
}
