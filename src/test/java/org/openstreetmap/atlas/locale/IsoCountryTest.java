package org.openstreetmap.atlas.locale;

import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.collections.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test case for IsoCountry
 *
 * @author mcuthbert
 * @author robert_stack
 */
public class IsoCountryTest
{
    private static final Logger logger = LoggerFactory.getLogger(IsoCountry.class);

    @Test
    public void testIsoCountry()
    {
        Assert.assertTrue(IsoCountry.isValidCountryCode("US"));
        Assert.assertFalse(IsoCountry.isValidCountryCode("ZZ"));
        Assert.assertFalse(IsoCountry.isValidCountryCode(null));
        Assert.assertTrue(IsoCountry.isValidCountryCode("USA"));
        Assert.assertFalse(IsoCountry.isValidCountryCode("ZZZ"));
        Assert.assertEquals("USA", IsoCountry.iso3ForIso2("US").get());
        Assert.assertEquals("US", IsoCountry.iso2ForIso3("USA").get());
        Assert.assertEquals("United States", IsoCountry.displayCountry("US").get());
        Assert.assertEquals("United States", IsoCountry.displayCountry("USA").get());

        Assert.assertEquals("South Africa", IsoCountry.displayCountry("ZA").get());
        Assert.assertEquals("South Africa", IsoCountry.displayCountry("ZAF").get());
        Assert.assertEquals("ZAF", IsoCountry.iso3ForIso2("ZA").get());
        Assert.assertEquals("ZA", IsoCountry.iso2ForIso3("ZAF").get());

        Assert.assertEquals(Optional.empty(), IsoCountry.displayCountry(null));
        Assert.assertEquals(Optional.empty(), IsoCountry.iso3ForIso2(null));
        Assert.assertEquals(Optional.empty(), IsoCountry.iso2ForIso3(null));
        Assert.assertEquals(Optional.empty(), IsoCountry.displayCountry("ZZZ"));
        Assert.assertEquals(Optional.empty(), IsoCountry.iso3ForIso2("ZZZ"));
        Assert.assertEquals(Optional.empty(), IsoCountry.iso2ForIso3("ZZZ"));

        Assert.assertEquals(Optional.empty(), IsoCountry.displayCountry("Z"));
        Assert.assertEquals(Optional.empty(), IsoCountry.displayCountry("ZZ"));
        Assert.assertEquals(Optional.empty(), IsoCountry.displayCountry("ZZZ"));
        Assert.assertEquals(Optional.empty(), IsoCountry.displayCountry("ZZZZ"));

        final Optional<IsoCountry> isoCountry1 = IsoCountry.forCountryCode("US");
        Assert.assertEquals("US", isoCountry1.get().getCountryCode());
        Assert.assertEquals("USA", isoCountry1.get().getIso3CountryCode());
        Assert.assertEquals("United States", isoCountry1.get().getDisplayCountry());
        final Optional<IsoCountry> isoCountry2 = IsoCountry.forCountryCode("USA");
        Assert.assertEquals("US", isoCountry2.get().getCountryCode());
        Assert.assertEquals("USA", isoCountry2.get().getIso3CountryCode());
        Assert.assertEquals("United States", isoCountry2.get().getDisplayCountry());
        final Optional<IsoCountry> isoCountry3 = IsoCountry.forCountryCode("ZZ");
        Assert.assertEquals(Optional.empty(), isoCountry3);
        final Optional<IsoCountry> isoCountry4 = IsoCountry.forCountryCode("ZZZ");
        Assert.assertEquals(Optional.empty(), isoCountry4);
        final Optional<IsoCountry> isoCountry5 = IsoCountry.forCountryCode(null);
        Assert.assertEquals(Optional.empty(), isoCountry5);

        Assert.assertTrue(IsoCountry.allCountryCodes().contains("DZ"));

        Assert.assertEquals("USA",
                IsoCountry.forDisplayCountry("United States").get().getIso3CountryCode());
        Assert.assertFalse(IsoCountry.forDisplayCountry("united states").isPresent());
        Assert.assertFalse(IsoCountry.forDisplayCountry("ZZzz zzzzz").isPresent());
        Assert.assertEquals("USA",
                IsoCountry.forDisplayCountryIgnoreCase("united states").get().getIso3CountryCode());
        Assert.assertEquals("USA",
                IsoCountry.forDisplayCountryIgnoreCase("UnITeD STatEs").get().getIso3CountryCode());
        Assert.assertEquals("USA", IsoCountry.forDisplayCountryClosestMatch("UnitedStates").get()
                .getIso3CountryCode());
        Assert.assertEquals("USA", IsoCountry.forDisplayCountryClosestMatch("united States").get()
                .getIso3CountryCode());
        Assert.assertEquals("USA", IsoCountry.forDisplayCountryClosestMatch("united stats").get()
                .getIso3CountryCode());
        Assert.assertEquals("GBR", IsoCountry.forDisplayCountryClosestMatch("unitde kindgdom").get()
                .getIso3CountryCode());
        Assert.assertFalse(IsoCountry.forDisplayCountryIgnoreCase("ZZzz zzzzz").isPresent());

        Assert.assertTrue(IsoCountry.allDisplayCountries().contains("United States"));

        Assert.assertEquals(Lists.arrayList("Sweden", "Macao", "Mexico"),
                IsoCountry.forDisplayCountryTopMatches(3, "abcdefg").stream()
                        .map(IsoCountry::getDisplayCountry).collect(Collectors.toList()));
    }
}
