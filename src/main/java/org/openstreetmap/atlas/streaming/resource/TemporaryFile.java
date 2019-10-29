package org.openstreetmap.atlas.streaming.resource;

import java.io.Closeable;

/**
 * @author matthieun
 */
public class TemporaryFile extends File implements Closeable
{
    TemporaryFile(final java.io.File file)
    {
        super(file);
    }

    @Override
    public void close()
    {
        this.delete();
    }
}
