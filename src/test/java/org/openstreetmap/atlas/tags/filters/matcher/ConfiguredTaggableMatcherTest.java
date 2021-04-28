package org.openstreetmap.atlas.tags.filters.matcher;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

/**
 * @author lcram
 */
public class ConfiguredTaggableMatcherTest
{
    private ConfiguredTaggableMatcher matcher;

    @Before
    public void prepare()
    {
        final Resource resource = new InputStreamResource(() -> ConfiguredTaggableMatcherTest.class
                .getResourceAsStream("test-matching.json"));
        final Configuration configuration = new StandardConfiguration(resource);
        this.matcher = new ConfiguredTaggableMatcher(configuration);
    }

    @Test
    public void test()
    {
        final Taggable valid1 = Taggable.with("foo", "bar", "hello", "world2");
        final Taggable valid2 = Taggable.with("foo", "bar1", "baz", "bat", "hello", "world2");

        final Taggable invalid1 = Taggable.with("barrier", "yes", "noexit", "yes");
        final Taggable invalid2 = Taggable.with("foo", "bar", "baz", "bat");

        Assert.assertTrue(this.matcher.test(valid1));
        Assert.assertTrue(this.matcher.test(valid2));

        Assert.assertFalse(this.matcher.test(invalid1));
        Assert.assertFalse(this.matcher.test(invalid2));
    }

}
