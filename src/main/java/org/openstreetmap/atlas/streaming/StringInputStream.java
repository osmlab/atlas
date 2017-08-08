package org.openstreetmap.atlas.streaming;

import java.io.IOException;
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

    @Override
    public int read() throws IOException
    {
        if (this.index < this.source.length())
        {
            final int result = this.source.charAt(this.index);
            this.index++;
            return result;
        }
        return -1;
    }
}
