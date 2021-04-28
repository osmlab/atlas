package org.openstreetmap.atlas.geography.atlas.command;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Helper class to capture the output stream for programmatic review.
 *
 * @author bbreithaupt
 */
public class CaptureOutputStream extends PrintStream
{
    private String log = "";

    public CaptureOutputStream(final OutputStream out)
    {
        super(out);
    }

    public String getLog()
    {
        return this.log;
    }

    @Override
    public void print(final String string)
    {
        this.log = this.log.concat(string);
        super.print(string);
    }

    @Override
    public PrintStream printf(final String format, final Object... args)
    {
        this.log = this.log.concat(String.format(format, args));
        return super.printf(format, args);
    }
}
