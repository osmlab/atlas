package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate;

import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Location;

/**
 * @author Yazad Khambata
 */
public final class Positions
{
    public static List<List<Location>> toCollectionsOfLocations(
            final List<List<Position>> collectionOfPositions)
    {
        return collectionOfPositions.stream().map(Positions::toLocations).collect(
                Collectors.toList());
    }
    
    public static List<Location> toLocations(final List<Position> positions)
    {
        return positions.stream().map(position -> position.toLocation()).collect(
                Collectors.toList());
    }
    
    private Positions()
    {
    }
}
