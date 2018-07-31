package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openstreetmap.atlas.utilities.caching.strategies.ByteArrayCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;

/**
 * An example of how to extend the {@link ResourceCache} to enhance functionality. This class caches
 * local files in memory, and abstracts the the messiness of file URIs behind a cleaner
 * {@link LocalFileInMemoryCache#withPath(String)} interface. Note that it still must override the
 * other "with" builder methods, so that the builder pattern does not throw up type errors. The
 * {@link LocalFileInMemoryCache} forces the caching strategy to be
 * {@link ByteArrayCachingStrategy}.
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
        super(null, new ByteArrayCachingStrategy(), null);
    }

    /**
     * Create a new {@link LocalFileInMemoryCache} with a given resource URI and fetcher.
     *
     * @param resourceURI
     *            the default resource {@link URI}
     * @param fetcher
     *            the default {@link ResourceFetchFunction}
     */
    public LocalFileInMemoryCache(final URI resourceURI, final ResourceFetchFunction fetcher)
    {
        super(resourceURI, new ByteArrayCachingStrategy(), fetcher);
    }

    @Override
    public LocalFileInMemoryCache withCachingStrategy(final CachingStrategy strategy)
    {
        throw new UnsupportedOperationException(
                "This cache does not support alternate caching strategies.");
    }

    @Override
    public LocalFileInMemoryCache withFetcher(final ResourceFetchFunction fetcher)
    {
        this.setDefaultFetcher(fetcher);
        return this;
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
        this.setResourceURI(resourceURI);
        return this;
    }
}
