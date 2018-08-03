package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The threadsafe {@link Resource} cache implementation. The cache needs a specified
 * {@link CachingStrategy} and default fetching {@link Function} at creation time. The cache then
 * loads a resource using a given {@link URI}. Since using raw URIs can often be cumbersome, users
 * of this class are encouraged to extend it and overload the {@link ConcurrentResourceCache#get}
 * method to take more convenient parameters.
 *
 * @author lcram
 */
public class ConcurrentResourceCache
{
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentResourceCache.class);

    private final CachingStrategy cachingStrategy;
    private final Function<URI, Resource> fetcher;

    /**
     * Create a new {@link ConcurrentResourceCache} with the given fetcher and strategy.
     *
     * @param cachingStrategy
     *            the caching strategy
     * @param fetcher
     *            the default fetcher
     */
    public ConcurrentResourceCache(final CachingStrategy cachingStrategy,
            final Function<URI, Resource> fetcher)
    {
        this.cachingStrategy = cachingStrategy;
        this.fetcher = fetcher;
    }

    /**
     * Attempt to get the resource specified by the given string URI.
     *
     * @param resourceURIString
     *            the resource {@link URI} as a {@link String}
     * @return an {@link Optional} wrapping the {@link Resource}
     */
    public Optional<Resource> get(final String resourceURIString)
    {
        return this.get(URI.create(resourceURIString));
    }

    /**
     * Attempt to get the resource specified by the given URI.
     *
     * @param resourceURI
     *            the resource {@link URI}
     * @return an {@link Optional} wrapping the {@link Resource}
     */
    public Optional<Resource> get(final URI resourceURI)
    {
        Optional<Resource> cachedResource;

        // We must synchronize the application of the caching strategy since we cannot guarantee
        // that the strategy does not utilize internal global state.
        synchronized (this)
        {
            cachedResource = this.cachingStrategy.attemptFetch(resourceURI, this.fetcher);
        }

        if (!cachedResource.isPresent())
        {
            logger.warn("Cache fetch failed, falling back to default fetcher...");

            // We must also synchronize the application of the fetcher, since it may rely on state
            // shared by the calling threads.
            synchronized (this)
            {
                cachedResource = Optional.ofNullable(this.fetcher.apply(resourceURI));
            }
        }

        return cachedResource;
    }
}
