package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;

import java.util.List;
import java.util.Map;

/**
 * @author Yazad Khambata
 */
public class LineString extends AbstractGeometryWithCoordinateSupport<List<Position>> {
    private MultiPoint value;

    public LineString(final Map<String, Object> map) {
        super(map);
        this.value = new MultiPoint(map);
    }

    @Override
    public Coordinates<List<Position>> getCoordinates() {
        return value.getCoordinates();
    }

    @Override
    public Bbox getBbox() {
        return value.getBbox();
    }

    @Override
    public ForeignFields getForeignFields() {
        return value.getForeignFields();
    }
}
