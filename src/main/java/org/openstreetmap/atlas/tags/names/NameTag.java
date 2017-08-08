package org.openstreetmap.atlas.tags.names;

import java.util.Optional;

import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagKey.KeyType;
import org.openstreetmap.atlas.tags.annotations.extraction.NonEmptyStringExtractor;

/**
 * OSM name tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/name#values", osm = "http://wiki.openstreetmap.org/wiki/Key:name")
public interface NameTag
{
    @TagKey(KeyType.LOCALIZED)
    String KEY = "name";

    static Optional<String> getNameOf(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            return NonEmptyStringExtractor.validateAndExtract(tagValue.get());
        }

        return Optional.empty();
    }
}
