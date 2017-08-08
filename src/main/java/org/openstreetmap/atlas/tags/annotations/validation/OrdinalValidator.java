package org.openstreetmap.atlas.tags.annotations.validation;

/**
 * Ordinals must be positive integers
 *
 * @author cstaylor
 */
public class OrdinalValidator extends NumericValidator
{
    @Override
    protected Number parse(final String value)
    {
        return Long.parseLong(value);
    }

    @Override
    protected boolean withinRange(final Number checkMe)
    {
        return super.withinRange(checkMe) && checkMe.longValue() > 0L;
    }
}
