package org.openstreetmap.atlas.geography.converters;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.TwoWayStringConverter;

/**
 * @author matthieun
 */
public class PolyLineStringConverter implements TwoWayStringConverter<PolyLine>
{
    @Override
    public String backwardConvert(final PolyLine object)
    {
        return object.toString();
    }

    @Override
    public PolyLine convert(final String object)
    {
        final StringList split = StringList.split(object, PolyLine.SEPARATOR);
        final List<Location> locations = new ArrayList<>();
        for (final String location : split)
        {
            locations.add(Location.forString(location));
        }
        return new PolyLine(locations);
    }
}
