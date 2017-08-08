package org.openstreetmap.atlas.streaming;

import java.io.IOException;
import java.io.OutputStream;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author matthieun
 */
public class CounterOutputStream extends OutputStream
{
    private long count = 0;
    private boolean closed = false;

    @Override
    public void close()
    {
        this.closed = true;
    }

    public long getCount()
    {
        if (!this.closed)
        {
            throw new CoreException("Cannot get the counts when the stream has not been closed.");
        }
        return this.count;
    }

    @Override
    public void write(final byte[] bite, final int offset, final int length) throws IOException
    {
        this.count += length - offset;
    }

    @Override
    public void write(final int value) throws IOException
    {
        this.count++;
    }
}
