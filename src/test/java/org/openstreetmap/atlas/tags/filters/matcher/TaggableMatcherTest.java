package org.openstreetmap.atlas.tags.filters.matcher;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;

/**
 * @author lcram
 */
public class TaggableMatcherTest
{
    @Test
    public void basicTests()
    {
        final Taggable taggable1 = Taggable.with("foo", "bar", "baz", "bat");
        Assert.assertTrue(TaggableMatcher.from("!(foo=bar ^ baz=bat)").test(taggable1));
        Assert.assertFalse(TaggableMatcher.from("foo=bar ^ baz=bat").test(taggable1));
        Assert.assertTrue(TaggableMatcher.from("foo = bar | baz = bat").test(taggable1));
        Assert.assertFalse(TaggableMatcher.from("!(foo=bar | baz=bat)").test(taggable1));
        Assert.assertTrue(TaggableMatcher.from("foo=bar & baz=bat").test(taggable1));
        Assert.assertFalse(TaggableMatcher.from("!(foo=bar & baz=bat)").test(taggable1));
        Assert.assertFalse(TaggableMatcher.from("foo=bar & baz=\" bat\"").test(taggable1));
        Assert.assertTrue(TaggableMatcher.from("foo=bar & baz!=\" bat\"").test(taggable1));
    }

    @Test
    public void basicTests2()
    {
        final Taggable taggable1 = Taggable.with("foo", "bar", "baz", "bat");
        Assert.assertTrue(TaggableMatcher.from("foo=/b.*/ & baz=/b.*/").test(taggable1));
        Assert.assertTrue(TaggableMatcher.from("foo = bar & !mat").test(taggable1));
        Assert.assertFalse(TaggableMatcher.from("foo=bar & mat!=hat").test(taggable1));
        Assert.assertTrue(TaggableMatcher.from("foo=bar & baz!=hat").test(taggable1));
        Assert.assertTrue(TaggableMatcher.from("foo=bar & baz=!hat").test(taggable1));
        Assert.assertEquals(TaggableMatcher.from("baz = (!hat & !cat)").test(taggable1),
                TaggableMatcher.from("baz = !(hat | cat)").test(taggable1));
        Assert.assertTrue(TaggableMatcher.from("  foo    =bar & baz= \"bat\"  ").test(taggable1));
    }

    @Test
    public void complexTests()
    {
        /*
         * Test a complex expression filter.
         */
        final Taggable taggable = Taggable.with("name", "Main Street", "highway", "secondary",
                "restricted", "no");
        Assert.assertTrue(TaggableMatcher.from(
                "name=/^.*(s|S)treet$/ & highway!=primary & (!restricted | restricted != (yes | sometimes))")
                .test(taggable));

        /*
         * Test matching against variable keys.
         */
        final Taggable oldStyleLakes = Taggable.with("natural", "lake");
        final Taggable newStyleLakes = Taggable.with("natural", "water", "water", "lake");
        /*
         * These filters should match both old style and new style lake tagging
         */
        final TaggableMatcher lakeMatcher = TaggableMatcher.from("(natural | water)=lake");
        Assert.assertTrue(lakeMatcher.test(oldStyleLakes));
        Assert.assertTrue(lakeMatcher.test(newStyleLakes));
    }

    @Test
    public void testEmptyMatcher()
    {
        final Taggable taggable1 = Taggable.with("name", "hello");

        final TaggableMatcher emptyMatcher = TaggableMatcher.from("");
        Assert.assertEquals(0L, emptyMatcher.lengthOfLongestLineForPrintedTree());
        Assert.assertEquals("", emptyMatcher.prettyPrintTree());
        Assert.assertTrue(emptyMatcher.test(taggable1));
    }

    @Test
    public void testFilterVsMatcher()
    {
        final Taggable primaryHighway = Taggable.with("highway", "primary", "name", "280");
        final Taggable secondaryHighway = Taggable.with("highway", "secondary", "name", "De Anza");
        final Taggable someLake = Taggable.with("water", "lake", "name", "Loch Ness");

        final TaggableMatcher allFeaturesNotPrimaryHighway = TaggableMatcher
                .from("highway != primary | !highway");
        final TaggableFilter allFeaturesNotPrimaryHighwayFilter = TaggableFilter
                .forDefinition("highway->!primary");

        final TaggableMatcher allHighwaysNotPrimaryHighway = TaggableMatcher
                .from("highway != primary");
        final TaggableFilter allHighwaysNotPrimaryHighwayFilter = TaggableFilter
                .forDefinition("highway->!primary&highway->*");

        Assert.assertFalse(allFeaturesNotPrimaryHighway.test(primaryHighway));
        Assert.assertTrue(allFeaturesNotPrimaryHighway.test(secondaryHighway));
        Assert.assertTrue(allFeaturesNotPrimaryHighway.test(someLake));

        Assert.assertFalse(allFeaturesNotPrimaryHighwayFilter.test(primaryHighway));
        Assert.assertTrue(allFeaturesNotPrimaryHighwayFilter.test(secondaryHighway));
        Assert.assertTrue(allFeaturesNotPrimaryHighwayFilter.test(someLake));

        Assert.assertFalse(allHighwaysNotPrimaryHighway.test(primaryHighway));
        Assert.assertTrue(allHighwaysNotPrimaryHighway.test(secondaryHighway));
        Assert.assertFalse(allHighwaysNotPrimaryHighway.test(someLake));

        Assert.assertFalse(allHighwaysNotPrimaryHighwayFilter.test(primaryHighway));
        Assert.assertTrue(allHighwaysNotPrimaryHighwayFilter.test(secondaryHighway));
        Assert.assertFalse(allHighwaysNotPrimaryHighwayFilter.test(someLake));
    }

    @Test
    public void testPrettyPrintTree()
    {
        Assert.assertEquals("        =       \n" + "    ┌───┴───┐   \n" + "   foo     bar  \n",
                TaggableMatcher.from("foo = bar").prettyPrintTree());
    }

    @Test
    public void testPrettyTreeLength()
    {
        Assert.assertEquals(16L,
                TaggableMatcher.from("foo = bar").lengthOfLongestLineForPrintedTree());
        Assert.assertEquals(32L,
                TaggableMatcher.from("foo = bar | baz = bat").lengthOfLongestLineForPrintedTree());
    }

    @Test
    public void testQuotes()
    {
        final Taggable taggable1 = Taggable.with("name", "John's \"Coffee\" Shop");
        Assert.assertTrue(
                TaggableMatcher.from("name = 'John\\'s \"Coffee\" Shop'").test(taggable1));
        Assert.assertTrue(
                TaggableMatcher.from("name = \"John's \\\"Coffee\\\" Shop\"").test(taggable1));
    }

    @Test
    public void testToString()
    {
        Assert.assertEquals("TaggableMatcher(foo = bar)",
                TaggableMatcher.from("foo = bar").toString());
    }
}
