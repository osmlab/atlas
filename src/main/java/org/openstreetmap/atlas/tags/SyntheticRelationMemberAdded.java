package org.openstreetmap.atlas.tags;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * Tag that tracks any {@link Relation} member identifiers that were added during country-slicing.
 * <p>
 * This is not an OSM tag.
 *
 * @author mgostintsev
 */
@Tag(synthetic = true, value = Validation.NON_EMPTY_STRING)
public interface SyntheticRelationMemberAdded
{
    @TagKey
    String KEY = "synthetic_relation_member_added";

    String MEMBER_DELIMITER = ",";

    /**
     * The list of all added member identifiers
     *
     * @param taggable
     *            The {@link Taggable} whose members we're interested in
     * @return Iterable of all the added member identifiers for this item
     */
    static Optional<Iterable<Long>> all(final Taggable taggable)
    {
        return taggable.getTag(KEY).map(tagValue -> Arrays.stream(tagValue.split(MEMBER_DELIMITER))
                .map(Long::valueOf).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    /**
     * @param taggable
     *            The {@link Taggable} we're looking at
     * @return {@code true} if this item has added relation members
     */
    static boolean hasAddedRelationMember(final Taggable taggable)
    {
        return taggable.getTag(KEY).isPresent();
    }
}
