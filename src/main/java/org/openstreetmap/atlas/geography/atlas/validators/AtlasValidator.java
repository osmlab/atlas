package org.openstreetmap.atlas.geography.atlas.validators;

import java.util.Map;
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
        logger.info("Starting validation of Atlas {}", this.atlas.getName());
        final Time start = Time.now();
        logger.trace("Starting relation validation of Atlas {}", this.atlas.getName());
        final Time startRelations = Time.now();
        validateRelationsPresent();
        validateRelationsLinked();
        logger.trace("Finished relation validation of Atlas {} in {}", this.atlas.getName(),
                startRelations.elapsedSince());
        logger.trace("Starting tags validation of Atlas {}", this.atlas.getName());
        final Time startTags = Time.now();
        validateTagsPresent();
        logger.trace("Finished tags validation of Atlas {} in {}", this.atlas.getName(),
                startTags.elapsedSince());
        new AtlasLocationItemValidator(this.atlas).validate();
        new AtlasLineItemValidator(this.atlas).validate();
        new AtlasEdgeValidator(this.atlas).validate();
        new AtlasNodeValidator(this.atlas).validate();
        logger.info("Finished validation of Atlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

    protected void validateRelationsLinked()
    {
        for (final AtlasEntity entity : this.atlas.entities())
        {
            for (final Relation relation : entity.relations())
            {
                if (!relation.members().asBean()
                        .getItemFor(entity.getIdentifier(), entity.getType()).isPresent())
                {
                    throw new CoreException(
                            "Entity {} {} lists parent relation {} which does not have it as a member.",
                            entity.getType(), entity.getIdentifier(), relation.getIdentifier());
                }
            }
        }
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

    protected void validateTagsPresent()
    {
        for (final AtlasEntity entity : this.atlas.entities())
        {
            final Map<String, String> tags = entity.getTags();
            if (tags == null)
            {
                throw new CoreException("Entity {} {} is missing tags.", entity.getType(),
                        entity.getIdentifier());
            }
        }
    }
}
