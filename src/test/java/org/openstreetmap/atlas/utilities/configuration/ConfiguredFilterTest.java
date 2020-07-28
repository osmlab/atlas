package org.openstreetmap.atlas.utilities.configuration;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author lcram
 * @author matthieun
 */
public class ConfiguredFilterTest
{
    private static final String NAMESAKE_JSON = ConfiguredFilterTest.class.getSimpleName()
            + FileSuffix.JSON;

    @Test
    public void testFilter()
    {
        final ConfiguredFilter junctionRoundaboutFilter = get("junctionRoundaboutFilter");
        final ConfiguredFilter dummyFilter = get("dummyFilter");
        final ConfiguredFilter tagFilterOnly = get("tagFilterOnly");
        final ConfiguredFilter defaultFilter = get("I am not there");
        final ConfiguredFilter nothingGoesThroughFilter = get("nothingGoesThroughFilter");
        final ConfiguredFilter unsafePredicateFilter = get("unsafePredicateFilter");

        final Edge edge = new CompleteEdge(123L, null, Maps.hashMap("junction", "roundabout"), null,
                null, null);

        Assert.assertTrue(junctionRoundaboutFilter.test(edge));
        Assert.assertFalse(dummyFilter.test(edge));
        Assert.assertTrue(tagFilterOnly.test(edge));
        Assert.assertTrue(defaultFilter.test(edge));
        Assert.assertFalse(nothingGoesThroughFilter.test(edge));
        Assert.assertTrue(unsafePredicateFilter.test(edge));
    }

    @Test
    public void testIsNoExpansion()
    {
        Assert.assertTrue(get("nothingGoesThroughFilter").isNoExpansion());
    }

    private ConfiguredFilter get(final String name)
    {
        final Configuration configuration = new StandardConfiguration(new InputStreamResource(
                () -> ConfiguredFilterTest.class.getResourceAsStream(NAMESAKE_JSON)));
        return ConfiguredFilter.from(name, configuration);
    }
}
