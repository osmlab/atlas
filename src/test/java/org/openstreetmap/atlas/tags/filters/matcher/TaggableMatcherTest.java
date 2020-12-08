package org.openstreetmap.atlas.tags.filters.matcher;

import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author lcram
 */
public class TaggableMatcherTest
{
    @Test
    public void basicTests()
    {
        final Taggable taggable1 = new Taggable()
        {
            private final Map<String, String> tags = Maps.hashMap("foo", "bar", "baz", "bat");

            @Override
            public Optional<String> getTag(final String key)
            {
                return Optional.ofNullable(this.tags.get(key));
            }

            @Override
            public Map<String, String> getTags()
            {
                return this.tags;
            }
        };
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
        final Taggable taggable1 = new Taggable()
        {
            private final Map<String, String> tags = Maps.hashMap("foo", "bar", "baz", "bat");

            @Override
            public Optional<String> getTag(final String key)
            {
                return Optional.ofNullable(this.tags.get(key));
            }

            @Override
            public Map<String, String> getTags()
            {
                return this.tags;
            }
        };
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
        final Taggable taggable = new Taggable()
        {
            private final Map<String, String> tags = Maps.hashMap("name", "Main Street", "highway",
                    "secondary", "restricted", "no");

            @Override
            public Optional<String> getTag(final String key)
            {
                return Optional.ofNullable(this.tags.get(key));
            }

            @Override
            public Map<String, String> getTags()
            {
                return this.tags;
            }
        };
        Assert.assertTrue(TaggableMatcher.from(
                "name=/^.*(s|S)treet$/ & highway!=primary & (!restricted | restricted != (yes | sometimes))")
                .test(taggable));

        /*
         * Test matching against variable keys.
         */
        final Taggable oldStyleLakes = new Taggable()
        {
            private final Map<String, String> tags = Maps.hashMap("natural", "lake");

            @Override
            public Optional<String> getTag(final String key)
            {
                return Optional.ofNullable(this.tags.get(key));
            }

            @Override
            public Map<String, String> getTags()
            {
                return this.tags;
            }
        };
        final Taggable newStyleLakes = new Taggable()
        {
            private final Map<String, String> tags = Maps.hashMap("natural", "water", "water",
                    "lake");

            @Override
            public Optional<String> getTag(final String key)
            {
                return Optional.ofNullable(this.tags.get(key));
            }

            @Override
            public Map<String, String> getTags()
            {
                return this.tags;
            }
        };
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
        final Taggable taggable1 = new Taggable()
        {
            private final Map<String, String> tags = Maps.hashMap("name", "hello");

            @Override
            public Optional<String> getTag(final String key)
            {
                return Optional.ofNullable(this.tags.get(key));
            }

            @Override
            public Map<String, String> getTags()
            {
                return this.tags;
            }
        };

        final TaggableMatcher emptyMatcher = TaggableMatcher.from("");
        Assert.assertEquals(0L, emptyMatcher.lengthOfLongestLineForPrintedTree());
        Assert.assertEquals("", emptyMatcher.prettyPrintTree());
        Assert.assertTrue(emptyMatcher.test(taggable1));
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
        final Taggable taggable1 = new Taggable()
        {
            private final Map<String, String> tags = Maps.hashMap("name", "John's \"Coffee\" Shop");

            @Override
            public Optional<String> getTag(final String key)
            {
                return Optional.ofNullable(this.tags.get(key));
            }

            @Override
            public Map<String, String> getTags()
            {
                return this.tags;
            }
        };
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
