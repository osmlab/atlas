package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM disused:shop tag
 * <p>
 * Note how we're using the with optional parameter to Tag. This lets us bring in all of the values
 * defined in the ShopTag enum
 *
 * @author cstaylor
 */
@Tag(with = {
        ShopTag.class }, taginfo = "http://taginfo.openstreetmap.org/keys/disused%3Ashop#values", osm = "http://wiki.openstreetmap.org/wiki/Key:disused:")
public interface DisusedShopTag
{
    @TagKey
    String KEY = "disused:shop";
}
