package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import org.openstreetmap.atlas.geography.geojson.parser.GoeJsonParser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link GeometryCollection} nesting inside other {@link GeometryCollection}(s) is NOT allowed.
 *
 * @author Yazad Khambata
 */
public class GeometryCollection extends AbstractGeometry {
    private List<Geometry> geometries;

    public GeometryCollection(final GoeJsonParser goeJsonParser, final Map<String, Object> map) {
        super(map);
        this.geometries = ((List<Map<String, Object>>) map.get("geometries")).stream()
                .map(goeJsonParser::deserialize)
                .map(item -> (Geometry) item)
                .collect(Collectors.toList());
    }

    public List<Geometry> getGeometries() {
        return geometries;
    }
}
