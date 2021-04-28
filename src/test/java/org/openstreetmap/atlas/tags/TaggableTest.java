package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author matthieun
 */
public class TaggableTest
{
    @Test
    public void hasAtLeastOneOfTest()
    {
        final Taggable taggable = Taggable.with("highway", "primary", "boundary", "administrative");
        Assert.assertTrue(taggable.hasAtLeastOneOf(Maps.hashMap("highway", "*")));
        Assert.assertTrue(taggable.hasAtLeastOneOf(Maps.hashMap("highway", "primary")));
        Assert.assertFalse(taggable.hasAtLeastOneOf(Maps.hashMap("highway", "secondary")));
        Assert.assertTrue(taggable
                .hasAtLeastOneOf(Maps.hashMap("highway", "primary", "boundary", "administrative")));
        Assert.assertTrue(taggable
                .hasAtLeastOneOf(Maps.hashMap("highway", "primary", "boundary", "unknown")));
        Assert.assertFalse(taggable.hasAtLeastOneOf(Maps.hashMap()));
    }
}
