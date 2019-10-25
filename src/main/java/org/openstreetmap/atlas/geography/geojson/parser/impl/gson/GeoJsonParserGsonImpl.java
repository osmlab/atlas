package org.openstreetmap.atlas.geography.geojson.parser.impl.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.Validate;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.GoeJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Yazad Khambata
 */
public enum GeoJsonParserGsonImpl implements GoeJsonParser {

    instance;

    private static final Logger log = LoggerFactory.getLogger(GeoJsonParserGsonImpl.class);

    @Override
    public GeoJsonItem deserialize(final String geoJson) {
        log.info("geoJson:: {}.", geoJson);

        final Gson gson = new GsonBuilder().create();
        final Map<String, Object> map = (Map<String, Object>)gson.fromJson(geoJson, Object.class);

        return deserialize(map);
    }

    @Override
    public GeoJsonItem deserialize(final Map<String, Object> map) {
        log.info("map:: {}.", map);

        final Type type = TypeUtil.identifyStandardType(getType(map));

        return type.construct(GeoJsonParserGsonImpl.instance, map);
    }

    private String getType(final Map<String, Object> map) {
        final Object type = map.get("type");
        Validate.isTrue(type instanceof String);
        return (String) type;
    }
}
