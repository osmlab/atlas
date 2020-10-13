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
public class NoCachingStrategyTest
{
    private static final String RESOURCE_CONTENTS = "hello world";

    @Test
    public void test()
    {
        final Function<URI, Optional<Resource>> fetcher = uri -> Optional
                .of(new StringResource(RESOURCE_CONTENTS));

        final NoCachingStrategy strategy = new NoCachingStrategy();
        Assert.assertEquals(strategy.getName(), strategy.getName());

        final URI foo = URI.create("scheme://foo");
        final Optional<Resource> resource = strategy.attemptFetch(foo, fetcher);
        Assert.assertTrue(resource.isEmpty());

        // NOOPs
        strategy.invalidate(foo);
        strategy.invalidate();
    }
}
