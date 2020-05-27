package org.openstreetmap.atlas.streaming.resource;

import java.io.Closeable;
import java.nio.file.FileSystem;
import java.nio.file.Path;

/**
 * A special type of {@link File} that automatically cleans up after itself. Please use
 * {@link File#temporary(FileSystem)}, {@link File#temporaryFolder(FileSystem)}, and the related
 * methods to obtain {@link TemporaryFile}s. You may take advantage of the automatic cleanup by
 * using it in a try-with-resources like so:
 *
 * <pre>
 * try (TemporaryFile directory = File.temporaryFolder(fileSystem))
 * {
 *     File foo = directory.child("foo");
 *     // do something with foo
 * }
 * // the temporary directory is automatically deleted here
 * </pre>
 *
 * @author matthieun
 * @author lcram
 */
public class TemporaryFile extends File implements Closeable
{
    /**
     * Construct a new {@link TemporaryFile} with the given {@link Path}. All parent directories
     * will be automatically created.
     *
     * @param path
     *            the path
     */
    TemporaryFile(final Path path)
    {
        super(path);
    }

    /**
     * Clean up this {@link TemporaryFile}. If it is a regular file, simply delete it. If it is a
     * directory, we will delete it and all its contents recursively.
     */
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
