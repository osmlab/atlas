package org.openstreetmap.atlas.geography.geojson.parser.domain.base.type;

import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.openstreetmap.atlas.geography.geojson.parser.GoeJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;

/**
 * @author Yazad Khambata
 */
public interface Type
{
    static <E extends Enum<E>> Type fromName(Class<? extends Type> subTypeClass, String typeValue)
    {
        return EnumUtils.getEnumList((Class<E>) subTypeClass).stream().map(item -> (Type) item)
                .filter(item -> item.getTypeValue().equals(typeValue)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(typeValue));
    }

    String getTypeValue();

    Class<? extends GeoJsonItem> getConcreteClass();

    boolean isCollection();

    GeoJsonItem construct(GoeJsonParser goeJsonParser, Map<String, Object> map);
}
