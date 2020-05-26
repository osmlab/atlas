package org.openstreetmap.atlas.streaming.resource;

import java.io.Closeable;
import java.nio.file.Path;

/**
 * @author matthieun
 */
public class TemporaryFile extends File implements Closeable
{
    TemporaryFile(final Path path)
    {
        super(path);
    }

    @Deprecated
    TemporaryFile(final java.io.File file)
    {
        super(file);
    }

    @Deprecated
    TemporaryFile(final String stringPath)
    {
        super(stringPath);
    }

    @Override
    public void close()
    {
        if (this.isDirectory())
        {
            this.deleteRecursively();
        }
        else
        {
            this.delete();
        }
    }
}
