package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.Map;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.AbstractGeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.GeometryType;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;

/**
 * @author Yazad Khambata
 */
public abstract class AbstractGeometry extends AbstractGeoJsonItem implements Geometry
{
    AbstractGeometry(final Map<String, Object> map)
    {
        super(map);
    }

    @Override
    public GeometryType getGeometryType()
    {
        return GeometryType.fromConcreteClass(this.getClass());
    }

    @Override
    public String getTypeValue()
    {
        return getGeometryType().getTypeValue();
    }

    @Override
    public Type getType()
    {
        return getGeometryType();
    }
}
