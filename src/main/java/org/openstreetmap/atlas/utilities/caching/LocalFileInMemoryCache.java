package org.openstreetmap.atlas.utilities.caching;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.ByteArrayCachingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example of how to extend the {@link ConcurrentResourceCache} to enhance functionality. This
 * class caches local files in memory, and abstracts the the messiness of file URIs behind a cleaner
 * interface. The {@link LocalFileInMemoryCache} forces the caching strategy to be
 * {@link ByteArrayCachingStrategy}, and forces the default fetcher to simply load a local file.
 *
 * @author lcram
 */
public class LocalFileInMemoryCache extends ConcurrentResourceCache
{
    private static final Logger logger = LoggerFactory.getLogger(LocalFileInMemoryCache.class);

    /**
     * Create a new {@link LocalFileInMemoryCache} with the default {@link FileSystem}. See
     * {@link FileSystems#getDefault()} for more information.
     */
    public LocalFileInMemoryCache()
    {
        this(FileSystems.getDefault());
    }

    /**
     * Create a new {@link LocalFileInMemoryCache} with the given {@link FileSystem}.
     * 
     * @param fileSystem
     *            the {@link FileSystem} to use for file loading
     */
    public LocalFileInMemoryCache(final FileSystem fileSystem)
    {
        super(new ByteArrayCachingStrategy(), uri ->
        {
            final File file = new File(uri.getPath(), fileSystem);
            if (!file.exists())
            {
                logger.warn("File {} does not exist!", file);
                return Optional.empty();
            }
            return Optional.of(file);
        });
    }

    /**
     * Attempt to get the resource specified by the given path.
     *
     * @param path
     *            the path to the desired resource
     * @return an {@link Optional} wrapping the {@link Resource}
     */
    @Override
    public Optional<Resource> get(final String path)
    {
        return this.get(Paths.get(path).toAbsolutePath().toUri());
    }
}
