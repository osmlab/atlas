package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagKey.KeyType;
import org.openstreetmap.atlas.tags.annotations.TagValue;
import org.openstreetmap.atlas.tags.annotations.TagValue.ValueType;

/**
 * OSM wikidata tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/wikidata#values", osm = "http://wiki.openstreetmap.org/wiki/Key:wikidata")
public interface WikidataTag
{
    @TagKey(KeyType.LOCALIZED)
    String KEY = "wikidata";

    @TagValue(ValueType.REGEX)
    String WIKI_DATA = "Q[1-9]\\d*";
}
