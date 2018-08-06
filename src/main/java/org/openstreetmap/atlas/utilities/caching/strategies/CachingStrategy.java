package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * Interface definition for a caching strategy. A caching strategy must provide a method for
 * obtaining a resource based on a {@link URI}.
 *
 * @author lcram
 */
public interface CachingStrategy
{
    /**
     * Attempt to fetch the resource located at the given URI.
     *
     * @param resourceURI
     *            the {@link URI} if the desired {@link Resource}
     * @param defaultFetcher
     *            the initial {@link Function} used to populate the cache
     * @return the {@link Resource} wrapped in an {@link Optional}
     */
    Optional<Resource> attemptFetch(URI resourceURI, Function<URI, Resource> defaultFetcher);

    /**
     * Get a strategy name for logging purposes.
     *
     * @return the strategy name
     */
    String getName();

    /**
     * Invalidate the contents of this strategy. The contract of this method is the following: a
     * {@link URI} that produces a cache hit on an {@link CachingStrategy#attemptFetch} before an
     * {@link CachingStrategy#invalidate} call must produce a cache miss on the first
     * {@link CachingStrategy#attemptFetch} after an {@link CachingStrategy#invalidate} call.
     */
    void invalidate();
}
