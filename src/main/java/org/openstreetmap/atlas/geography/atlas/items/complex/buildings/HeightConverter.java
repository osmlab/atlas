package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Example use: Building Height
 *
 * @author matthieun
 */
@Deprecated
public class HeightConverter implements StringConverter<Distance>
{
    private static final String METERS_SUFFIX = " m";

    @Override
    public Distance convert(final String object)
    {
        try
        {
            if (object.endsWith(METERS_SUFFIX))
            {
                return Distance.meters(
                        Double.valueOf(object.substring(0, object.lastIndexOf(METERS_SUFFIX))));
            }
            if (object.contains("\'") || object.contains("\""))
            {
                final StringList split = StringList.split(object, "\'");
                if (split.size() == 2)
                {
                    return Distance.feetAndInches(Double.valueOf(split.get(0)), Double
                            .valueOf(split.get(1).substring(0, split.get(1).lastIndexOf("\""))));
                }
                else if (split.size() == 1)
                {
                    return Distance.inches(Double
                            .valueOf(split.get(1).substring(0, split.get(1).lastIndexOf("\""))));
                }
                else
                {
                    throw new CoreException("Invalid Feet & Inches height value: {}", object);
                }
            }
            return Distance.meters(Double.valueOf(object));
        }
        catch (final Exception e)
        {
            throw new CoreException("Cannot parse height {}", object, e);
        }
    }
}
