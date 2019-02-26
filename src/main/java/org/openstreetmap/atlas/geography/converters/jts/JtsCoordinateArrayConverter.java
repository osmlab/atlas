package org.openstreetmap.atlas.geography.converters.jts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * Convert an {@link Iterable} of {@link Location} to a {@link CoordinateSequence} from the JTS
 * library.
 *
 * @author matthieun
 */
public class JtsCoordinateArrayConverter
        implements TwoWayConverter<Iterable<Location>, CoordinateSequence>
{
    private static final JtsLocationConverter LOCATION_CONVERTER = new JtsLocationConverter();

    @Override
    public Iterable<Location> backwardConvert(final CoordinateSequence coordinateSequence)
    {
        final List<Location> result = new ArrayList<>();
        for (final Coordinate coordinate : coordinateSequence.toCoordinateArray())
        {
            result.add(LOCATION_CONVERTER.backwardConvert(coordinate));
        }
        return result;
    }

    @Override
    public CoordinateSequence convert(final Iterable<Location> locations)
    {
        final int size;
        if (locations instanceof Collection)
        {
            size = ((Collection<Location>) locations).size();
        }
        else
        {
            size = (int) Iterables.size(locations);
        }
        final Coordinate[] result = new Coordinate[size];
        int index = 0;
        for (final Location location : locations)
        {
            result[index++] = LOCATION_CONVERTER.convert(location);
        }
        return new CoordinateArraySequence(result);
    }
}
