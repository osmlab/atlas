package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.ResourceFetchFunction;

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
     *            the initial {@link ResourceFetchFunction} used to populate the cache
     * @return the {@link Resource} wrapped in an {@link Optional}
     */
    Optional<Resource> attemptFetch(URI resourceURI, ResourceFetchFunction defaultFetcher);

    /**
     * Get a strategy name for logging purposes.
     *
     * @return the strategy name
     */
    String getName();
}
