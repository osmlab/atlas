package org.openstreetmap.atlas.tags.annotations.validation;

/**
 * Checks if the value of a tag has at least one non-whitespace character
 *
 * @author cstaylor
 */
public class NonEmptyStringValidator implements TagValidator
{
    @Override
    public boolean isValid(final String value)
    {
        return value.trim().length() > 0;
    }
}
