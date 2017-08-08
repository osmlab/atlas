package org.openstreetmap.atlas.streaming.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.arrays.ByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WritableResource} backed by a large {@link ByteArray}.
 *
 * @author matthieun
 */
public class ByteArrayResource extends AbstractWritableResource
{
    private static final Logger logger = LoggerFactory.getLogger(ByteArrayResource.class);

    private static final int BYTE_MASK = 0xFF;

    private final ByteArray array;

    public ByteArrayResource()
    {
        this.array = new ByteArray(Long.MAX_VALUE);
        this.array.setName("ByteArrayResource");
    }

    /**
     * @param initialSize
     *            An initial size to help avoiding resizings.
     */
    public ByteArrayResource(final long initialSize)
    {
        final int blockSize = (int) (initialSize <= Integer.MAX_VALUE ? initialSize
                : Integer.MAX_VALUE);
        this.array = new ByteArray(Long.MAX_VALUE, blockSize, Integer.MAX_VALUE);
        this.array.setName("ByteArrayResource");
    }

    @Override
    public long length()
    {
        return this.array.size();
    }

    public ByteArrayResource withName(final String name)
    {
        setName(name);
        this.array.setName(name);
        return this;
    }

    @Override
    protected InputStream onRead()
    {
        return new InputStream()
        {
            private long index = 0L;
            private boolean readOpen = true;

            @Override
            public void close()
            {
                this.readOpen = false;
            }

            @Override
            public int read() throws IOException
            {
                if (!this.readOpen)
                {
                    throw new CoreException("Cannot read a closed stream");
                }
                if (this.index >= ByteArrayResource.this.array.size())
                {
                    return -1;
                }
                return ByteArrayResource.this.array.get(this.index++) & BYTE_MASK;
            }
        };
    }

    @Override
    protected OutputStream onWrite()
    {
        return new OutputStream()
        {
            private boolean writeOpen = true;

            @Override
            public void close()
            {
                this.writeOpen = false;
                logger.trace("Closed writer after {} bytes.", ByteArrayResource.this.array.size());
            }

            @Override
            public void write(final int byteValue) throws IOException
            {
                if (!this.writeOpen)
                {
                    throw new CoreException("Cannot write to a closed stream");
                }
                ByteArrayResource.this.array.add((byte) (byteValue & BYTE_MASK));
            }
        };
    }
}
