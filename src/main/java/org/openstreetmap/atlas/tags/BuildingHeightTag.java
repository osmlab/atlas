package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.geography.Altitude;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.extraction.AltitudeExtractor;

/**
 * OSM building:height tag: https://taginfo.openstreetmap.org/keys/building%3Aheight#values. OSM
 * Wiki indicates that height is the standard key, but taginfo usage suggest prevalent use of
 * building:height as well
 *
 * @author isabellehillberg
 * @author bbreithaupt
 */
@Tag(value = Validation.LENGTH, taginfo = "https://taginfo.openstreetmap.org/keys/building%3Aheight#values", osm = "http://wiki.openstreetmap.org/wiki/Key:height")
public interface BuildingHeightTag
{
    @TagKey
    String KEY = "building:height";

    static Optional<Altitude> get(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            return AltitudeExtractor.validateAndExtract(tagValue.get());
        }

        return Optional.empty();
    }
}
