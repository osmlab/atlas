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
        Assert.assertEquals(Arrays.asList("Sweden", "Cambodia", "Yemen"),
                IsoCountryFuzzyMatcher.forDisplayCountryTopMatches(3, "abcdefg").stream()
                        .map(IsoCountry::getDisplayCountry).collect(Collectors.toList()));
    }
}
