package org.openstreetmap.atlas.utilities.caching;

import java.nio.file.Paths;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.ByteArrayCachingStrategy;

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
    public LocalFileInMemoryCache()
    {
        super(new ByteArrayCachingStrategy(), uri -> new File(uri.getPath()));
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
