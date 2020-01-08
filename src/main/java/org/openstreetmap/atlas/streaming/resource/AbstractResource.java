package org.openstreetmap.atlas.streaming.resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.compression.Decompressor;

/**
 * Base implementation for a {@link Resource}
 *
 * @author matthieun
 */
public abstract class AbstractResource implements Resource
{
    private Decompressor decompressor = Decompressor.NONE;
    private String name = null;

    public Decompressor getDecompressor()
    {
        return this.decompressor;
    }

    @Override
    public String getName()
    {
        if (this.name == null)
        {
            return Resource.super.getName();
        }
        return this.name;
    }

    @Override
    public long length()
    {
        try (InputStream input = new BufferedInputStream(read()))
        {
            long length = 0;
            while (input.read() >= 0)
            {
                length++;
            }
            return length;
        }
        catch (final IOException e)
        {
            throw new CoreException("Resource Length can't be obtained.", e);
        }
    }

    @Override
    public final InputStream read()
    {
        if (this.decompressor == null)
        {
            return this.onRead();
        }
        return this.decompressor.decompress(this.onRead());
    }

    public void setDecompressor(final Decompressor decompressor)
    {
        this.decompressor = decompressor;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        if (getName() != null)
        {
            return getName();
        }
        return super.toString();
    }

    /**
     * @return The raw stream from the resource
     */
    protected abstract InputStream onRead();
}
