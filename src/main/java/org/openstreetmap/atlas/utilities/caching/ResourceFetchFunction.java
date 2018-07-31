package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;

import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * A {@link ResourceFetchFunction} is any function that takes a {@link URI} and returns the
 * {@link Resource} at that {@link URI}. The function should return null if the resource cannot be
 * retrieved.
 *
 * @author lcram
 */
@FunctionalInterface
public interface ResourceFetchFunction
{
    /**
     * Fetch the resource specified by the given URI.
     *
     * @param resourceURI
     *            the {@link URI} of the desired {@link Resource}
     * @return the {@link Resource}, or {@code null} if the fetch fails
     */
    Resource fetch(URI resourceURI);
}
