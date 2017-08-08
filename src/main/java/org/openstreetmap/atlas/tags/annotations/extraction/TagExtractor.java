package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;

/**
 * Extracts the tag value from the textual representation and associated {@link Tag}.
 *
 * @param <T>
 *            The extracted value type
 * @author mgostintsev
 */
public interface TagExtractor<T>
{
    /**
     * Validates and extracts the value from given textual representation
     *
     * @param value
     *            The textual representation of this tag's value
     * @param tag
     *            The associated {@link Tag}
     * @return the {@link Optional} containing the extracted value
     */
    Optional<T> validateAndExtract(String value, Tag tag);
}
