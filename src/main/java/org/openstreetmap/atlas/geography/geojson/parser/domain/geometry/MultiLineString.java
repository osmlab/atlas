package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;

/**
 * @author Yazad Khambata
 */
public class MultiLineString extends AbstractGeometryWithCoordinateSupport<List<List<Position>>>
{
    private Coordinates<List<List<Position>>> coordinates;

    public MultiLineString(final Map<String, Object> map)
    {
        super(map, null);
        this.coordinates = Coordinates
                .forMultiLineString((List<List<List<Double>>>) extractRawCoordinates(map));
    }

    @Override
    public Coordinates<List<List<Position>>> getCoordinates()
    {
        return this.coordinates;
    }
}
