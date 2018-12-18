package org.openstreetmap.atlas.geography.atlas.validators;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasRelationValidator
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasRelationValidator.class);

    private final Atlas atlas;

    public AtlasRelationValidator(final Atlas atlas)
    {
        this.atlas = atlas;
    }

    public void validate()
    {
        logger.trace("Starting Relation validation of Atlas {}", this.atlas.getName());
        final Time start = Time.now();
        validateRelationMembersPresent();
        logger.trace("Finished Reltaion validation of Atlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

    private void validateRelationMembersPresent()
    {
        for (final Relation relation : this.atlas.relations())
        {
            for (final RelationMember relationMember : relation.members())
            {
                if (relationMember.getEntity() == null
                        || this.atlas.entity(relationMember.getEntity().getIdentifier(),
                                relationMember.getEntity().getType()) == null)
                {
                    throw new CoreException(
                            "Relation {} specifies a member with role \"{}\" that is not present in the Atlas.",
                            relation.getIdentifier(), relationMember.getRole());
                }
            }
        }
    }
}
