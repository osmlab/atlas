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

    @Override
    public PrintStream printf(final String format, final Object... args)
    {
        this.log = this.log.concat(String.format(format, args));
        return super.printf(format, args);
    }

    public String getLog()
    {
        return this.log;
    }
}
