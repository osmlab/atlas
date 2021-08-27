package org.openstreetmap.atlas.tags.filters;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class ConfiguredTaggableFilterTest
{
    private ConfiguredTaggableFilter filter;

    @Before
    public void prepare()
    {
        final Resource resource = new InputStreamResource(() -> ConfiguredTaggableFilterTest.class
                .getResourceAsStream("test-filtering.json"));
        final Configuration configuration = new StandardConfiguration(resource);
        this.filter = new ConfiguredTaggableFilter(configuration);
    }

    @Test
    public void testConfigured()
    {
        final Taggable valid1 = Taggable.with("barrier", "yes");
        final Taggable valid2 = Taggable.with("highway", "motorway_junction");
        final Taggable valid3 = Taggable.with("railway", "level_crossing", "public_transport",
                "platform");
        final Taggable valid4 = Taggable.with("barrier", "yes", "oneway", "yes");

        final Taggable invalid1 = Taggable.with("barrier", "yes", "noexit", "yes");
        final Taggable invalid2 = Taggable.with("highway", "motorway_junction", "noexit", "yes");
        final Taggable invalid3 = Taggable.with("highway", "pedestrian");
        final Taggable invalid4 = Taggable.with();
        final Taggable invalid5 = Taggable.with("barrier", "yes", "oneway", "reversed");

        Assert.assertTrue(this.filter.test(valid1));
        Assert.assertTrue(this.filter.test(valid2));
        Assert.assertTrue(this.filter.test(valid3));
        Assert.assertTrue(this.filter.test(valid4));

        Assert.assertFalse(this.filter.test(invalid1));
        Assert.assertFalse(this.filter.test(invalid2));
        Assert.assertFalse(this.filter.test(invalid3));
        Assert.assertFalse(this.filter.test(invalid4));
        Assert.assertFalse(this.filter.test(invalid5));
    }

    @Test
    public void testDefaultOsmosisWayConfiguration()
    {
        final Resource wayResource = new InputStreamResource(
                () -> getClass().getClassLoader().getResourceAsStream(
                        "org/openstreetmap/atlas/geography/atlas/pbf/osm-pbf-way.json"));
        final Configuration wayConfiguration = new StandardConfiguration(wayResource);
        final ConfiguredTaggableFilter wayFilter = new ConfiguredTaggableFilter(wayConfiguration);

        final Taggable boundary = Taggable.with("boundary", "administrative");
        final Taggable hirez = Taggable.with("hires", "yes");

        Assert.assertTrue(wayFilter.test(boundary));
        Assert.assertFalse(wayFilter.test(hirez));
    }
}
