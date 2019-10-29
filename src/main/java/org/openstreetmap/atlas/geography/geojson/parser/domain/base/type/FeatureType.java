package org.openstreetmap.atlas.geography.geojson.parser.domain.base.type;

import java.util.Map;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.openstreetmap.atlas.geography.geojson.parser.GeoJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.feature.AbstractFeature;
import org.openstreetmap.atlas.geography.geojson.parser.domain.feature.Feature;
import org.openstreetmap.atlas.geography.geojson.parser.domain.feature.FeatureCollection;

/**
 * @author Yazad Khambata
 */
public enum FeatureType implements Type
{
    FEATURE("Feature", Feature.class),
    FEATURE_COLLECTION("FeatureCollection", FeatureCollection.class, true);

    private String typeValue;
    private Class<? extends AbstractFeature> concreteClass;
    private boolean collection;

    public static AbstractFeature construct(final FeatureType geometryType,
            final GeoJsonParser goeJsonParser, final Map<String, Object> map)
    {
        try
        {
            final Class<? extends AbstractFeature> concreteClass = geometryType.getConcreteClass();

            return ConstructorUtils.invokeConstructor(concreteClass, goeJsonParser, map);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    FeatureType(final String typeValue, final Class<? extends AbstractFeature> concreteClass)
    {
        this(typeValue, concreteClass, false);
    }

    FeatureType(final String typeValue, final Class<? extends AbstractFeature> concreteClass,
            final boolean collection)
    {
        this.typeValue = typeValue;
        this.concreteClass = concreteClass;
        this.collection = collection;
    }

    @Override
    public AbstractFeature construct(final GeoJsonParser goeJsonParser,
            final Map<String, Object> map)
    {
        return FeatureType.construct(this, goeJsonParser, map);
    }

    @Override
    public Class<? extends AbstractFeature> getConcreteClass()
    {
        return this.concreteClass;
    }

    @Override
    public String getTypeValue()
    {
        return this.typeValue;
    }

    @Override
    public boolean isCollection()
    {
        return this.collection;
    }
}
