package org.openstreetmap.atlas.geography.atlas.pbf.converters;

import java.util.Date;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;

/**
 * @author matthieun
 */
public class LocationToOsmosisNodeConverter implements Converter<Location, Node>
{
    @Override
    public Node convert(final Location object)
    {
        return new Node(new CommonEntityData(object.asConcatenation(), 0, new Date(),
                new OsmUser(0, "osm"), 0), object.getLatitude().asDegrees(),
                object.getLongitude().asDegrees());
    }
}
