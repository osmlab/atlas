package org.openstreetmap.atlas.tags;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Tag that tracks any {@link Relation} member role update. An example is an inner role changed to
 * an outer role. The format will be {relation-member-identifier}|{RoleChange}.
 * <p>
 * This is not an OSM tag.
 *
 * @author mgostintsev
 */
@Tag(synthetic = true, value = Validation.NON_EMPTY_STRING)
public interface SyntheticRelationRoleUpdated
{
    /**
     * @author mgostintsev
     */
    enum RoleChange
    {
        INNER_TO_OUTER,
        OUTER_TO_INNER;

        static RoleChange safeValueOf(final String value)
        {
            try
            {
                return valueOf(value.toUpperCase());
            }
            catch (final IllegalArgumentException ignored)
            {
                return null;
            }
        }
    }

    @TagKey
    String KEY = "synthetic_relation_role_updated";

    String ROLE_CHANGE_IDENTIFIER_DELIMITER = "|";

    /**
     * The optional Tuple of the member identifier and type of {@link RoleChange}
     *
     * @param taggable
     *            The {@link Taggable} we're interested in
     * @return the optional tuple of the member and type of {@link RoleChange}
     */
    static Optional<Tuple<Long, RoleChange>> all(final Taggable taggable)
    {
        final Optional<String> possibleUpdatedRole = taggable
                .getTag(SyntheticRelationRoleUpdated.class, Optional.empty());

        if (possibleUpdatedRole.isPresent())
        {
            final List<String> updatedRole = Arrays
                    .asList(possibleUpdatedRole.get().split(ROLE_CHANGE_IDENTIFIER_DELIMITER));

            return Optional.of(Tuple.createTuple(Long.valueOf(updatedRole.get(0)),
                    RoleChange.safeValueOf(updatedRole.get(1))));
        }

        return Optional.empty();
    }

    /**
     * @param taggable
     *            The {@link Taggable} we're looking at
     * @return {@code true} if this item has added relation members
     */
    static boolean hasUpdatedRelationMemberRole(final Taggable taggable)
    {
        return taggable.getTag(KEY).isPresent();
    }
}
