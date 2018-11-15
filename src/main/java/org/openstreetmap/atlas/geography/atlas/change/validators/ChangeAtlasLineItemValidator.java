package org.openstreetmap.atlas.geography.atlas.change.validators;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class ChangeAtlasLineItemValidator
{
    private static final Logger logger = LoggerFactory
            .getLogger(ChangeAtlasLineItemValidator.class);

    private final ChangeAtlas atlas;

    public ChangeAtlasLineItemValidator(final ChangeAtlas atlas)
    {
        this.atlas = atlas;
    }

    public void validate()
    {
        logger.trace("Starting LineItem validation of ChangeAtlas {}", this.atlas.getName());
        final Time start = Time.now();
        validatePolyLinePresent();
        logger.trace("Finished LineItem validation of ChangeAtlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

    private void validatePolyLinePresent()
    {
        // TODO Change to this.atlas.lineItems
        for (final LineItem lineItem : this.atlas.edges())
        {
            if (lineItem.asPolyLine() == null)
            {
                throw new CoreException("LineItem {} is missing its PolyLine.",
                        lineItem.getIdentifier());
            }
        }
    }
}
