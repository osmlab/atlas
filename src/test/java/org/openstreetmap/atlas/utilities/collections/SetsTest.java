package org.openstreetmap.atlas.utilities.collections;

import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author matthieun
 */
public class SetsTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testWithSets()
    {
        final Set<String> set1 = Sets.hashSet("key1");
        final Set<String> set2 = Sets.hashSet("key2");
        final Set<String> result = Sets.hashSet("key1", "key2");

        Assert.assertEquals(result, Sets.withSets(true, set1, set2));
    }

    @Test
    public void testWithSetsCollision()
    {
        final Set<String> set1 = Sets.hashSet("key");
        final Set<String> set2 = Sets.hashSet("key");

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge sets! Collision on element.");

        Sets.withSets(true, set1, set2);
    }
}
