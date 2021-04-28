package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.NamespaceCachingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The threadsafe {@link ResourceCache} implementation. There is a caveat, related to the fact that
 * some caching strategies utilize system-wide global state e.g. {@link NamespaceCachingStrategy}
 * uses the tmp filesystem. In doing so it becomes impossible to guarantee concurrency safety from
 * within the {@link ConcurrentResourceCache} alone.<br>
 * {@link ConcurrentResourceCache} needs a specified {@link CachingStrategy} and default fetching
 * {@link Function} at creation time. The cache then loads a resource using a given {@link URI}.
 * Since using {@link URI} objects can often be cumbersome, users of this class are encouraged to
 * extend it and overload the {@link ConcurrentResourceCache#get} method to take more convenient
 * parameters.
 * </p>
 *
 * @author lcram
 */
public class ConcurrentResourceCache implements ResourceCache
{
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentResourceCache.class);

    private final CachingStrategy cachingStrategy;
    private final Function<URI, Optional<Resource>> fetcher;
    private final UUID cacheID;

    /**
     * Create a new {@link ConcurrentResourceCache} with the given fetcher and strategy.
     *
     * @param cachingStrategy
     *            the caching strategy
     * @param fetcher
     *            the default fetcher
     */
    public ConcurrentResourceCache(final CachingStrategy cachingStrategy,
            final Function<URI, Optional<Resource>> fetcher)
    {
        this.cachingStrategy = cachingStrategy;
        this.fetcher = fetcher;
        this.cacheID = UUID.randomUUID();
        logger.info("Initialized cache {} with ID {}", this.getClass().getName(), this.cacheID);
    }

    @Override
    public Optional<Resource> get(final URI resourceURI)
    {
        Optional<Resource> cachedResource;

        // We must synchronize the application of the caching strategy since we cannot guarantee
        // that the strategy does not utilize internal global state.
        synchronized (this)
        {
            cachedResource = this.cachingStrategy.attemptFetch(resourceURI, this.fetcher);
        }

        if (cachedResource.isEmpty())
        {
            logger.warn("CacheID {}: cache fetch of {} failed, falling back to default fetcher...",
                    this.cacheID, resourceURI);

            // We must also synchronize the application of the fetcher, since it may rely on state
            // shared by the calling threads.
            synchronized (this)
            {
                cachedResource = this.fetcher.apply(resourceURI);
            }
        }

        return cachedResource;
    }

    /**
     * Get the name of the backing {@link CachingStrategy}.
     *
     * @return the name
     */
    public String getStrategyName()
    {
        return this.cachingStrategy.getName();
    }

    @Override
    public void invalidate()
    {
        logger.info("CacheID {}: invalidating cache", this.cacheID);
        // Synchronize invalidation with the same lock used to fetch and cache. This prevents
        // invalidation corruption.
        synchronized (this)
        {
            this.cachingStrategy.invalidate();
        }
    }

    @Override
    public void invalidate(final URI resourceURI)
    {
        logger.info("CacheID {}: invalidating resource {}", this.cacheID, resourceURI);
        // Synchronize invalidation with the same lock used to fetch and cache. This prevents
        // invalidation corruption.
        synchronized (this)
        {
            this.cachingStrategy.invalidate(resourceURI);
        }
    }

    /**
     * Get a {@link UUID} for this cache instance. This is useful for logging.
     *
     * @return The cache instance {@link UUID}
     */
    protected UUID getCacheID()
    {
        return this.cacheID;
    }
}
