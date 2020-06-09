package org.openstreetmap.atlas.streaming;

import java.io.InputStream;

/**
 * An {@link InputStream} that reads from a {@link String} for convenience
 *
 * @author matthieun
 */
public class StringInputStream extends InputStream
{
    private final String source;
    private int index;

    public StringInputStream(final String source)
    {
        this.source = source;
        this.index = 0;
    }

    /**
     * Get the source {@link String} for this {@link StringInputStream}.
     * 
     * @return the source {@link String}
     */
    public String getSource()
    {
        return this.source;
    }

    @Override
    public int read()
    {
        if (this.index < this.source.length())
        {
            final int result = this.source.charAt(this.index);
            this.index++;
            return result;
        }
        return -1;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length)
    {
        int requestedReadLength = length;
        if (this.index >= this.source.length())
        {
            return -1;
        }

        final int availableToRead = this.source.length() - this.index;
        if (requestedReadLength > availableToRead)
        {
            requestedReadLength = availableToRead;
        }
        if (requestedReadLength <= 0)
        {
            return 0;
        }

        final byte[] stringBytes = this.source.getBytes();
        System.arraycopy(stringBytes, this.index, buffer, offset, requestedReadLength);

        this.index += requestedReadLength;
        return requestedReadLength;
    }
}
