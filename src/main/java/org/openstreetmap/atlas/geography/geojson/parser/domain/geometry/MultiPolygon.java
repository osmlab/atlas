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
public class MultiPolygon extends AbstractGeometryWithCoordinateSupport<List<List<Position>>> {
    private MultiLineString value;

    public MultiPolygon(final Map<String, Object> map) {
        super(map);
        this.value = new MultiLineString(map);
    }

    @Override
    public Coordinates<List<List<Position>>> getCoordinates() {
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
