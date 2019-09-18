package org.openstreetmap.atlas.geography.atlas.change.serializer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.serializer.FeatureChangeGeoJsonSerializer.FeatureChangeTypeHierarchyAdapter;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.conversion.Converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author matthieun
 */
public class ChangeGeoJsonSerializer
        implements BiConsumer<Change, WritableResource>, Converter<Change, String>
{
    /**
     * @author matthieun
     */
    private static class ChangeTypeHierarchyAdapter implements JsonSerializer<Change>
    {
        private final FeatureChangeTypeHierarchyAdapter featureChangeTypeHierarchyAdapter;

        ChangeTypeHierarchyAdapter(final boolean showDescription)
        {
            this.featureChangeTypeHierarchyAdapter = new FeatureChangeTypeHierarchyAdapter(
                    showDescription);
        }

        @Override
        public JsonElement serialize(final Change source, final Type typeOfSource,
                final JsonSerializationContext context)
        {
            final JsonObject result = new JsonObject();

            result.addProperty("type", "FeatureCollection");
            final Rectangle bounds = source.bounds();
            result.add("bbox", bounds.asGeoJsonBbox());

            final JsonArray features = new JsonArray();
            source.changes().map(this.featureChangeTypeHierarchyAdapter::serialize)
                    .forEach(features::add);
            result.add("features", features);

            final JsonObject properties = new JsonObject();
            properties.addProperty("bboxWKT", source.bounds().toWkt());
            result.add("properties", properties);
            return result;
        }
    }

    private final Gson jsonSerializer;

    public ChangeGeoJsonSerializer()
    {
        this(true);
    }

    public ChangeGeoJsonSerializer(final boolean prettyPrint, final boolean showDescription)
    {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        if (prettyPrint)
        {
            gsonBuilder.setPrettyPrinting();
        }
        gsonBuilder.disableHtmlEscaping();
        gsonBuilder.registerTypeHierarchyAdapter(Change.class,
                new ChangeTypeHierarchyAdapter(showDescription));
        this.jsonSerializer = gsonBuilder.create();
    }

    public ChangeGeoJsonSerializer(final boolean prettyPrint)
    {
        this(prettyPrint, true);
    }

    @Override
    public void accept(final Change change, final WritableResource resource)
    {
        try (Writer writer = resource.writer())
        {
            this.jsonSerializer.toJson(change, writer);
        }
        catch (final IOException e)
        {
            throw new CoreException("Could not save FeatureChange to resource {}",
                    resource.getName(), e);
        }
    }

    @Override
    public String convert(final Change change)
    {
        return this.jsonSerializer.toJson(change);
    }
}
