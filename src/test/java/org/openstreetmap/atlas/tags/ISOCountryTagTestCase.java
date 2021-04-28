package org.openstreetmap.atlas.tags;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

import com.google.common.collect.Lists;

/**
 * Test case for verifying if ISOCountryTag's from method works
 *
 * @author cstaylor
 * @author ahsieh
 */
public class ISOCountryTagTestCase
{
    @Test
    public void testAll()
    {
        final TestTaggable testable = new TestTaggable(ISOCountryTag.KEY, "PRK,KOR,USA");

        final List<String> countries = Lists.newArrayList(ISOCountryTag.all(testable));
        Assert.assertTrue(countries.size() == 3);
        Assert.assertTrue(countries.contains("PRK"));
        Assert.assertTrue(countries.contains("KOR"));
        Assert.assertTrue(countries.contains("USA"));
    }

    @Test
    public void testFilterAllIn()
    {
        final Collection<String> countries = new HashSet<>();
        countries.add("KOR");
        countries.add("USA");
        final Predicate<Taggable> checkMe = ISOCountryTag.allIn(countries);

        Assert.assertFalse(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "PRK,KOR,USA,RUS")));
        Assert.assertFalse(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "PRK,KOR,USA")));
        Assert.assertTrue(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "KOR,USA")));
        Assert.assertTrue(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "USA,KOR")));
        Assert.assertTrue(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "KOR")));
        Assert.assertFalse(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "PRK")));
        Assert.assertFalse(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "PRK,RUS")));
    }

    @Test
    public void testFilterIsIn()
    {
        final Predicate<Taggable> checkMe = ISOCountryTag.isIn("KOR");
        Assert.assertTrue(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "KOR")));
        Assert.assertFalse(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "PRK")));
    }

    @Test
    public void testFromOrdering()
    {
        // We expect PRK to be the country code because it's the first in the list
        final TestTaggable testable = new TestTaggable(ISOCountryTag.KEY, "PRK,KOR,USA");

        final Iterable<String> possibleCountry = ISOCountryTag.all(testable);
        Assert.assertTrue(Iterables.size(possibleCountry) > 0);
        Assert.assertEquals("PRK", possibleCountry.iterator().next());
    }

    @Test
    public void testKorea()
    {
        final TestTaggable testable = new TestTaggable(ISOCountryTag.KEY, "KOR");

        final List<String> countries = Lists.newArrayList(ISOCountryTag.all(testable));
        Assert.assertTrue(countries.size() == 1);
        Assert.assertTrue(countries.contains("KOR"));

        final Iterable<String> possibleCountry = ISOCountryTag.all(testable);
        Assert.assertTrue(Iterables.size(possibleCountry) > 0);
        final String country = possibleCountry.iterator().next();
        Assert.assertEquals("KOR", country);
    }
}
