package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for the {@link Iterables#addAll(java.util.Collection, Iterable)} method
 *
 * @author cstaylor
 */
public class IterablesAddAllTestCase
{
    @Test
    public void testAddNone()
    {
        Assert.assertFalse(Iterables.addAll(new ArrayList<>(), Iterables.from()));
    }

    @Test
    public void testAddOne()
    {
        Assert.assertTrue(Iterables.addAll(new ArrayList<>(), Iterables.from("Test")));
    }

    @Test
    public void testSameToList()
    {
        final List<String> items = Iterables.asList(new String[] { "Test" });
        Assert.assertTrue(Iterables.addAll(items, Iterables.from("Test")));
    }

    @Test
    public void testSameToSet()
    {
        final Set<String> items = Iterables.asSet(new String[] { "Test" });
        Assert.assertFalse(Iterables.addAll(items, Iterables.from("Test")));
    }
}
