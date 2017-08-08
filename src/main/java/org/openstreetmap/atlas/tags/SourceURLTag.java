package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM source_url tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.URI, taginfo = "http://taginfo.openstreetmap.org/keys/source%3Aurl#values")
public interface SourceURLTag
{
    @TagKey
    String KEY = "source:url";
}
