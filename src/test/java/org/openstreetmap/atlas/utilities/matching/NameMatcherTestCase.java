package org.openstreetmap.atlas.utilities.matching;

import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link NameMatcher}
 *
 * @author brian_l_davis
 */
public class NameMatcherTestCase
{
    private final String[] data = new String[] { "Main Street", "main street", "Rain Street",
            "Second Street", "Second Avenue", null };
    private final String source = "Main Street";

    @Test
    public void testExactMatch()
    {
        final String[] results = Stream.of(data).filter(new NameMatcher(source).matchExactly())
                .toArray(String[]::new);
        Assert.assertArrayEquals(new String[] { "Main Street" }, results);
    }

    @Test
    public void testExactMatchWithNulls()
    {
        final String[] results = Stream.of(data)
                .filter(new NameMatcher(source).matchExactly().matchNulls()).toArray(String[]::new);
        Assert.assertArrayEquals(new String[] { "Main Street", null }, results);
    }

    @Test
    public void testFuzzyMatch()
    {
        final String[] results = Stream.of(data).filter(new NameMatcher(source).matchSimilar())
                .toArray(String[]::new);
        Assert.assertArrayEquals(new String[] { "Main Street", "main street", "Rain Street" },
                results);
    }

    @Test
    public void testFuzzyMatchWithNulls()
    {
        final String[] results = Stream.of(data)
                .filter(new NameMatcher(source).matchSimilar().matchNulls()).toArray(String[]::new);
        Assert.assertArrayEquals(new String[] { "Main Street", "main street", "Rain Street", null },
                results);
    }

    @Test
    public void testReallyFuzzyMatch()
    {
        final String[] results = Stream.of(data).filter(new NameMatcher(source).matchSimilar(10))
                .toArray(String[]::new);
        Assert.assertArrayEquals(
                new String[] { "Main Street", "main street", "Rain Street", "Second Street" },
                results);
    }

    @Test
    public void testUncasedMatch()
    {
        final String[] results = Stream.of(data).filter(new NameMatcher(source))
                .toArray(String[]::new);
        Assert.assertArrayEquals(new String[] { "Main Street", "main street" }, results);
    }

    @Test
    public void testUncasedMatchWithNulls()
    {
        final String[] results = Stream.of(data).filter(new NameMatcher(source).matchNulls())
                .toArray(String[]::new);
        Assert.assertArrayEquals(new String[] { "Main Street", "main street", null }, results);
    }
}
