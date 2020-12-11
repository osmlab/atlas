package org.openstreetmap.atlas.tags.filters;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;

/**
 * @author lcram
 */
public class TaggableFilterToMatcherConverterTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void basicTest()
    {
        final TaggableFilterToMatcherConverter converter = new TaggableFilterToMatcherConverter();
        final TaggableFilter filter = TaggableFilter.forDefinition(
                "highway->service&cycleway->lane||cycleway:lane->*&&cycleway->!|highway->!service");
        final TaggableMatcher equivalentMatcher = converter.convert(filter);

        Assert.assertEquals(
                "(highway=service & (cycleway=lane | (cycleway:lane & !cycleway))) | (highway!=service | !highway)",
                equivalentMatcher.getDefinition());

        final Taggable taggable1 = Taggable.with("highway", "primary");
        final Taggable taggable2 = Taggable.with("highway", "service", "cycleway", "lane");
        final Taggable taggable3 = Taggable.with("highway", "service", "cycleway", "lane",
                "cycleway:lane", "left");
        final Taggable taggable4 = Taggable.with("highway", "service", "cycleway:lane", "left");
        final Taggable taggable5 = Taggable.with("cycleway", "lane");
        final Taggable taggable6 = Taggable.with("natural", "lake");

        Assert.assertEquals(filter.test(taggable1), equivalentMatcher.test(taggable1));
        Assert.assertEquals(filter.test(taggable2), equivalentMatcher.test(taggable2));
        Assert.assertEquals(filter.test(taggable3), equivalentMatcher.test(taggable3));
        Assert.assertEquals(filter.test(taggable4), equivalentMatcher.test(taggable4));
        Assert.assertEquals(filter.test(taggable5), equivalentMatcher.test(taggable5));
        Assert.assertEquals(filter.test(taggable6), equivalentMatcher.test(taggable6));
    }

    @Test
    public void parenthesesTest()
    {
        final TaggableFilterToMatcherConverter converter = new TaggableFilterToMatcherConverter();
        final TaggableFilter filter = TaggableFilter.forDefinition("foo->bar&baz->bat");
        final TaggableMatcher equivalentMatcher = converter.convert(filter);
        Assert.assertEquals("(foo=bar & baz=bat)", equivalentMatcher.getDefinition());
    }

    @Test
    public void regexTest()
    {
        final TaggableFilterToMatcherConverter converter = new TaggableFilterToMatcherConverter();
        final TaggableFilter filter = TaggableFilter.forDefinition("foo->bar*|baz->*bat");
        final TaggableMatcher equivalentMatcher = converter.convert(filter);

        Assert.assertEquals("foo=/bar.*/ | baz=/.*bat/", equivalentMatcher.getDefinition());

        final Taggable taggable1 = Taggable.with("foo", "bar2");
        final Taggable taggable2 = Taggable.with("foo", "2bar", "baz", "222bat");
        final Taggable taggable3 = Taggable.with("foo", "2bar", "baz", "bat2");

        Assert.assertEquals(filter.test(taggable1), equivalentMatcher.test(taggable1));
        Assert.assertEquals(filter.test(taggable2), equivalentMatcher.test(taggable2));
        Assert.assertEquals(filter.test(taggable3), equivalentMatcher.test(taggable3));
    }

    @Test
    public void testBangFailure()
    {
        final TaggableFilterToMatcherConverter converter = new TaggableFilterToMatcherConverter();
        final TaggableFilter filter = TaggableFilter.forDefinition("highway->primary,!,secondary");

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "Cannot transpile `highway->primary,!,secondary' since value `primary,!,secondary' contains a lone `!' operator.");
        converter.convert(filter);
    }

    @Test
    public void testMultipleValues()
    {
        final TaggableFilterToMatcherConverter converter = new TaggableFilterToMatcherConverter();
        final TaggableFilter filter = TaggableFilter
                .forDefinition("highway->primary,!service,secondary");
        final TaggableMatcher equivalentMatcher = converter.convert(filter);

        Assert.assertEquals("highway=(primary | !service | secondary)",
                equivalentMatcher.getDefinition());

        final Taggable taggable1 = Taggable.with("highway", "primary");
        final Taggable taggable2 = Taggable.with("highway", "service");
        final Taggable taggable3 = Taggable.with("natural", "lake");

        Assert.assertEquals(filter.test(taggable1), equivalentMatcher.test(taggable1));
        Assert.assertEquals(filter.test(taggable2), equivalentMatcher.test(taggable2));
        Assert.assertEquals(filter.test(taggable3), equivalentMatcher.test(taggable3));
    }

    @Test
    public void testRegexFailure1()
    {
        final TaggableFilterToMatcherConverter converter = new TaggableFilterToMatcherConverter();
        final TaggableFilter filter = TaggableFilter.forDefinition("highway->*(primary)");

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "Cannot transpile `highway->*(primary)' since new value `(primary)' contains a regex control character.");
        converter.convert(filter);
    }

    @Test
    public void testRegexFailure2()
    {
        final TaggableFilterToMatcherConverter converter = new TaggableFilterToMatcherConverter();
        final TaggableFilter filter = TaggableFilter.forDefinition("highway->(primary)*");

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "Cannot transpile `highway->(primary)*' since new value `(primary)' contains a regex control character.");
        converter.convert(filter);
    }

    @Test
    public void testStarFailure()
    {
        final TaggableFilterToMatcherConverter converter = new TaggableFilterToMatcherConverter();
        final TaggableFilter filter = TaggableFilter.forDefinition("highway->primary,*,secondary");

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "Cannot transpile `highway->primary,*,secondary' since value `primary,*,secondary' contains a lone `*' operator.");
        converter.convert(filter);
    }
}
