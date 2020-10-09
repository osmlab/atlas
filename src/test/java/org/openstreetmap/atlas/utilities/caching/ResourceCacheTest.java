package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;

/**
 * @author lcram
 */
public class ResourceCacheTest
{
    private static class TestCache implements ResourceCache
    {
        @Override
        public Optional<Resource> get(final URI resourceURI)
        {
            return Optional.of(new StringResource(RESOURCE_CONTENTS));
        }

        @Override
        public void invalidate()
        {
            // NOOP
        }

        @Override
        public void invalidate(final URI resourceURI)
        {
            // NOOP
        }
    }

    private static final String RESOURCE_CONTENTS = "hello world";

    @Test
    public void test()
    {
        final ResourceCache cache = new TestCache();
        final Optional<Resource> fromCache = cache.get("scheme://foo");
        Assert.assertTrue(fromCache.isPresent());
        Assert.assertEquals(RESOURCE_CONTENTS, fromCache.get().all());
    }
}
