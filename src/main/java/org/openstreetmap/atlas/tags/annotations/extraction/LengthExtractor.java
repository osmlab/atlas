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
    private static final String SINGLE_SPACE = " ";

    /**
     * Validates and converts a value to a {@link Distance}.
     *
     * @param value
     *            {@link String} value.
     * @return {@link Optional} of a {@link Distance}
     */
    public static Optional<Distance> validateAndExtract(final String value)
    {
        final String uppercaseValue = value.toUpperCase();
        if (VALIDATOR.isValid(uppercaseValue))
        {
            if (uppercaseValue.endsWith(SINGLE_SPACE + Distance.UnitAbbreviations.M))
            {
                return Optional.of(Distance.meters(Double.valueOf(uppercaseValue.substring(0,
                        uppercaseValue.lastIndexOf(SINGLE_SPACE + Distance.UnitAbbreviations.M)))));
            }
            else if (uppercaseValue.endsWith(SINGLE_SPACE + Distance.UnitAbbreviations.KM))
            {
                return Optional.of(Distance
                        .kilometers(Double.valueOf(uppercaseValue.substring(0, uppercaseValue
                                .lastIndexOf(SINGLE_SPACE + Distance.UnitAbbreviations.KM)))));
            }
            else if (uppercaseValue.endsWith(SINGLE_SPACE + Distance.UnitAbbreviations.MI))
            {
                return Optional
                        .of(Distance.miles(Double.valueOf(uppercaseValue.substring(0, uppercaseValue
                                .lastIndexOf(SINGLE_SPACE + Distance.UnitAbbreviations.MI)))));
            }
            else if (uppercaseValue.endsWith(SINGLE_SPACE + Distance.UnitAbbreviations.NMI))
            {
                return Optional.of(Distance
                        .nauticalMiles(Double.valueOf(uppercaseValue.substring(0, uppercaseValue
                                .lastIndexOf(SINGLE_SPACE + Distance.UnitAbbreviations.NMI)))));
            }
            else if (uppercaseValue.contains(Distance.INCHES_NOTATION))
            {
                final StringList split = StringList.split(uppercaseValue, Distance.FEET_NOTATION);
                if (split.size() == 2)
                {
                    return Optional.of(Distance.feetAndInches(Double.valueOf(split.get(0)),
                            Double.valueOf(split.get(1).substring(0,
                                    split.get(1).lastIndexOf(Distance.INCHES_NOTATION)))));
                }
                else if (split.size() == 1)
                {
                    return Optional.of(Distance.inches(Double.valueOf(split.get(0).substring(0,
                            split.get(0).lastIndexOf(Distance.INCHES_NOTATION)))));
                }
            }
            else if (uppercaseValue.contains(Distance.FEET_NOTATION))
            {
                return Optional.of(Distance.feet(Double.valueOf(uppercaseValue.substring(0,
                        uppercaseValue.lastIndexOf(Distance.FEET_NOTATION)))));
            }
            else
            {
                return Optional.of(Distance.meters(Double.valueOf(uppercaseValue)));
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
