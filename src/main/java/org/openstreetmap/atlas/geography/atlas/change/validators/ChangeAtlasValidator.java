package org.openstreetmap.atlas.geography.atlas.change.validators;

import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class ChangeAtlasValidator
{
    private static final Logger logger = LoggerFactory.getLogger(ChangeAtlasValidator.class);

    private final ChangeAtlas atlas;

    public ChangeAtlasValidator(final ChangeAtlas atlas)
    {
        this.atlas = atlas;
    }

    public void validate()
    {
        logger.debug("Starting validation of ChangeAtlas {}", this.atlas.getName());
        final Time start = Time.now();
        new ChangeAtlasLocationItemValidator(this.atlas).validate();
        new ChangeAtlasLineItemValidator(this.atlas).validate();
        new ChangeAtlasEdgeValidator(this.atlas).validate();
        new ChangeAtlasNodeValidator(this.atlas).validate();
        logger.debug("Finished validation of ChangeAtlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

}
