package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;

/**
 * @author lcram
 */
public class ConcurrentResourceCacheTest
{
    private static class DebugCachingStrategy implements CachingStrategy
    {
        static final String NAME = "DebugCachingStrategy";

        private final Set<URI> cacheContains = Collections
                .newSetFromMap(new ConcurrentHashMap<URI, Boolean>());

        @Override
        public Optional<Resource> attemptFetch(final URI resourceURI,
                final Function<URI, Optional<Resource>> defaultFetcher)
        {
            if (this.cacheContains.contains(resourceURI))
            {
                return Optional.of(new StringResource(RESOURCE_CONTENTS));
            }
            this.cacheContains.add(resourceURI);
            return Optional.empty();
        }

        public boolean cacheContains(final URI uri)
        {
            return this.cacheContains.contains(uri);
        }

        @Override
        public String getName()
        {
            return NAME;
        }

        @Override
        public void invalidate()
        {
            this.cacheContains.clear();
        }

        @Override
        public void invalidate(final URI resourceURI)
        {
            this.cacheContains.remove(resourceURI);
        }
    }

    private static final String RESOURCE_CONTENTS = "hello world";

    @Test
    public void testCache()
    {
        final Function<URI, Optional<Resource>> fetcher = uri -> Optional
                .of(new StringResource(RESOURCE_CONTENTS));

        final DebugCachingStrategy strategy = new DebugCachingStrategy();
        Assert.assertEquals(DebugCachingStrategy.NAME, strategy.getName());
        final ConcurrentResourceCache cache = new ConcurrentResourceCache(strategy, fetcher);
        Assert.assertEquals(cache.getStrategyName(), strategy.getName());

        final URI foo = URI.create("scheme://foo");
        Assert.assertFalse(strategy.cacheContains(foo));
        Optional<Resource> fromCache = cache.get(foo);
        Assert.assertTrue(strategy.cacheContains(foo));
        Assert.assertTrue(fromCache.isPresent());
        Assert.assertEquals("hello world", fromCache.get().all());

        final URI bar = URI.create("scheme://bar");
        Assert.assertFalse(strategy.cacheContains(bar));
        fromCache = cache.get(bar);
        Assert.assertTrue(strategy.cacheContains(bar));
        Assert.assertTrue(fromCache.isPresent());
        Assert.assertEquals("hello world", fromCache.get().all());

        cache.invalidate(bar);
        Assert.assertFalse(strategy.cacheContains(bar));

        cache.invalidate();
        Assert.assertFalse(strategy.cacheContains(foo));
        Assert.assertFalse(strategy.cacheContains(bar));
    }
}
