package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * A special case of {@link NamespaceCachingStrategy} that uses a predefined, global namespace. This
 * means that all instances of {@link GlobalNamespaceCachingStrategy} will share the same underlying
 * contents. From this it follows that fetches and invalidates will manifest across instances. To
 * prevent concurrency issues, the global namespace is locked using a class lock. It is worth noting
 * that this still does not protect the namespace's integrity from multiple JVMs running concurrent
 * {@link GlobalNamespaceCachingStrategy} objects.
 *
 * @author lcram
 */
public class GlobalNamespaceCachingStrategy extends NamespaceCachingStrategy
{
    /*
     * This is a random SHA256 hash. Collisions with this namespace are astronomically unlikely (due
     * to the 256 bits of entropy).
     */
    private static final String GLOBAL_NAMESPACE = "3707740A818531237051A0F1E086CF701E2C38483675FCD1AAD8F5C5C33F19BC";

    public GlobalNamespaceCachingStrategy()
    {
        super(GLOBAL_NAMESPACE);
    }

    @Override
    public Optional<Resource> attemptFetch(final URI resourceURI,
            final Function<URI, Optional<Resource>> defaultFetcher)
    {
        synchronized (GlobalNamespaceCachingStrategy.class)
        {
            return super.attemptFetch(resourceURI, defaultFetcher);
        }
    }

    @Override
    public String getName()
    {
        return "GlobalNamespaceCachingStrategy";
    }

    @Override
    public void invalidate()
    {
        synchronized (GlobalNamespaceCachingStrategy.class)
        {
            super.invalidate();
        }
    }

    @Override
    public void invalidate(final URI resourceURI)
    {
        synchronized (GlobalNamespaceCachingStrategy.class)
        {
            super.invalidate(resourceURI);
        }
    }
}
