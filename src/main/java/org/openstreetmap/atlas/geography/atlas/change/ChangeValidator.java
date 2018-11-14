package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validate a {@link Change}
 *
 * @author matthieun
 */
class ChangeValidator
{
    private static final Logger logger = LoggerFactory.getLogger(ChangeValidator.class);

    private final Change change;

    ChangeValidator(final Change change)
    {
        this.change = change;
    }

    protected void validate()
    {
        logger.debug("Starting validation of Change {}", this.change.getName());
        final Time start = Time.now();
        // One among many
        logger.debug("Finished validation of Change {} in {}", this.change.getName(),
                start.elapsedSince());
    }
}
