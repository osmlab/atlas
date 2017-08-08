package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.Date;

/**
 * Checks if the value of a tag is either an exact value or can be coerced into a java Date object
 *
 * @author cstaylor
 */
public class TimestampValidator extends ExactMatchValidator
{
    @Override
    public boolean isValid(final String value)
    {
        if (super.isValid(value))
        {
            return true;
        }

        try
        {
            new Date(Long.parseLong(value));
            return true;
        }
        catch (final NumberFormatException oops)
        {
            return false;
        }
    }
}
