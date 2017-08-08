package org.openstreetmap.atlas.streaming.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Properties;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.Streams;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

/**
 * @author cuthbertm
 */
public class ClassResource extends AbstractResource
{
    private final String resource;

    public ClassResource(final String resource)
    {
        this.resource = resource;
    }

    /**
     * Converts the resource into an object. It is expected that the format of the resource would be
     * JSON
     *
     * @param gson
     *            A Gson object to convert the json resource to the specified type. This could have
     *            special type adapters registered with it so that you can deserialize the resource
     *            correctly
     * @param classType
     *            The type of object to convert the resource json into
     * @param <T>
     *            The type T of the object being returned
     * @return The instantiated object
     */
    public <T> T getJSONResourceObject(final Gson gson, final Type classType)
    {
        InputStream input = null;
        try
        {
            input = onRead();
            final JsonReader reader = new JsonReader(new InputStreamReader(onRead()));
            return gson.fromJson(reader, classType);
        }
        catch (final JsonIOException | JsonSyntaxException e)
        {
            throw new CoreException("Failed to load json file from resource file {}", this.resource,
                    e);
        }
        finally
        {
            Streams.close(input);
        }
    }

    /**
     * Converts the resource into an object. It is expected that the format of the resource would be
     * JSON
     *
     * @param classType
     *            The type of object to convert the resource json into
     * @param <T>
     *            The type T of the object being returned
     * @return The instantiated objecte
     */
    public <T> T getJSONResourceObject(final Type classType)
    {
        return this.getJSONResourceObject(new Gson(), classType);
    }

    /**
     * Given a resource file location will load from jar/classpath and return a Properties file.
     * Resource file should follow pattern for Properties file
     *
     * @return A properties file
     */
    public Properties getResourceAsPropertyFile()
    {
        InputStream input = null;
        try
        {
            input = onRead();
            final Properties props = new Properties();
            props.load(input);
            return props;
        }
        catch (final IOException ioe)
        {
            throw new CoreException("Failed to load properties from resource file {}",
                    this.resource, ioe);
        }
        finally
        {
            Streams.close(input);
        }
    }

    @Override
    protected InputStream onRead()
    {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        if (classloader == null)
        {
            throw new CoreException("Context Class loader could not be initialized.");
        }
        final InputStream input = classloader.getResourceAsStream(this.resource);
        if (input == null)
        {
            throw new CoreException("Resource, {}, not found.", this.resource);
        }
        return input;
    }
}
