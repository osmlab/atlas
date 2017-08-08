package org.openstreetmap.atlas.streaming.resource;

import java.io.OutputStream;

import org.openstreetmap.atlas.streaming.compression.Compressor;

/**
 * Implementation for a {@link WritableResource}
 *
 * @author matthieun
 */
public abstract class AbstractWritableResource extends AbstractResource implements WritableResource
{
    private Compressor compressor = Compressor.NONE;

    public Compressor getCompressor()
    {
        return this.compressor;
    }

    public void setCompressor(final Compressor compressor)
    {
        this.compressor = compressor;
    }

    @Override
    public final OutputStream write()
    {
        if (this.compressor == null)
        {
            return this.onWrite();
        }
        return this.compressor.compress(this.onWrite());
    }

    /**
     * @return The stream where to write raw bytes to the resource
     */
    protected abstract OutputStream onWrite();
}
