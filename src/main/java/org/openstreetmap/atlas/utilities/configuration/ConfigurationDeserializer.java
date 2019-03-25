package org.openstreetmap.atlas.utilities.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Basic {@link JsonDeserializer} the builds a key-value maps for quick lookup
 *
 * @author cstaylor
 * @author brian_l_davis
 */
class ConfigurationDeserializer extends JsonDeserializer<Map<String, Object>>
{
    private static final String INVALID_JSON = "Invalid JSON format.";
    private static final String DOT = ".";

    @Override
    public Map<String, Object> deserialize(final JsonParser jacksonParser,
            final DeserializationContext context) throws IOException
    {
        return deserializeObject(jacksonParser, context);
    }

    private ArrayList<Object> deserializeArray(final JsonParser jacksonParser,
            final DeserializationContext context) throws IOException
    {
        if (jacksonParser.getCurrentToken() != JsonToken.START_ARRAY)
        {
            throw new JsonMappingException(jacksonParser, INVALID_JSON);
        }

        jacksonParser.nextToken();

        final ArrayList<Object> list = new ArrayList<>();

        while (jacksonParser.getCurrentToken() != JsonToken.END_ARRAY)
        {
            list.add(deserializeValue(jacksonParser, context));
            jacksonParser.nextToken();
        }

        return list;
    }

    private Map<String, Object> deserializeObject(final JsonParser jacksonParser,
            final DeserializationContext context) throws IOException
    {
        if (jacksonParser.getCurrentToken() != JsonToken.START_OBJECT)
        {
            throw new JsonMappingException(jacksonParser, INVALID_JSON);
        }

        final Map<String, Object> map = new LinkedHashMap<>();
        while (jacksonParser.nextToken() != JsonToken.END_OBJECT)
        {
            final String dotKey = jacksonParser.getCurrentName();
            final Map<String, Object> locatedMap = locateMap(dotKey, map, jacksonParser);

            final String key;
            final int lastDot = dotKey.lastIndexOf(DOT);
            if (lastDot > 0 && lastDot < dotKey.length())
            {
                key = dotKey.substring(lastDot + 1);
            }
            else
            {
                key = dotKey;
            }
            // skip key, go to value
            jacksonParser.nextToken();
            final Object deserializedValue = deserializeValue(jacksonParser, context);
            locatedMap.put(key, deserializedValue);
        }

        return map;
    }

    private Object deserializeValue(final JsonParser jacksonParser,
            final DeserializationContext context) throws IOException
    {
        switch (jacksonParser.getCurrentToken())
        {
            case START_OBJECT:
                return deserializeObject(jacksonParser, context);
            case START_ARRAY:
                return deserializeArray(jacksonParser, context);
            case VALUE_STRING:
                return jacksonParser.getText();
            case VALUE_NUMBER_INT:
                return jacksonParser.getLongValue();
            case VALUE_NUMBER_FLOAT:
                return jacksonParser.getDoubleValue();
            case VALUE_TRUE:
                return Boolean.TRUE;
            case VALUE_FALSE:
                return Boolean.FALSE;
            case VALUE_NULL:
                return null;
            default:
                return context.handleUnexpectedToken(Object.class, jacksonParser);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> locateMap(final String dotKey, final Map<String, Object> map,
            final JsonParser jacksonParser) throws IOException
    {
        final int dotIndex = dotKey.indexOf(DOT);
        if (dotIndex > 0 && dotIndex < dotKey.length())
        {
            final String part = dotKey.substring(0, dotIndex);
            final String remaining = dotKey.substring(dotIndex + 1);
            final Object nextMap = map.getOrDefault(part, new LinkedHashMap<>());
            if (!(nextMap instanceof Map))
            {
                throw new JsonMappingException(jacksonParser, INVALID_JSON);
            }
            map.put(part, nextMap);
            return locateMap(remaining, (Map<String, Object>) nextMap, jacksonParser);
        }
        return map;
    }
}
