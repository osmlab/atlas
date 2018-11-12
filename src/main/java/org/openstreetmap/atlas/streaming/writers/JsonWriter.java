package org.openstreetmap.atlas.streaming.writers;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.WritableResource;

import com.google.gson.JsonObject;

/**
 * Write Json objects
 *
 * @author matthieun
 */
public class JsonWriter implements Closeable
{
    private final BufferedWriter writer;

    public JsonWriter(final WritableResource resource)
    {
        this.writer = new BufferedWriter(
                new OutputStreamWriter(resource.write(), StandardCharsets.UTF_8));
    }

    @Override
    public void close()
    {
        try
        {
            this.writer.close();
        }
        catch (final IOException e)
        {
            throw new CoreException("Cannot close JsonWriter", e);
        }
    }

    public void flush()
    {
        try
        {
            this.writer.flush();
        }
        catch (final IOException e)
        {
            close();
            throw new CoreException("Cannot flush JsonWriter", e);
        }
    }

    public void write(final JsonObject object)
    {
        final String value = object.toString();
        try
        {
            this.writer.write(value);
        }
        catch (final IOException e)
        {
            close();
            throw new CoreException("Could not write String to JsonWriter", e);
        }
    }

    public void writeLine(final JsonObject object)
    {
        final String value = object.toString();
        writeLine(value);
    }

    private void writeLine(final String stringValue)
    {
        try
        {
            this.writer.write(stringValue);
            this.writer.newLine();
        }
        catch (final IOException e)
        {
            close();
            throw new CoreException("Could not write String to JsonWriter", e);
        }
    }
}
