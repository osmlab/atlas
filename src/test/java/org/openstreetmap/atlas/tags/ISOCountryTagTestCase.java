package org.openstreetmap.atlas.tags;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.locale.IsoCountry;
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

        final List<IsoCountry> isoCountries = Lists.newArrayList(ISOCountryTag.all(testable));
        Assert.assertTrue(isoCountries.size() == 3);
        Assert.assertTrue(isoCountries.contains(IsoCountry.forCountryCode("PRK").get()));
        Assert.assertTrue(isoCountries.contains(IsoCountry.forCountryCode("KOR").get()));
        Assert.assertTrue(isoCountries.contains(IsoCountry.forCountryCode("USA").get()));
    }

    @Test
    public void testBestKorea()
    {
        final TestTaggable testable = new TestTaggable(ISOCountryTag.KEY, "PRK");

        final List<IsoCountry> isoCountries = Lists.newArrayList(ISOCountryTag.all(testable));
        Assert.assertTrue(isoCountries.size() == 1);
        Assert.assertTrue(isoCountries.contains(IsoCountry.forCountryCode("PRK").get()));

        final Iterable<IsoCountry> possibleCountry = ISOCountryTag.all(testable);
        Assert.assertTrue(Iterables.size(possibleCountry) > 0);
        Assert.assertNotEquals(IsoCountry.forCountryCode("KOR"),
                Optional.of(possibleCountry.iterator().next()));
        Assert.assertEquals(IsoCountry.forCountryCode("PRK"),
                Optional.of(possibleCountry.iterator().next()));
    }

    @Test
    public void testFilterAllIn()
    {
        final Predicate<Taggable> checkMe = ISOCountryTag.allIn(
                IsoCountry.forCountryCode("KOR").get(), IsoCountry.forCountryCode("USA").get());

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
        final Predicate<Taggable> checkMe = ISOCountryTag
                .isIn(IsoCountry.forCountryCode("KOR").get());
        Assert.assertTrue(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "KOR")));
        Assert.assertFalse(checkMe.test(new TestTaggable(ISOCountryTag.KEY, "PRK")));
    }

    @Test
    public void testFromOrdering()
    {
        // We expect PRK to be the country code because it's the first in the list
        final TestTaggable testable = new TestTaggable(ISOCountryTag.KEY, "PRK,KOR,USA");

        final Iterable<IsoCountry> possibleCountry = ISOCountryTag.all(testable);
        Assert.assertTrue(Iterables.size(possibleCountry) > 0);
        Assert.assertEquals(IsoCountry.forCountryCode("PRK"),
                Optional.of(possibleCountry.iterator().next()));
    }

    @Test
    public void testKorea()
    {
        final TestTaggable testable = new TestTaggable(ISOCountryTag.KEY, "KOR");

        final List<IsoCountry> isoCountries = Lists.newArrayList(ISOCountryTag.all(testable));
        Assert.assertTrue(isoCountries.size() == 1);
        Assert.assertTrue(isoCountries.contains(IsoCountry.forCountryCode("KOR").get()));

        final Iterable<IsoCountry> possibleCountry = ISOCountryTag.all(testable);
        Assert.assertTrue(Iterables.size(possibleCountry) > 0);
        final IsoCountry country = possibleCountry.iterator().next();
        Assert.assertEquals(IsoCountry.forCountryCode("KOR"), Optional.of(country));
    }

    @Test
    public void testNoData()
    {
        final TestTaggable testable = new TestTaggable(ISOCountryTag.KEY, "NOPE");

        Assert.assertTrue(Lists.newArrayList(ISOCountryTag.all(testable)).isEmpty());
        Assert.assertFalse(Iterables.size(ISOCountryTag.all(testable)) > 0);
    }
}
