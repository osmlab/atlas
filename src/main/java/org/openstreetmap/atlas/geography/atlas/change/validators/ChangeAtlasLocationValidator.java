package org.openstreetmap.atlas.geography.atlas.change.validators;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
class ChangeAtlasLocationValidator
{
    private static final Logger logger = LoggerFactory
            .getLogger(ChangeAtlasLocationValidator.class);

    private final ChangeAtlas atlas;

    ChangeAtlasLocationValidator(final ChangeAtlas atlas)
    {
        this.atlas = atlas;
    }

    protected void validate()
    {
        logger.trace("Starting Location validation of ChangeAtlas {}", this.atlas.getName());
        final Time start = Time.now();
        validateLocationPresent();
        logger.trace("Finished Location validation of ChangeAtlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

    private void validateLocationPresent()
    {
        for (final LocationItem locationItem : this.atlas.nodes())
        {
            if (locationItem.getLocation() == null)
            {
                throw new CoreException("{} {} is missing a Location.", locationItem.getType(),
                        locationItem.getIdentifier());
            }
        }
    }
}
