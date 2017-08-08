package org.openstreetmap.atlas.geography.atlas.pbf.converters;

import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

/**
 * @author matthieun
 */
public class LocationIterableToWayNodeListConverter
        implements Converter<Iterable<? extends Location>, List<WayNode>>
{
    @Override
    public List<WayNode> convert(final Iterable<? extends Location> object)
    {
        return Iterables.stream(object).map(location -> new WayNode(location.asConcatenation()))
                .collectToList();
    }
}
