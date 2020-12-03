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
            final Map<String, String> tags = Maps.hashMap("foo", "bar", "baz", "bat");

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
    public void complexTest()
    {
        final Taggable taggable = new Taggable()
        {
            final Map<String, String> tags = Maps.hashMap("name", "Main Street", "highway",
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
    }

    @Test
    public void testQuotes()
    {
        final Taggable taggable1 = new Taggable()
        {
            final Map<String, String> tags = Maps.hashMap("foo", "'bar'", "baz", "\"bat\"");

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
        Assert.assertTrue(TaggableMatcher.from("foo=\"'bar'\" & baz='\"bat\"'").test(taggable1));
    }

    @Test
    public void testToString()
    {
        Assert.assertEquals("TaggableMatcher(foo = bar)",
                TaggableMatcher.from("foo = bar").toString());
    }
}
