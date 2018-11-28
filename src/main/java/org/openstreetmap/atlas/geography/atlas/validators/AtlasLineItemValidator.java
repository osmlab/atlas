package org.openstreetmap.atlas.geography.atlas.validators;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasLineItemValidator
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasLineItemValidator.class);

    private final Atlas atlas;

    public AtlasLineItemValidator(final Atlas atlas)
    {
        this.atlas = atlas;
    }

    public void validate()
    {
        logger.trace("Starting LineItem validation of Atlas {}", this.atlas.getName());
        final Time start = Time.now();
        validatePolyLinePresent();
        logger.trace("Finished LineItem validation of Atlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

    private void validatePolyLinePresent()
    {
        for (final LineItem lineItem : this.atlas.lineItems())
        {
            if (lineItem.asPolyLine() == null)
            {
                throw new CoreException("{} {} is missing its PolyLine.", lineItem.getType(),
                        lineItem.getIdentifier());
            }
        }
    }
}
