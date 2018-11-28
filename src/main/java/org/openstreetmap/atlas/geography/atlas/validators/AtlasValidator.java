package org.openstreetmap.atlas.geography.atlas.validators;

import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasValidator
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasValidator.class);

    private final Atlas atlas;

    public AtlasValidator(final Atlas atlas)
    {
        this.atlas = atlas;
    }

    public void validate()
    {
        logger.debug("Starting validation of ChangeAtlas {}", this.atlas.getName());
        final Time start = Time.now();
        new AtlasLocationItemValidator(this.atlas).validate();
        new AtlasLineItemValidator(this.atlas).validate();
        new AtlasEdgeValidator(this.atlas).validate();
        new AtlasNodeValidator(this.atlas).validate();
        this.validateRelationsPresent();
        logger.debug("Finished validation of ChangeAtlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

    protected void validateRelationsPresent()
    {
        for (final AtlasEntity entity : this.atlas.entities())
        {
            for (final Relation relation : entity.relations())
            {
                if (relation == null)
                {
                    throw new CoreException(
                            "Entity {} {} lists some parent relation that is not present: {}",
                            entity.getType(), entity.getIdentifier(),
                            entity.relations().stream()
                                    .map(parent -> parent == null ? "null"
                                            : String.valueOf(parent.getIdentifier()))
                                    .collect(Collectors.toSet()));
                }
            }
        }
    }
}
