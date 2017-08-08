package org.openstreetmap.atlas.tags.annotations.validation;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.HeightConverter;

/**
 * Based on mnahoum's {@link HeightConverter} class.
 * <p>
 * Note that values like width and height are still considered lengths in the OSM Units definition
 * http://wiki.openstreetmap.org/wiki/Map_Features/Units
 *
 * @author cstaylor
 * @author gpogulsky
 */
public class LengthValidator implements TagValidator
{
    private static final DoubleValidator DOUBLE_VALIDATOR;
    private static final String METERS_SUFFIX = " m";

    static
    {
        DOUBLE_VALIDATOR = new DoubleValidator();
        DOUBLE_VALIDATOR.setMinimum(0);
    }

    /**
     * Validates if the give value is a proper length value.
     * <p>
     * The expected values are "12.5 m", "12.5", "12'5\"".Incomplete values like "12'"or "5\"" are
     * also recognized. The method will properly handle malformed values like "Estacion de Servicio
     * \"Los Arrayanes\"" (contains \ ", but is not a number), "12'err", etc.
     * </p>
     */
    @Override
    public boolean isValid(final String value)
    {
        final boolean result;

        if (value.endsWith(METERS_SUFFIX))
        {
            result = DOUBLE_VALIDATOR.isValid(value.substring(0, value.length() - 2));
        }
        else
        {
            final int feetIndex = value.indexOf('\'');
            if (feetIndex > -1)
            {
                if (DOUBLE_VALIDATOR.isValid(value.substring(0, feetIndex)))
                {
                    // Tail?
                    if (feetIndex + 1 < value.length())
                    {
                        final int inchesIndex = value.indexOf('\"', feetIndex + 1);
                        if (inchesIndex > -1)
                        {
                            result = this.validateInchesAndTail(value, feetIndex + 1, inchesIndex);
                        }
                        else
                        {
                            result = StringUtils
                                    .isBlank(value.substring(feetIndex + 1, value.length()));
                        }
                    }
                    else
                    {
                        result = true;
                    }
                }
                else
                {
                    result = false;
                }
            }
            else
            {
                final int inchesIndex = value.indexOf('\"');
                if (inchesIndex > -1)
                {
                    result = this.validateInchesAndTail(value, 0, inchesIndex);
                }
                else
                {
                    result = DOUBLE_VALIDATOR.isValid(value);
                }
            }
        }

        return result;
    }

    private boolean validateInchesAndTail(final String value, final int start, final int index)
    {
        return DOUBLE_VALIDATOR.isValid(value.substring(start, index)) ? index + 1 == value.length()
                || StringUtils.isBlank(value.substring(index + 1, value.length())) : false;
    }

}
