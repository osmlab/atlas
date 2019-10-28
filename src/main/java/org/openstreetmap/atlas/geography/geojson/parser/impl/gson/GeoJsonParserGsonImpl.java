package org.openstreetmap.atlas.geography.geojson.parser.impl.gson;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.openstreetmap.atlas.geography.geojson.parser.GeoJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.TypeUtil;
import org.openstreetmap.atlas.geography.geojson.parser.mapper.Mapper;
import org.openstreetmap.atlas.geography.geojson.parser.mapper.impl.DefaultBeanUtilsBasedMapperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Yazad Khambata
 */
public enum GeoJsonParserGsonImpl implements GeoJsonParser
{
    
    instance;
    
    private static final Logger log = LoggerFactory.getLogger(GeoJsonParserGsonImpl.class);
    
    @Override
    public GeoJsonItem deserialize(final String geoJson)
    {
        log.info("geoJson:: {}.", geoJson);
        
        final Map<String, Object> map = toMap(geoJson);
        
        return deserialize(map);
    }
    
    @Override
    public GeoJsonItem deserialize(final Map<String, Object> map)
    {
        log.info("map:: {}.", map);
        
        final Type type = TypeUtil.identifyStandardType(getType(map));
        
        return type.construct(GeoJsonParserGsonImpl.instance, map);
    }
    
    @Override
    public <T> T deserializeExtension(String json, Class<T> targetClass)
    {
        final Map<String, Object> map = toMap(json);
        return deserializeExtension(map, targetClass);
    }
    
    @Override
    public <T> T deserializeExtension(Map<String, Object> map, Class<T> targetClass)
    {
        final Mapper mapper = DefaultBeanUtilsBasedMapperImpl.instance;
        return mapper.map(map, targetClass);
    }
    
    private String getType(final Map<String, Object> map)
    {
        final Object type = map.get("type");
        Validate.isTrue(type instanceof String, "type: %s.", type);
        return (String) type;
    }
    
    private Map<String, Object> toMap(String geoJson)
    {
        final Gson gson = new GsonBuilder().create();
        return (Map<String, Object>) gson.fromJson(geoJson, Object.class);
    }
}
