package org.openstreetmap.atlas.geography.geojson.parser.domain.feature;

import org.openstreetmap.atlas.geography.geojson.parser.GoeJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.FeatureType;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yazad Khambata
 */
public class FeatureCollection extends AbstractFeature {
    private List<Feature> features;

    public FeatureCollection(final GoeJsonParser goeJsonParser, final Map<String, Object> map) {
        super(map);
        this.features = ((List<Map<String, Object>>) map.get("features")).stream()
                .map(goeJsonParser::deserialize)
                .map(item -> (Feature) item)
                .collect(Collectors.toList());

        //TODO: Properties?
    }

    @Override
    public Type getType() {
        return FeatureType.FEATURE_COLLECTION;
    }

    public List<Feature> getFeatures() {
        return features;
    }
}
