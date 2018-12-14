package org.openstreetmap.atlas.geography.atlas.change.serializer;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.GeometryPrintable;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
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
    private static class AtlasEntityTypeHierarchyAdapter
            implements Converter<AtlasEntity, JsonElement>
    {
        @Override
        public JsonElement convert(final AtlasEntity source)
        {
            final JsonObject result = new JsonObject();
            result.addProperty("type", source.getType().toString());
            result.addProperty("class", source.getClass().getName());
            result.addProperty("identifier", source.getIdentifier());
            addProperty(result, "tags", source.getTags(), map -> map);
            addProperty(result, "relations", source.relations(), relations -> relations.stream()
                    .map(Relation::getIdentifier).collect(Collectors.toSet()).toString());
            if (source instanceof Area)
            {
                addGeometry(result, ((Area) source).asPolygon());
            }
            else if (source instanceof LineItem)
            {
                if (source instanceof Edge)
                {
                    addProperty(result, "startNode", ((Edge) source).start(), Node::getIdentifier);
                    addProperty(result, "endNode", ((Edge) source).end(), Node::getIdentifier);
                }
                addGeometry(result, ((LineItem) source).asPolyLine());
            }
            else if (source instanceof LocationItem)
            {
                if (source instanceof Node)
                {
                    final Function<SortedSet<Edge>, Set<Long>> identifierMapper = edges -> edges
                            .stream().map(Edge::getIdentifier).collect(Collectors.toSet());
                    addProperty(result, "inEdges", ((Node) source).inEdges(), identifierMapper);
                    addProperty(result, "outEdges", ((Node) source).outEdges(), identifierMapper);
                }
                addGeometry(result, ((LocationItem) source).getLocation());
            }
            else
            {
                // Relation
                final Relation relation = (Relation) source;
                add(result, "members", relation.members(), members ->
                {
                    final JsonArray beanResult = new JsonArray();
                    members.forEach(member -> beanResult.add(new JsonPrimitive(member.toString())));
                    return beanResult;
                });
                addGeometry(result, relation.bounds());
            }
            return result;
        }

        private <T> void add(final JsonObject result, final String name, final T property,
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

        private void addGeometry(final JsonObject result, final GeometryPrintable property)
        {
            addProperty(result, "WKT", property, GeometryPrintable::toWkt);
            add(result, GEOJSON, property, GeometryPrintable::asGeoJsonGeometry);
        }

        private <T> void addProperty(final JsonObject result, final String name, final T property,
                final Function<T, ? extends Object> writer)
        {
            result.addProperty(name, property == null ? NULL : writer.apply(property).toString());
        }

    }

    /**
     * @author matthieun
     */
    private static class FeatureChangeTypeHierarchyAdapter implements JsonSerializer<FeatureChange>
    {
        @Override
        public JsonElement serialize(final FeatureChange source, final Type typeOfSource,
                final JsonSerializationContext context)
        {
            final JsonObject result = new JsonObject();
            result.addProperty("type", source.getChangeType().toString());
            result.addProperty("boundsWKT", source.bounds().toWkt());
            result.add("atlasEntityReference",
                    new AtlasEntityTypeHierarchyAdapter().convert(source.getReference()));
            result.add("boundsGEOJSON", source.bounds().asGeoJsonGeometry());
            return result;
        }
    }

    private static final String NULL = "null";
    private static final String GEOJSON = "GEOJSON";

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

    @Override
    public String convert(final FeatureChange featureChange)
    {
        return jsonSerializer.toJson(featureChange);
    }
}
