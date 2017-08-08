package org.openstreetmap.atlas.streaming.resource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Write lines to a {@link WritableResource}
 *
 * @author matthieun
 */
public class LineWriter extends BufferedWriter
{
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final WritableResource writableResource;

    public LineWriter(final WritableResource writableResource)
    {
        super(new OutputStreamWriter(writableResource.write(), CHARSET));
        this.writableResource = writableResource;
    }

    public void writeLine(final String line)
    {
        try
        {
            write(line);
            write(LINE_SEPARATOR);
        }
        catch (final IOException e)
        {
            throw new CoreException("Unable to write line to {}", this.writableResource, e);
        }
    }
}
