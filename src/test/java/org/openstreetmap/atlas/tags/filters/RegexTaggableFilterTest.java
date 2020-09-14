package org.openstreetmap.atlas.tags.filters;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.Taggable;

/**
 * @author mm-ciub on 14/09/2020.
 */
public class RegexTaggableFilterTest
{

    @Test
    public void testException()
    {
        final Set<String> tagNames = new HashSet<>(Arrays.asList("source", "highway"));
        final Set<String> regex = new HashSet<>(
                Arrays.asList(".*(?i)\\bmap\\b.*", ".*(?i)\\bsecondary\\b.*"));
        final HashMap<String, Set<String>> exceptions = new HashMap<>(
                Map.of("source", Set.of("personal map", "public map")));
        final RegexTaggableFilter filter = new RegexTaggableFilter(tagNames, regex, exceptions);

        Assert.assertFalse(filter.test(Taggable.with()));
        Assert.assertFalse(filter.test(Taggable.with("highway", "primary")));
        Assert.assertTrue(
                filter.test(Taggable.with("source", "local knowledge", "highway", "secondary")));
        Assert.assertTrue(
                filter.test(Taggable.with("source", "illegal map", "highway", "secondary")));
        Assert.assertFalse(
                filter.test(Taggable.with("source", "public map", "highway", "primary")));

    }

    @Test
    public void testMultipleOccurrence()
    {
        final Set<String> tagNames = new HashSet<>(Arrays.asList("source", "highway"));
        final Set<String> regex = new HashSet<>(
                Arrays.asList(".*(?i)\\bmap\\b.*", ".*(?i)\\bsecondary\\b.*"));
        final RegexTaggableFilter filter = new RegexTaggableFilter(tagNames, regex, null);

        Assert.assertFalse(filter.test(Taggable.with()));
        Assert.assertFalse(filter.test(Taggable.with("highway", "primary")));
        Assert.assertTrue(
                filter.test(Taggable.with("source", "local knowledge", "highway", "secondary")));
        Assert.assertTrue(
                filter.test(Taggable.with("source", "illegal map", "highway", "secondary")));
    }

    @Test
    public void testReturnedTags()
    {
        final Set<String> tagNames = new HashSet<>(Arrays.asList("source", "highway"));
        final Set<String> regex = new HashSet<>(
                Arrays.asList(".*(?i)\\bmap\\b.*", ".*(?i)\\bsecondary\\b.*"));
        final HashMap<String, Set<String>> exceptions = new HashMap<>(
                Map.of("source", Set.of("personal map", "public map")));
        final RegexTaggableFilter filter = new RegexTaggableFilter(tagNames, regex, exceptions);
        final Taggable taggable = Taggable.with("source", "illegal map", "highway", "secondary");

        Assert.assertTrue(filter.test(taggable));
        Assert.assertEquals("source,highway", filter.getMatchedTags(taggable));
    }

    @Test
    public void testSimpleCase()
    {
        final Set<String> tagNames = new HashSet<>(Collections.singletonList("source"));
        final Set<String> regex = new HashSet<>(Collections.singletonList(".*(?i)\\bmap\\b.*"));
        final RegexTaggableFilter filter = new RegexTaggableFilter(tagNames, regex, null);

        Assert.assertFalse(filter.test(Taggable.with()));
        Assert.assertFalse(filter.test(Taggable.with("highway", "primary")));
        Assert.assertFalse(filter.test(Taggable.with("source", "local knowledge")));
        Assert.assertTrue(filter.test(Taggable.with("source", "illegal map")));
    }

}
