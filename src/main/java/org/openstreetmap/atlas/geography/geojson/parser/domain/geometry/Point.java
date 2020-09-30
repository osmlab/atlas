package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Positions;

/**
 * @author Yazad Khambata
 */
@SuppressWarnings("squid:S2160")
public class Point extends AbstractGeometryWithCoordinateSupport<Position, Location>
{
    private Position coordinates;

    public Point(final Map<String, Object> map)
    {
        super(map);
        this.coordinates = Coordinates.forPoint((List<Double>) extractRawCoordinates(map))
                .getValue();
    }

    @Override
    public Coordinates<Position> getCoordinates()
    {
        return new Coordinates<>(this.coordinates);
    }

    @Override
    public Location toAtlasGeometry()
    {
        return Positions.toLocation(this.coordinates);
    }
}
