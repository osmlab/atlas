package org.openstreetmap.atlas.streaming.compression;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Decompressor for an {@link InputStream}
 *
 * @author matthieun
 */
public interface Decompressor
{
    Decompressor NONE = new Decompressor()
    {
        @Override
        public InputStream decompress(final InputStream input)
        {
            return input;
        }

        @Override
        public String toString()
        {
            return "NONE";
        }
    };
    Decompressor GZIP = input ->
    {
        try
        {
            return new GZIPInputStream(input);
        }
        catch (final IOException e)
        {
            throw new CoreException("Cannot create decompressor.", e);
        }
    };

    /**
     * @param input
     *            The {@link InputStream} to decompress
     * @return The decompressed {@link InputStream}
     */
    InputStream decompress(InputStream input);
}
