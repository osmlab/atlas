package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;

/**
 * @author Yazad Khambata
 */
public class Point extends AbstractGeometryWithCoordinateSupport<Position, Located>
{
    private Coordinates<Position> coordinates;

    public Point(final Map<String, Object> map)
    {
        super(map);
        this.coordinates = Coordinates.forPoint((List<Double>) extractRawCoordinates(map));
    }

    @Override
    public Coordinates<Position> getCoordinates()
    {
        return this.coordinates;
    }
    
    @Override
    public Located toAtlasGeometry()
    {
        return coordinates.getValue().toLocation();
    }
}
