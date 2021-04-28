package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;

/**
 * @author lcram
 */
public class ByteArrayCachingStrategyTest
{
    private static final String RESOURCE_CONTENTS = "hello world";
    private static final URI DOES_NOT_EXIST = URI.create("scheme://DNE");
    private static final int NEW_ARRAY_SIZE = 1024;

    @Test
    public void test()
    {
        final Function<URI, Optional<Resource>> fetcher = uri ->
        {
            if (DOES_NOT_EXIST.toString().equals(uri.toString()))
            {
                return Optional.empty();
            }
            return Optional.of(new StringResource(RESOURCE_CONTENTS));
        };

        final ByteArrayCachingStrategy strategy = new ByteArrayCachingStrategy();
        Assert.assertEquals(strategy.getName(), strategy.getName());

        // the cache map should be empty here
        Assert.assertTrue(strategy.getResourceCache().isEmpty());
        final URI foo = URI.create("scheme://foo");
        final Optional<Resource> resource = strategy.attemptFetch(foo, fetcher);
        Assert.assertTrue(resource.isPresent());
        Assert.assertEquals(RESOURCE_CONTENTS, resource.get().all());
        // the cache map should now have a single entry
        Assert.assertEquals(1, strategy.getResourceCache().size());

        // now use the exact resource size
        strategy.useExactResourceSize();
        final URI bar = URI.create("scheme://bar");
        final Optional<Resource> anotherResource = strategy.attemptFetch(bar, fetcher);
        Assert.assertTrue(anotherResource.isPresent());
        Assert.assertEquals(RESOURCE_CONTENTS, anotherResource.get().all());
        // the cache map should now have two entries
        Assert.assertEquals(2, strategy.getResourceCache().size());

        // remove foo from cache
        strategy.invalidate(foo);
        Assert.assertEquals(1, strategy.getResourceCache().size());

        // add foo back with a set array size
        strategy.withInitialArraySize(NEW_ARRAY_SIZE);
        final Optional<Resource> fooAgain = strategy.attemptFetch(foo, fetcher);
        Assert.assertTrue(fooAgain.isPresent());
        Assert.assertEquals(RESOURCE_CONTENTS, fooAgain.get().all());
        // the cache map should now have two entries again
        Assert.assertEquals(2, strategy.getResourceCache().size());

        // clear cache
        strategy.invalidate();
        Assert.assertTrue(strategy.getResourceCache().isEmpty());

        // fetch a non-existent resource
        final Optional<Resource> shouldBeEmpty = strategy.attemptFetch(DOES_NOT_EXIST, fetcher);
        Assert.assertTrue(shouldBeEmpty.isEmpty());
    }
}
