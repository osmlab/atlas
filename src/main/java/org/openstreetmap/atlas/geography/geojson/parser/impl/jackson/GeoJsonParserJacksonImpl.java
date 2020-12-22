package org.openstreetmap.atlas.geography.geojson.parser.impl.jackson;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.geojson.parser.GeoJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.TypeUtil;
import org.openstreetmap.atlas.geography.geojson.parser.mapper.Mapper;
import org.openstreetmap.atlas.geography.geojson.parser.mapper.impl.DefaultBeanUtilsBasedMapperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Yazad Khambata
 * @author seancoulter
 */
public enum GeoJsonParserJacksonImpl implements GeoJsonParser
{

    INSTANCE;

    private static final Logger log = LoggerFactory.getLogger(GeoJsonParserJacksonImpl.class);

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

        return type.construct(GeoJsonParserJacksonImpl.INSTANCE, map);
    }

    @Override
    public <T> T deserializeExtension(final String json, final Class<T> targetClass)
    {
        final Map<String, Object> map = toMap(json);
        return deserializeExtension(map, targetClass);
    }

    @Override
    public <T> T deserializeExtension(final Map<String, Object> map, final Class<T> targetClass)
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

    private Map<String, Object> toMap(final String geoJson)
    {
        try
        {
            final ObjectMapper mapper = new ObjectMapper();
            return (Map<String, Object>) mapper.readValue(geoJson, Object.class);
        }
        catch (final JsonProcessingException exception1)
        {
            throw new CoreException(exception1.getMessage());
        }
    }
}
