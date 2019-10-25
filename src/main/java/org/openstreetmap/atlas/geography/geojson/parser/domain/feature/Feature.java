package org.openstreetmap.atlas.geography.geojson.parser.domain.feature;

import org.openstreetmap.atlas.geography.geojson.parser.GoeJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.FeatureType;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Geometry;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.Properties;

import java.util.Map;

/**
 * @author Yazad Khambata
 */
public class Feature extends AbstractFeature {
    private Geometry geometry;
    private Properties properties;

    public Feature(final GoeJsonParser goeJsonParser, final Map<String, Object> map) {
        super(map);
        this.geometry = (Geometry) goeJsonParser.deserialize((Map<String, Object>) map.get("geometry"));

        //TODO: properties
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public Type getType() {
        return FeatureType.FEATURE;
    }
}
