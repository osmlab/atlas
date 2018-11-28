package org.openstreetmap.atlas.geography.atlas.validators;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasLocationItemValidator
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasLocationItemValidator.class);

    private final Atlas atlas;

    public AtlasLocationItemValidator(final Atlas atlas)
    {
        this.atlas = atlas;
    }

    public void validate()
    {
        logger.trace("Starting LocationItem validation of Atlas {}", this.atlas.getName());
        final Time start = Time.now();
        validateLocationPresent();
        logger.trace("Finished LocationItem validation of Atlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

    private void validateLocationPresent()
    {
        for (final LocationItem locationItem : this.atlas.locationItems())
        {
            if (locationItem.getLocation() == null)
            {
                throw new CoreException("{} {} is missing a Location.", locationItem.getType(),
                        locationItem.getIdentifier());
            }
        }
    }
}
