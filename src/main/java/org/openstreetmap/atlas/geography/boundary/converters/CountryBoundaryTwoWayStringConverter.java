package org.openstreetmap.atlas.geography.boundary.converters;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.boundary.CountryBoundary;
import org.openstreetmap.atlas.geography.converters.MultiPolygonStringConverter;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.TwoWayStringConverter;

/**
 * @author matthieun
 */
public class CountryBoundaryTwoWayStringConverter
        implements TwoWayStringConverter<CountryBoundary>, Serializable
{
    private static final long serialVersionUID = -6339568171860908305L;

    private static final String COUNTRY_NAME_SEPARATOR = "@";

    private static final MultiPolygonStringConverter multiPolygonStringConverter = new MultiPolygonStringConverter();

    @Override
    public String backwardConvert(final CountryBoundary object)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(object.getCountryName());
        builder.append(COUNTRY_NAME_SEPARATOR);
        builder.append(multiPolygonStringConverter.backwardConvert(object.getBoundary()));
        return builder.toString();
    }

    @Override
    public CountryBoundary convert(final String object)
    {
        final StringList split = StringList.split(object, COUNTRY_NAME_SEPARATOR);
        return new CountryBoundary(split.get(0), multiPolygonStringConverter.convert(split.get(1)));
    }
}
