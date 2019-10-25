package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;

import java.util.List;
import java.util.Map;

/**
 * @author Yazad Khambata
 */
public class MultiPoint extends AbstractGeometryWithCoordinateSupport<List<Position>> {
    private Coordinates<List<Position>> coordinates;

    public MultiPoint(final Map<String, Object> map) {
        super(map);
        this.coordinates = Coordinates.forMultiPoint((List<List<Double>>) extractRawCoordinates(map));
    }

    @Override
    public Coordinates<List<Position>> getCoordinates() {
        return coordinates;
    }
}