package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.validation.SpeedValidator;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Speed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts {@link Speed} values.
 *
 * @author mgostintsev
 */
public final class SpeedExtractor
{
    private static final SpeedValidator VALIDATOR = new SpeedValidator();
    private static final String SINGLE_SPACE = " ";
    private static final Logger logger = LoggerFactory.getLogger(SpeedExtractor.class);

    public static Optional<Speed> validateAndExtract(final String value)
    {
        if (VALIDATOR.isValid(value))
        {
            try
            {
                if (value.endsWith(Speed.MILES_PER_HOUR))
                {
                    return Optional.of(Speed.milesPerHour(Double
                            .valueOf(StringList.split(value, SINGLE_SPACE).iterator().next())));
                }
                if (value.endsWith(Speed.NAUTICAL_MILES_PER_HOUR))
                {
                    return Optional.of(Speed.knots(Double
                            .valueOf(StringList.split(value, SINGLE_SPACE).iterator().next())));
                }
                if (value.endsWith(Speed.KILOMETERS_PER_HOUR))
                {
                    return Optional.of(Speed.kilometersPerHour(Double
                            .valueOf(StringList.split(value, SINGLE_SPACE).iterator().next())));
                }
                if ("none".equals(value))
                {
                    return Optional.empty();
                }
                return Optional.of(Speed.kilometersPerHour(Double.valueOf(value)));
            }
            catch (final NumberFormatException e)
            {
                logger.warn("Unable to read speed from {}", value);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private SpeedExtractor()
    {
    }
}
