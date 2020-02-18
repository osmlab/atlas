package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate;

import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Point;

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
        return positions.stream().map(Positions::toLocation).collect(
                Collectors.toList());
    }
    
    /**
     * The order of longitude and latitude in GeoJson as per the RFC is [lon, lat, alt].
     * <p>
     * However the order longitude and latitude is not shared in various atlas constructors, hence
     * the flip in the order.
     *
     * @return - the {@link Location} represented by the {@link Position}.
     */
    public static Location toLocation(final Position position)
    {
        return new Location(Latitude.degrees(position.getCoordinate2()), Longitude.degrees(position.getCoordinate1()));
    }
    
    private Positions()
    {
    }
}
