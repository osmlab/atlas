package org.openstreetmap.atlas.geography.geojson.parser.mapper;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Yazad Khambata
 */
public interface Mapper extends Serializable
{
    <T> T map(Map<String, Object> map, Class<T> targetClass);
}
