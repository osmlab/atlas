package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.ResourceFetchFunction;

/**
 * Caching strategy that always produces a cache miss.
 *
 * @author lcram
 */
public class NoCachingStrategy extends AbstractCachingStrategy
{
    @Override
    public Optional<Resource> attemptFetch(final URI resourceURI,
            final ResourceFetchFunction defaultFetcher)
    {
        return Optional.of(null);
    }
}
