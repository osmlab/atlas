package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class FeatureChangeTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAfterViewIsFull()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY, null, null));
        Assert.assertFalse(featureChange1.afterViewIsFull());
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, new CompleteArea(
                123L, Polygon.SILICON_VALLEY, Maps.hashMap("key1", "value2"), Sets.hashSet(123L)));
        Assert.assertTrue(featureChange2.afterViewIsFull());
    }

    @Test
    public void testTags()
    {
        final String key = "key1";
        final String value = "value1";
        final Map<String, String> tags = Maps.hashMap(key, value, "key2", "value2");
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.CENTER, tags, null));
        Assert.assertEquals(new HashMap<>(tags), featureChange.getTags());
        Assert.assertEquals(value, featureChange.getTag(key).get());
        Assert.assertTrue(featureChange.toString().contains(tags.toString()));
    }
}
