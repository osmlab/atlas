package org.openstreetmap.atlas.geography.atlas.change.serializer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

import org.openstreetmap.atlas.geography.GeometryPrintable;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.conversion.Converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author matthieun
 */
public class FeatureChangeJsonSerializer implements Converter<FeatureChange, String>
{
    /**
     * @author matthieun
     */
    private static class AtlasEntityGeometryPrintableConverter
            implements Converter<AtlasEntity, GeometryPrintable>
    {
        @Override
        public GeometryPrintable convert(final AtlasEntity source)
        {
            GeometryPrintable result = null;
            if (source instanceof Area)
            {
                result = ((Area) source).asPolygon();
            }
            else if (source instanceof LineItem)
            {
                result = ((LineItem) source).asPolyLine();
            }
            else if (source instanceof LocationItem)
            {
                result = ((LocationItem) source).getLocation();
            }
            else
            {
                // Relation
                result = ((Relation) source).bounds();
            }
            if (result == null)
            {
                result = source.bounds();
            }
            return result;
        }
    }

    /**
     * @author matthieun
     */
    private static class AtlasEntityPropertiesConverter
            implements Converter<AtlasEntity, JsonObject>
    {
        private static final Function<Map<String, String>, JsonElement> tagPrinter = map ->
        {
            final JsonObject result = new JsonObject();
            map.forEach(result::addProperty);
            return result;
        };

        private static final Function<Iterable<? extends AtlasEntity>, JsonElement> identifierMapper = entity ->
        {
            final JsonArray result = new JsonArray();
            Iterables.stream(entity).map(AtlasEntity::getIdentifier).collectToSortedSet()
                    .forEach(number -> result.add(new JsonPrimitive(number)));
            return result;
        };

        @Override
        public JsonObject convert(final AtlasEntity source)
        {
            final JsonObject properties = new JsonObject();
            properties.addProperty("entityType", source.getType().toString());
            properties.addProperty("class", source.getClass().getName());
            properties.addProperty("identifier", source.getIdentifier());
            add(properties, "tags", source.getTags(), tagPrinter);
            add(properties, "relations", source.relations(), identifierMapper);

            if (source instanceof Edge)
            {
                addProperty(properties, "startNode", ((Edge) source).start(), Node::getIdentifier);
                addProperty(properties, "endNode", ((Edge) source).end(), Node::getIdentifier);
            }
            else if (source instanceof Node)
            {
                add(properties, "inEdges", ((Node) source).inEdges(), identifierMapper);
                add(properties, "outEdges", ((Node) source).outEdges(), identifierMapper);
            }
            else if (source instanceof Relation)
            {
                // Relation
                final Relation relation = (Relation) source;
                add(properties, "members", relation.members(), members ->
                {
                    final JsonArray beanResult = new JsonArray();
                    members.forEach(member -> beanResult.add(new JsonPrimitive(member.toString())));
                    return beanResult;
                });
            }
            return properties;
        }
    }

    /**
     * @author matthieun
     */
    private static class FeatureChangeTypeHierarchyAdapter implements JsonSerializer<FeatureChange>
    {
        private static void addGeometryGeojson(final JsonObject result,
                final GeometryPrintable property)
        {
            add(result, "geometry", property, GeometryPrintable::asGeoJsonGeometry);
        }

        private static void addGeometryWkt(final JsonObject result,
                final GeometryPrintable property)
        {
            addProperty(result, "WKT", property, GeometryPrintable::toWkt);
        }

        @Override
        public JsonElement serialize(final FeatureChange source, final Type typeOfSource,
                final JsonSerializationContext context)
        {
            final JsonObject result = new JsonObject();

            result.addProperty("type", "Feature");
            final Rectangle bounds = source.bounds();
            result.add("bbox", bounds.asGeoJsonBbox());

            final GeometryPrintable geometryPrintable = new AtlasEntityGeometryPrintableConverter()
                    .convert(source.getReference());
            addGeometryGeojson(result, geometryPrintable);

            final JsonObject properties = new JsonObject();
            properties.addProperty("featureChangeType", source.getChangeType().toString());
            new AtlasEntityPropertiesConverter().convert(source.getReference()).entrySet()
                    .forEach(entry -> properties.add(entry.getKey(), entry.getValue()));
            addGeometryWkt(properties, geometryPrintable);
            properties.addProperty("bboxWKT", source.bounds().toWkt());
            result.add("properties", properties);
            return result;
        }
    }

    private static final String NULL = "null";
    private static final Gson jsonSerializer;
    static
    {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        gsonBuilder.registerTypeHierarchyAdapter(FeatureChange.class,
                new FeatureChangeTypeHierarchyAdapter());
        jsonSerializer = gsonBuilder.create();
    }

    private static <T> void add(final JsonObject result, final String name, final T property,
            final Function<T, JsonElement> writer)
    {
        if (property == null)
        {
            result.addProperty(name, NULL);
        }
        else
        {
            result.add(name, writer.apply(property));
        }
    }

    private static <T> void addProperty(final JsonObject result, final String name,
            final T property, final Function<T, ? extends Object> writer)
    {
        result.addProperty(name, property == null ? NULL : writer.apply(property).toString());
    }

    @Override
    public String convert(final FeatureChange featureChange)
    {
        return jsonSerializer.toJson(featureChange);
    }
}
