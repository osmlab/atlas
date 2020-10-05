package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Tag to indicate an entity is a synthetic relation member added during country slicing.
 * <p>
 * This is not an OSM tag.
 *
 * @author samg
 */
@Tag(synthetic = true)
public enum SyntheticSyntheticRelationMemberTag
{
    YES;

    @TagKey
    public static final String KEY = "synthetic_relation_member";

    public static boolean isSyntheticRelationMember(final Taggable taggable)
    {
        return Validators.from(SyntheticSyntheticRelationMemberTag.class, taggable).isPresent();
    }
}
