package org.openstreetmap.atlas.tags.annotations.validation;

/**
 * Checks if the value of a tag is either an exact value or can be coerced into a java long and
 * within an optional range of accepted values
 *
 * @author cstaylor
 */
public class LongValidator extends NumericValidator
{
    @Override
    protected Number parse(final String value)
    {
        return Long.parseLong(value);
    }
}
