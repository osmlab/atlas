package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.MultiLineString;

/**
 * A utility class to help with conversions of geo json geometry instances with positions to atlas
 * geometry.
 *
 * @author Yazad Khambata
 */
public final class Positions
{
    public static Polygon toAtlasPolygonFromMultiLineString(final MultiLineString multiLineString)
    {
        final List<Polygon> atlasPolygons = toListOfAtlasPolygonsFromMultiLineString(
                multiLineString, 1);
        Validate.isTrue(atlasPolygons.size() == 1);
        return atlasPolygons.get(0);
    }

    public static List<List<Location>> toCollectionsOfLocations(
            final List<List<Position>> collectionOfPositions)
    {
        return collectionOfPositions.stream().map(Positions::toLocations)
                .collect(Collectors.toList());
    }

    public static List<Polygon> toListOfAtlasPolygonsFromMultiLineString(
            final MultiLineString multiLineString, final int expectedSize)
    {
        Validate.notNull(multiLineString);
        Validate.notNull(multiLineString.getCoordinates());
        Validate.notEmpty(multiLineString.getCoordinates().getValue());

        if (expectedSize > 0)
        {
            Validate.isTrue(expectedSize == multiLineString.getCoordinates().getValue().size());
        }

        final List<Polygon> polygons = multiLineString.getCoordinates().getValue().stream()
                .map(positions -> new Polygon(Positions.toLocations(positions)))
                .collect(Collectors.toList());
        return polygons;
    }

    public static List<Polygon> toListOfAtlasPolygonsFromMultiLineString(
            final MultiLineString multiLineString)
    {
        return toListOfAtlasPolygonsFromMultiLineString(multiLineString, -1);
    }

    /**
     * The order of longitude and latitude in GeoJson as per the RFC is [lon, lat, alt].
     * <p>
     * However the order longitude and latitude is not shared in various atlas constructors, hence
     * the flip in the order.
     *
     * @param position
     *            - the {@link Position} to convert to {@link Location}.
     * @return - the {@link Location} represented by the {@link Position}.
     */
    public static Location toLocation(final Position position)
    {
        return new Location(Latitude.degrees(position.getCoordinate2()),
                Longitude.degrees(position.getCoordinate1()));
    }

    public static List<Location> toLocations(final List<Position> positions)
    {
        return positions.stream().map(Positions::toLocation).collect(Collectors.toList());
    }

    private Positions()
    {
    }
}
