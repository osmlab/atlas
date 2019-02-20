package org.openstreetmap.atlas.tags.annotations.validation;

import java.io.InputStreamReader;

import org.openstreetmap.atlas.utilities.scalars.Speed;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/**
 * Checks if the value of the tag matches the OSM conventions for denoting speed. See more on the
 * <a href="http://wiki.openstreetmap.org/wiki/Speed_limits">speed limits wiki</a> and
 * <a href="http://wiki.openstreetmap.org/wiki/Key:maxspeed">max speed wiki</a>. Note: the
 * validation has been loosened slightly from OSM standards to handle more valid limits.
 * Specifically, OSM dictates if km/h are the unit, no unit should be specified. The validation
 * below identifies 'kph' as a valid suffix.
 *
 * @author mgostintsev
 */
public class SpeedValidator implements TagValidator
{
    private static final DoubleValidator DOUBLE_VALIDATOR;
    private static final JsonObject IMPLICIT_SPEED_MAP = new Gson().fromJson(
            new JsonReader(
                    new InputStreamReader(SpeedValidator.class.getClassLoader().getResourceAsStream(
                            "org/openstreetmap/atlas/tags/annotations/implicit-speed-values.json"))),
            JsonObject.class);

    static
    {
        DOUBLE_VALIDATOR = new DoubleValidator();
        DOUBLE_VALIDATOR.setMinimum(0);
    }

    @Override
    public boolean isValid(final String value)
    {
        if (value.endsWith(Speed.MILES_PER_HOUR))
        {
            return DOUBLE_VALIDATOR.isValid(
                    value.substring(0, value.length() - Speed.MILES_PER_HOUR.length()).trim());
        }
        else if (value.endsWith(Speed.NAUTICAL_MILES_PER_HOUR))
        {
            return DOUBLE_VALIDATOR.isValid(value
                    .substring(0, value.length() - Speed.NAUTICAL_MILES_PER_HOUR.length()).trim());
        }
        else if (value.endsWith(Speed.KILOMETERS_PER_HOUR))
        {
            return DOUBLE_VALIDATOR.isValid(
                    value.substring(0, value.length() - Speed.KILOMETERS_PER_HOUR.length()).trim());
        }
        else if ("none".equals(value))
        {
            return true;
        }
        else if (IMPLICIT_SPEED_MAP.has(value.toLowerCase()))
        {
            return true;
        }
        else
        {
            return DOUBLE_VALIDATOR.isValid(value);
        }
    }
}
