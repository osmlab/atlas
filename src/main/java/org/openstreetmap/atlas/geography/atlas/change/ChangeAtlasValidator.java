package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
class ChangeAtlasValidator
{
    private static final Logger logger = LoggerFactory.getLogger(ChangeAtlasValidator.class);

    private final ChangeAtlas atlas;

    ChangeAtlasValidator(final ChangeAtlas atlas)
    {
        this.atlas = atlas;
    }

    protected void validate()
    {
        logger.debug("Starting validation of ChangeAtlas {}", this.atlas.getName());
        final Time start = Time.now();
        new ChangeAtlasEdgeValidator(this.atlas).validate();
        new ChangeAtlasNodeValidator(this.atlas).validate();
        logger.debug("Finished validation of ChangeAtlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

}
