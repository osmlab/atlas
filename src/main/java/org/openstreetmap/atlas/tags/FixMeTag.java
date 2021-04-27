package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM fix me tag. Check OSM Wiki below for more information.
 *
 * @author v-garei
 */
@Tag(value = Tag.Validation.NON_EMPTY_STRING, taginfo = "https://taginfo.openstreetmap.org/keys/fixme#values", osm = "https://wiki.openstreetmap.org/wiki/Key:fixme")
public interface FixMeTag
{
    @TagKey
    String KEY = "fixme";
}
