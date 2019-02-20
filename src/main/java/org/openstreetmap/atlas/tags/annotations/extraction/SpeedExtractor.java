package org.openstreetmap.atlas.tags.annotations.extraction;

import java.io.InputStreamReader;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.validation.SpeedValidator;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Speed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

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
    private static final JsonObject IMPLICIT_SPEED_MAP = new Gson().fromJson(
            new JsonReader(
                    new InputStreamReader(SpeedExtractor.class.getClassLoader().getResourceAsStream(
                            "org/openstreetmap/atlas/tags/annotations/implicit-speed-values.json"))),
            JsonObject.class);

    public static Optional<Speed> validateAndExtract(final String value)
    {
        if (VALIDATOR.isValid(value))
        {
            try
            {
                final String valueOrImplicit = IMPLICIT_SPEED_MAP.has(value.toLowerCase())
                        ? IMPLICIT_SPEED_MAP.get(value.toLowerCase()).getAsString() : value;
                if (valueOrImplicit.endsWith(Speed.MILES_PER_HOUR))
                {
                    return Optional.of(Speed.milesPerHour(Double.valueOf(
                            StringList.split(valueOrImplicit, SINGLE_SPACE).iterator().next())));
                }
                if (valueOrImplicit.endsWith(Speed.NAUTICAL_MILES_PER_HOUR))
                {
                    return Optional.of(Speed.knots(Double.valueOf(
                            StringList.split(valueOrImplicit, SINGLE_SPACE).iterator().next())));
                }
                if (valueOrImplicit.endsWith(Speed.KILOMETERS_PER_HOUR))
                {
                    return Optional.of(Speed.kilometersPerHour(Double.valueOf(
                            StringList.split(valueOrImplicit, SINGLE_SPACE).iterator().next())));
                }
                if ("none".equals(valueOrImplicit))
                {
                    return Optional.empty();
                }
                return Optional.of(Speed.kilometersPerHour(Double.valueOf(valueOrImplicit)));
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
