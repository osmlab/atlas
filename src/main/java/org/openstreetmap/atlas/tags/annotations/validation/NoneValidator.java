package org.openstreetmap.atlas.tags.annotations.validation;

/**
 * Does no checking: only used for permitting any kind of values in a tag
 *
 * @author cstaylor
 */
public class NoneValidator implements TagValidator
{
    @Override
    public boolean isValid(final String value)
    {
        return true;
    }
}
