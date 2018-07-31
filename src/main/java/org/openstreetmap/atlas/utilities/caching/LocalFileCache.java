package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;

/**
 * An example of how to extend the {@link SimpleResourceCache} to enhance functionality. This class
 * caches local files, and abstracts the the messiness of file URIs behind a cleaner
 * {@link LocalFileCache#withPath(String)} interface. Note that it still must override the other
 * "with" builder methods, so that the builder pattern does not throw up type errors.
 *
 * @author lcram
 */
public class LocalFileCache extends SimpleResourceCache
{
    public LocalFileCache()
    {
        super();
    }

    public LocalFileCache(final URI resourceURI, final CachingStrategy cachingStrategy,
            final ResourceFetchFunction fetcher)
    {
        super(resourceURI, cachingStrategy, fetcher);
    }

    @Override
    public LocalFileCache withCachingStrategy(final CachingStrategy strategy)
    {
        this.setCachingStrategy(strategy);
        return this;
    }

    @Override
    public LocalFileCache withFetcher(final ResourceFetchFunction fetcher)
    {
        this.setDefaultFetcher(fetcher);
        return this;
    }

    /**
     * Set the path of the desired resource.
     *
     * @param path
     *            the path to the file
     * @return the configured {@link LocalFileCache}
     */
    public LocalFileCache withPath(final String path)
    {
        final Path localPath = Paths.get(path).toAbsolutePath();
        this.setResourceURI(localPath.toUri());
        return this;
    }

    @Override
    public LocalFileCache withResourceURI(final URI resourceURI)
    {
        this.setResourceURI(resourceURI);
        return this;
    }
}
