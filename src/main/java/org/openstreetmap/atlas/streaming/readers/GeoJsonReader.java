package org.openstreetmap.atlas.streaming.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.readers.json.deserializers.LocatedDeserializer;
import org.openstreetmap.atlas.streaming.readers.json.serializers.PropertiesLocated;
import org.openstreetmap.atlas.streaming.resource.Resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * Basic GeoJson stream reader. It reads only Point, LineString and Polygon. It ignores the rest,
 * including CRS.
 *
 * @author matthieun
 */
public class GeoJsonReader implements Iterator<PropertiesLocated>
{
    private final InputStream input;
    private final JsonReader reader;
    private final Gson gson;

    public GeoJsonReader(final Resource source)
    {
        this.input = source.read();
        this.reader = new JsonReader(new InputStreamReader(this.input));
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Located.class, new LocatedDeserializer());
        this.gson = builder.create();

        try
        {
            this.reader.beginObject();
            if (!"type".equals(this.reader.nextName())
                    || !"FeatureCollection".equals(this.reader.nextString()))
            {
                throw new CoreException("Malformed feature collection");
            }
            String features = this.reader.nextName();
            while (!"features".equals(features))
            {
                // Read and skip the object (crs for example)
                this.gson.fromJson(this.reader, JsonObject.class);
                features = this.reader.nextName();
            }
            this.reader.beginArray();
        }
        catch (final Exception e)
        {
            Streams.close(this.input);
            throw new CoreException("Error reading GeoJson stream", e);
        }
    }

    @Override
    public boolean hasNext()
    {
        try
        {
            final boolean hasNext = this.reader.hasNext()
                    && !this.reader.peek().equals(JsonToken.END_ARRAY);
            if (!hasNext)
            {
                Streams.close(this.input);
            }
            return hasNext;
        }
        catch (final IOException e)
        {
            Streams.close(this.input);
            throw new CoreException("Error reading GeoJson stream", e);
        }
    }

    @Override
    public PropertiesLocated next()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException();
        }
        try
        {
            Located geometry = null;
            JsonObject properties = null;
            this.reader.beginObject();
            while (this.reader.hasNext())
            {
                final String name = this.reader.nextName();
                if ("properties".equals(name))
                {
                    // Populate the properties
                    properties = this.gson.fromJson(this.reader, JsonObject.class);
                }
                else if ("geometry".equals(name))
                {
                    geometry = this.gson.fromJson(this.reader, Located.class);
                }
                else
                {
                    this.reader.skipValue();
                }
            }
            this.reader.endObject();
            if (geometry == null || properties == null)
            {
                throw new CoreException("Geometry or properties were null.");
            }
            return new PropertiesLocated(geometry, properties);
        }
        catch (final IOException e)
        {
            Streams.close(this.input);
            throw new CoreException("Error reading GeoJson stream", e);
        }
    }
}
