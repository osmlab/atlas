package org.openstreetmap.atlas.tags.annotations.validation;

import org.openstreetmap.atlas.locale.IsoCountry;

/**
 * Checks if the value of a tag matches an ISO2 or ISO3 country code
 *
 * @author cstaylor
 */
public class ISOCountryValidator implements TagValidator
{
    @Override
    public boolean isValid(final String value)
    {
        return IsoCountry.isValidCountryCode(value);
    }
}
