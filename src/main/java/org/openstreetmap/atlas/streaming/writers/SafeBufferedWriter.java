package org.openstreetmap.atlas.streaming.writers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author matthieun
 */
public class SafeBufferedWriter extends BufferedWriter
{

    public SafeBufferedWriter(final Writer out)
    {
        super(out);
    }

    @Override
    public void write(final String string)
    {
        try
        {
            super.write(string);
        }
        catch (final IOException e)
        {
            throw new CoreException("Could not write.", e);
        }
    }

    public void writeLine(final String line)
    {
        write(line + "\n");
    }
}
