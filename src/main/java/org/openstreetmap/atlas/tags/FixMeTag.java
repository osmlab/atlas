package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM fixme tag: http://taginfo.openstreetmap.org/keys/fixme#values
 *
 * @author v-garei
 */
@Tag(value = Tag.Validation.LENGTH, taginfo = "https://taginfo.openstreetmap.org/keys/fixme#values", osm = "https://wiki.openstreetmap.org/wiki/Key:fixme")
public class FixMeTag
{

    @TagKey
    private static final String KEY = "fixme";
}
