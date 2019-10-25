package org.openstreetmap.atlas.geography.geojson.parser.domain.feature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.openstreetmap.atlas.geography.geojson.parser.GoeJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.FeatureType;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.DefaultForeignFieldsImpl;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Geometry;

/**
 * @author Yazad Khambata
 */
public class Feature extends AbstractFeature
{
    private Geometry geometry;

    public Feature(final GoeJsonParser goeJsonParser, final Map<String, Object> map)
    {
        super(map, new DefaultForeignFieldsImpl(extractForeignFields(map,
                new HashSet<>(Arrays.asList("type", "bbox", "geometry", "properties")))));
        this.geometry = (Geometry) goeJsonParser
                .deserialize((Map<String, Object>) map.get("geometry"));
    }

    public Geometry getGeometry()
    {
        return this.geometry;
    }

    @Override
    public Type getType()
    {
        return FeatureType.FEATURE;
    }
}
