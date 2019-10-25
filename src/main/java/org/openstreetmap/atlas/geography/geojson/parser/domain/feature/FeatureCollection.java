package org.openstreetmap.atlas.geography.geojson.parser.domain.feature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.geojson.parser.GoeJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.FeatureType;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.DefaultForeignFieldsImpl;

/**
 * @author Yazad Khambata
 */
public class FeatureCollection extends AbstractFeature
{
    private List<Feature> features;

    public FeatureCollection(final GoeJsonParser goeJsonParser, final Map<String, Object> map)
    {
        super(map, new DefaultForeignFieldsImpl(extractForeignFields(map, new HashSet<>(Arrays.asList("type", "bbox", "features", "properties")))));
        this.features = ((List<Map<String, Object>>) map.get("features")).stream()
                .map(goeJsonParser::deserialize).map(item -> (Feature) item)
                .collect(Collectors.toList());
    }

    @Override
    public Type getType()
    {
        return FeatureType.FEATURE_COLLECTION;
    }

    public List<Feature> getFeatures()
    {
        return this.features;
    }
}
