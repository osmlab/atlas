package org.openstreetmap.atlas.geography.atlas.change.validators;

import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validate a {@link Change}
 *
 * @author matthieun
 */
public class ChangeValidator
{
    private static final Logger logger = LoggerFactory.getLogger(ChangeValidator.class);

    private final Change change;

    public ChangeValidator(final Change change)
    {
        this.change = change;
    }

    public void validate()
    {
        logger.debug("Starting validation of Change {}", this.change.getName());
        final Time start = Time.now();
        // One among many
        logger.debug("Finished validation of Change {} in {}", this.change.getName(),
                start.elapsedSince());
    }
}
