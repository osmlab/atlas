package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.ByteArrayCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;

/**
 * An example of how to extend the {@link ResourceCache} to enhance functionality. This class caches
 * local files in memory, and abstracts the the messiness of file URIs behind a cleaner
 * {@link LocalFileInMemoryCache#withPath(String)} interface. Note that it still must override the
 * other "with" builder methods, so that the builder pattern does not throw up type errors. The
 * {@link LocalFileInMemoryCache} forces the caching strategy to be
 * {@link ByteArrayCachingStrategy}, and forces the default fetcher to simply load a local file.
 *
 * @author lcram
 */
public class LocalFileInMemoryCache extends ResourceCache
{
    /**
     * Create a new {@link LocalFileInMemoryCache} with uninitialized default resource and fetcher.
     */
    public LocalFileInMemoryCache()
    {
        this(null);
    }

    /**
     * Create a new {@link LocalFileInMemoryCache} with a given resource URI.
     *
     * @param resourceURI
     *            the default resource {@link URI}
     */
    public LocalFileInMemoryCache(final URI resourceURI)
    {
        super(resourceURI, new ByteArrayCachingStrategy(), uri -> new File(uri.getPath()));
    }

    @Override
    public LocalFileInMemoryCache withCachingStrategy(final CachingStrategy strategy)
    {
        throw new UnsupportedOperationException(
                "This cache does not support alternate caching strategies.");
    }

    @Override
    public LocalFileInMemoryCache withFetcher(final Function<URI, Resource> fetcher)
    {
        throw new UnsupportedOperationException(
                "This cache does not support alternate default fetchers.");
    }

    /**
     * Set the path of the desired resource.
     *
     * @param path
     *            the path to the file
     * @return the configured {@link LocalFileInMemoryCache}
     */
    public LocalFileInMemoryCache withPath(final String path)
    {
        final Path localPath = Paths.get(path).toAbsolutePath();
        this.setResourceURI(localPath.toUri());
        return this;
    }

    @Override
    public LocalFileInMemoryCache withResourceURI(final URI resourceURI)
    {
        throw new UnsupportedOperationException(
                "This cache does not support setting the resource URI directly. Use the withPath method instead.");
    }
}
