package org.openstreetmap.atlas.locale;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author lcram
 */
public class IsoCountryFuzzyMatcherTest
{
    @Test
    public void testFuzzyMatching()
    {
        Assert.assertEquals("USA", IsoCountryFuzzyMatcher
                .forDisplayCountryClosestMatch("UnitedStates").get().getIso3CountryCode());
        Assert.assertEquals("USA", IsoCountryFuzzyMatcher
                .forDisplayCountryClosestMatch("united States").get().getIso3CountryCode());
        Assert.assertEquals("USA", IsoCountryFuzzyMatcher
                .forDisplayCountryClosestMatch("united stats").get().getIso3CountryCode());
        Assert.assertEquals("GBR", IsoCountryFuzzyMatcher
                .forDisplayCountryClosestMatch("unitde kindgdom").get().getIso3CountryCode());

        Assert.assertEquals(Arrays.asList("Sweden", "Cambodia", "Yemen"),
                IsoCountryFuzzyMatcher.forDisplayCountryTopMatches(3, "abcdefg").stream()
                        .map(IsoCountry::getDisplayCountry).collect(Collectors.toList()));
    }
}
