package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.validation.LengthValidator;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Extracts a {@link Distance} from a value. Can be used on tag values such as
 * {@link org.openstreetmap.atlas.tags.WidthTag}.
 *
 * @author bbreithaupt
 */
public class LengthExtractor implements TagExtractor
{
    private static final LengthValidator VALIDATOR = new LengthValidator();
    private static final String METERS_SUFFIX = " m";
    private static final String KILOMETERS_SUFFIX = " km";
    private static final String MILES_SUFFIX = " mi";
    private static final String NAUTICAL_MILES_SUFFIX = " nmi";

    /**
     * Validates and converts a value to a {@link Distance}.
     *
     * @param value
     *            {@link String} value.
     * @return {@link Optional} of a {@link Distance}
     */
    public static Optional<Distance> validateAndExtract(final String value)
    {
        if (VALIDATOR.isValid(value))
        {
            if (value.endsWith(METERS_SUFFIX))
            {
                return Optional.of(Distance.meters(
                        Double.valueOf(value.substring(0, value.lastIndexOf(METERS_SUFFIX)))));
            }
            else if (value.endsWith(KILOMETERS_SUFFIX))
            {
                return Optional.of(Distance.kilometers(
                        Double.valueOf(value.substring(0, value.lastIndexOf(KILOMETERS_SUFFIX)))));
            }
            else if (value.endsWith(MILES_SUFFIX))
            {
                return Optional.of(Distance.miles(
                        Double.valueOf(value.substring(0, value.lastIndexOf(MILES_SUFFIX)))));
            }
            else if (value.endsWith(NAUTICAL_MILES_SUFFIX))
            {
                return Optional.of(Distance.nauticalMiles(Double
                        .valueOf(value.substring(0, value.lastIndexOf(NAUTICAL_MILES_SUFFIX)))));
            }
            else if (value.contains("\""))
            {
                final StringList split = StringList.split(value, "\'");
                if (split.size() == 2)
                {
                    return Optional.of(Distance.feetAndInches(Double.valueOf(split.get(0)), Double
                            .valueOf(split.get(1).substring(0, split.get(1).lastIndexOf("\"")))));
                }
                else if (split.size() == 1)
                {
                    return Optional.of(Distance.inches(Double
                            .valueOf(split.get(0).substring(0, split.get(0).lastIndexOf("\"")))));
                }
            }
            else if (value.contains("'"))
            {
                return Optional.of(
                        Distance.feet(Double.valueOf(value.substring(0, value.lastIndexOf("'")))));
            }
            else
            {
                return Optional.of(Distance.meters(Double.valueOf(value)));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Distance> validateAndExtract(final String value, final Tag tag)
    {
        return LengthExtractor.validateAndExtract(value);
    }
}
