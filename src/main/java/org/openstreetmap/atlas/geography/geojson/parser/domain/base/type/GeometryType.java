package org.openstreetmap.atlas.geography.geojson.parser.domain.base.type;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.openstreetmap.atlas.geography.geojson.parser.GeoJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Geometry;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.GeometryCollection;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.LineString;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.MultiLineString;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.MultiPoint;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.MultiPolygon;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Point;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Polygon;

/**
 * @author Yazad Khambata
 */
public enum GeometryType implements Type
{
    POINT("Point", Point.class),
    MULTI_POINT("MultiPoint", MultiPoint.class),
    LINE_STRING("LineString", LineString.class),
    MULTI_LINE_STRING("MultiLineString", MultiLineString.class),
    POLYGON("Polygon", Polygon.class),
    MULTI_POLYGON("MultiPolygon", MultiPolygon.class),
    GEOMETRY_COLLECTION("GeometryCollection", GeometryCollection.class, true);

    private String typeValue;
    private Class<? extends Geometry> concreteClass;
    private boolean collection;

    public static Geometry construct(final GeometryType geometryType,
            final GeoJsonParser goeJsonParser, final Map<String, Object> map)
    {
        try
        {
            final Class<? extends Geometry> concreteClass = geometryType.getConcreteClass();

            if (geometryType.isCollection())
            {
                return ConstructorUtils.invokeConstructor(concreteClass, goeJsonParser, map);
            }

            return ConstructorUtils.invokeConstructor(concreteClass, map);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static GeometryType fromConcreteClass(final Class<? extends Geometry> concreteClass)
    {
        Validate.notNull(concreteClass);

        final Predicate<GeometryType> filterFunction = geometryType -> geometryType
                .getConcreteClass().equals(concreteClass);
        return associatedGeometryType(concreteClass.toString(), filterFunction);
    }

    public static GeometryType fromTypeValue(final String typeValue)
    {
        Validate.notEmpty(typeValue);

        final Predicate<GeometryType> filterFunction = geometryType -> geometryType.getTypeValue()
                .equals(typeValue);
        return associatedGeometryType(typeValue, filterFunction);
    }

    private static GeometryType associatedGeometryType(final String typeValue,
            final Predicate<GeometryType> filterFunction)
    {
        return Arrays.stream(GeometryType.values()).filter(filterFunction).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(typeValue));
    }

    GeometryType(final String typeValue, final Class<? extends Geometry> concreteClass)
    {
        this(typeValue, concreteClass, false);
    }

    GeometryType(final String typeValue, final Class<? extends Geometry> concreteClass,
            final boolean collection)
    {
        this.typeValue = typeValue;
        this.concreteClass = concreteClass;
        this.collection = collection;
    }

    @Override
    public Geometry construct(final GeoJsonParser goeJsonParser, final Map<String, Object> map)
    {
        return GeometryType.construct(this, goeJsonParser, map);
    }

    @Override
    public Class<? extends Geometry> getConcreteClass()
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
