package org.openstreetmap.atlas.geography.converters;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.TwoWayStringConverter;

/**
 * @author matthieun
 */
public class PolygonStringConverter implements TwoWayStringConverter<Polygon>
{
    @Override
    public String backwardConvert(final Polygon object)
    {
        return object.toCompactString();
    }

    @Override
    public Polygon convert(final String object)
    {
        final StringList split = StringList.split(object, PolyLine.SEPARATOR);
        final List<Location> locations = new ArrayList<>();
        for (final String location : split)
        {
            locations.add(Location.forString(location));
        }
        return new Polygon(locations);
    }

    public Polygon convertLongitudeLatitude(final String object)
    {
        final StringList split = StringList.split(object, PolyLine.SEPARATOR);
        final List<Location> locations = new ArrayList<>();
        for (final String location : split)
        {
            locations.add(Location.forStringLongitudeLatitude(location));
        }
        return new Polygon(locations);
    }
}
