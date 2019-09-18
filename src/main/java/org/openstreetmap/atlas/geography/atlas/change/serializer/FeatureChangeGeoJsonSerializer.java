package org.openstreetmap.atlas.geography.atlas.change.serializer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometryPrintable;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescription;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
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
public class FeatureChangeGeoJsonSerializer
        implements BiConsumer<FeatureChange, WritableResource>, Converter<FeatureChange, String>
{
    /**
     * @author matthieun
     */
    protected static class FeatureChangeTypeHierarchyAdapter
            implements JsonSerializer<FeatureChange>
    {
        private final boolean showDescription;

        private static void addGeometryGeojson(final JsonObject result,
                final GeometryPrintable property)
        {
            add(result, "geometry", property, GeometryPrintable::asGeoJson);
        }

        private static void addGeometryWkt(final JsonObject result,
                final GeometryPrintable property)
        {
            addProperty(result, "WKT", property, GeometryPrintable::toWkt);
        }

        FeatureChangeTypeHierarchyAdapter(final boolean showDescription)
        {
            this.showDescription = showDescription;
        }

        public JsonElement serialize(final FeatureChange source)
        {
            final JsonObject result = new JsonObject();

            result.addProperty("type", "Feature");
            final Rectangle bounds = source.bounds();
            result.add("bbox", bounds.asGeoJsonBbox());

            final GeometryPrintable geometryPrintable = new AtlasEntityGeometryPrintableConverter()
                    .convert(source);
            addGeometryGeojson(result, geometryPrintable);

            final JsonObject properties = new JsonObject();
            properties.addProperty("featureChangeType", source.getChangeType().toString());
            add(properties, "meta-data", source.getMetaData(), tagPrinter);
            if (this.showDescription)
            {
                add(properties, "description", source.explain(), ChangeDescription::toJsonElement);
            }
            new AtlasEntityPropertiesConverter().convert(source.getAfterView()).entrySet()
                    .forEach(entry -> properties.add(entry.getKey(), entry.getValue()));
            addGeometryWkt(properties, geometryPrintable);
            properties.addProperty("bboxWKT", source.bounds().toWkt());
            result.add("properties", properties);
            return result;
        }

        @Override
        public JsonElement serialize(final FeatureChange source, final Type typeOfSource,
                final JsonSerializationContext context)
        {
            return serialize(source);
        }
    }

    /**
     * @author matthieun
     */
    private static class AtlasEntityGeometryPrintableConverter
            implements Converter<FeatureChange, GeometryPrintable>
    {
        @Override
        public GeometryPrintable convert(final FeatureChange featureChange)
        {
            final AtlasEntity source = featureChange.getAfterView();
            GeometryPrintable result;
            if (source instanceof Area)
            {
                result = ((Area) source).asPolygon();
                if (result == null && featureChange.getBeforeView() != null)
                {
                    result = ((Area) featureChange.getBeforeView()).asPolygon();
                }
            }
            else if (source instanceof LineItem)
            {
                result = ((LineItem) source).asPolyLine();
                if (result == null && featureChange.getBeforeView() != null)
                {
                    result = ((LineItem) featureChange.getBeforeView()).asPolyLine();
                }
            }
            else if (source instanceof LocationItem)
            {
                result = ((LocationItem) source).getLocation();
                if (result == null && featureChange.getBeforeView() != null)
                {
                    result = ((LocationItem) featureChange.getBeforeView()).getLocation();
                }
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

    private static final Function<Iterable<? extends AtlasEntity>, JsonElement> identifierMapper = entity ->
    {
        final JsonArray result = new JsonArray();
        Iterables.stream(entity).map(AtlasEntity::getIdentifier).collectToSortedSet()
                .forEach(number -> result.add(new JsonPrimitive(number)));
        return result;
    };

    private static final Function<Map<String, String>, JsonElement> tagPrinter = map ->
    {
        final JsonObject result = new JsonObject();
        map.forEach(result::addProperty);
        return result;
    };

    private static final String NULL = "null";
    private final Gson jsonSerializer;

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

    public FeatureChangeGeoJsonSerializer(final boolean prettyPrint, final boolean showDescription)
    {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        if (prettyPrint)
        {
            gsonBuilder.setPrettyPrinting();
        }
        gsonBuilder.disableHtmlEscaping();
        gsonBuilder.registerTypeHierarchyAdapter(FeatureChange.class,
                new FeatureChangeTypeHierarchyAdapter(showDescription));
        this.jsonSerializer = gsonBuilder.create();
    }

    public FeatureChangeGeoJsonSerializer(final boolean prettyPrint)
    {
        this(prettyPrint, true);
    }

    @Override
    public void accept(final FeatureChange featureChange, final WritableResource resource)
    {
        try (Writer writer = resource.writer())
        {
            this.jsonSerializer.toJson(featureChange, writer);
        }
        catch (final IOException e)
        {
            throw new CoreException("Could not save FeatureChange to resource {}",
                    resource.getName(), e);
        }
    }

    @Override
    public String convert(final FeatureChange featureChange)
    {
        return this.jsonSerializer.toJson(featureChange);
    }
}
