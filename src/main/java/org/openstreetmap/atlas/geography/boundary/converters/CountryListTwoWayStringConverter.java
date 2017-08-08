package org.openstreetmap.atlas.geography.boundary.converters;

import java.io.Serializable;

import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.TwoWayStringConverter;

/**
 * @author tony
 */
public class CountryListTwoWayStringConverter
        implements TwoWayStringConverter<StringList>, Serializable
{
    private static final long serialVersionUID = -9019352938025359414L;
    private static final String COUNTRY_LIST_SEPARATOR = ",";

    @Override
    public String backwardConvert(final StringList countryList)
    {
        return countryList.join(COUNTRY_LIST_SEPARATOR);
    }

    @Override
    public StringList convert(final String countries)
    {
        return StringList.split(countries, COUNTRY_LIST_SEPARATOR);
    }
}
