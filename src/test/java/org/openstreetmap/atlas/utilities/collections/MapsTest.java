package org.openstreetmap.atlas.utilities.collections;

import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author matthieun
 */
public class MapsTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testWithMaps()
    {
        final Map<String, String> map1 = Maps.hashMap("key1", "value1");
        final Map<String, String> map2 = Maps.hashMap("key2", "value2");
        final Map<String, String> result = Maps.hashMap("key1", "value1", "key2", "value2");

        Assert.assertEquals(result, Maps.withMaps(true, map1, map2));
    }

    @Test
    public void testWithMapsCollision()
    {
        final Map<String, String> map1 = Maps.hashMap("key", "value1");
        final Map<String, String> map2 = Maps.hashMap("key", "value2");

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge maps! Collision on key.");

        Maps.withMaps(true, map1, map2);
    }
}
