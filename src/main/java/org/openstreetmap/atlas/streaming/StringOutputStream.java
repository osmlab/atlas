package org.openstreetmap.atlas.streaming;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link OutputStream} backed by a {@link StringBuilder}
 *
 * @author matthieun
 */
public class StringOutputStream extends OutputStream
{
    private final StringBuilder builder = new StringBuilder();

    @Override
    public String toString()
    {
        return this.builder.toString();
    }

    @Override
    public void write(final int byteValue) throws IOException
    {
        this.builder.append(String.valueOf((char) byteValue));
    }
}
