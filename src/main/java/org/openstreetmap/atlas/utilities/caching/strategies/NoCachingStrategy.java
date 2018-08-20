package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * Caching strategy that always produces a cache miss.
 *
 * @author lcram
 */
public class NoCachingStrategy extends AbstractCachingStrategy
{
    @Override
    public Optional<Resource> attemptFetch(final URI resourceURI,
            final Function<URI, Resource> defaultFetcher)
    {
        return Optional.empty();
    }

    @Override
    public String getName()
    {
        return "NoCachingStrategy";
    }

    @Override
    public void invalidate()
    {
        return;
    }
}
