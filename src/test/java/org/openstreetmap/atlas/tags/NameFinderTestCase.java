package org.openstreetmap.atlas.tags;

import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.AlternativeNameTag;
import org.openstreetmap.atlas.tags.names.InternationallyKnownAsTag;
import org.openstreetmap.atlas.tags.names.NameFinder;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test class for the NameFinder
 *
 * @author cstaylor
 */
public class NameFinderTestCase
{
    private Taggable taggable;

    @Before
    public void setUp()
    {
        final Map<String, String> testData = Maps
                .hashMap(NameTag.KEY, "Test", AlternativeNameTag.KEY, "Real Test",
                        Validators.localizeKeyName(NameTag.class,
                                Optional.of(IsoLanguage.forLanguageCode("ru").get())).get(),
                        "nyet");
        this.taggable = new TestTaggable(testData);
    }

    @Test
    public void testAllEnglish()
    {
        final Map<Class<?>, String> all = new NameFinder()
                .withTags(NameTag.class, AlternativeNameTag.class)
                .inLanguage(IsoLanguage.forLanguageCode("en").get()).all(this.taggable);
        Assert.assertEquals(2, all.size());
        Assert.assertEquals("Test", all.get(NameTag.class));
        Assert.assertEquals("Real Test", all.get(AlternativeNameTag.class));
        Assert.assertFalse(all.containsKey(InternationallyKnownAsTag.class));
    }

    @Test
    public void testAllRussian()
    {
        final Map<Class<?>, String> all = new NameFinder()
                .withTags(NameTag.class, AlternativeNameTag.class)
                .inLanguage(IsoLanguage.forLanguageCode("ru").get()).all(this.taggable);
        Assert.assertEquals(2, all.size());
        Assert.assertEquals("nyet", all.get(NameTag.class));
        Assert.assertEquals("Real Test", all.get(AlternativeNameTag.class));
        Assert.assertFalse(all.containsKey(InternationallyKnownAsTag.class));
    }

    @Test
    public void testBestEnglishName()
    {
        final Optional<String> value = new NameFinder().withTags(NameTag.class)
                .inLanguage(IsoLanguage.forLanguageCode("en").get()).best(this.taggable);
        Assert.assertTrue(value.isPresent());
        Assert.assertEquals("Test", value.get());
    }

    @Test
    public void testBestName()
    {
        final Optional<String> value = new NameFinder().withTags(NameTag.class).best(this.taggable);
        Assert.assertTrue(value.isPresent());
        Assert.assertEquals("Test", value.get());
    }

    @Test
    public void testBestRussianName()
    {
        final Optional<String> value = new NameFinder().withTags(NameTag.class)
                .inLanguage(IsoLanguage.forLanguageCode("ru").get()).best(this.taggable);
        Assert.assertTrue(value.isPresent());
        Assert.assertEquals("nyet", value.get());
    }
}
