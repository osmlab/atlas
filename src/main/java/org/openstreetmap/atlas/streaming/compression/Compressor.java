package org.openstreetmap.atlas.streaming.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Compressor for an {@link OutputStream}
 *
 * @author matthieun
 */
public interface Compressor
{
    Compressor NONE = new Compressor()
    {
        @Override
        public OutputStream compress(final OutputStream out)
        {
            return out;
        }

        @Override
        public String toString()
        {
            return "NONE";
        }
    };
    Compressor GZIP = out ->
    {
        try
        {
            return new GZIPOutputStream(out);
        }
        catch (final IOException e)
        {
            throw new CoreException("Cannot create compressor.", e);
        }
    };

    /**
     * @param out
     *            The {@link OutputStream} to compress
     * @return The compressed {@link OutputStream}
     */
    OutputStream compress(OutputStream out);
}
