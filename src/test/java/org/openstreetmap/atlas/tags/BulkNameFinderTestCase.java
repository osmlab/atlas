package org.openstreetmap.atlas.tags;

import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.AlternativeNameTag;
import org.openstreetmap.atlas.tags.names.BulkNameFinder;
import org.openstreetmap.atlas.tags.names.BulkNameFinder.BulkFindResults;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test case for the BulkNameFinder
 *
 * @author cstaylor
 */
public class BulkNameFinderTestCase
{
    private Taggable taggable;

    @Test
    public void allLanguagesTest()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet().allLanguages()
                .findIn(this.taggable);
        Assert.assertTrue(results.valueFor(Optional.empty(), AlternativeNameTag.class).isPresent());
        Assert.assertEquals("Real Test",
                results.valueFor(Optional.empty(), AlternativeNameTag.class).get());
        Assert.assertTrue(results
                .valueFor(Optional.of(IsoLanguage.forLanguageCode("ru").get()), NameTag.class)
                .isPresent());
        Assert.assertEquals("nyet", results
                .valueFor(Optional.of(IsoLanguage.forLanguageCode("ru").get()), NameTag.class)
                .get());
    }

    @Test
    public void allValuesForCountryWithDataTest()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet().allLanguages()
                .findIn(this.taggable);
        final Optional<Map<Class<?>, String>> russianValues = results
                .allValuesFor(Optional.of(IsoLanguage.forLanguageCode("ru").get()));
        Assert.assertTrue(russianValues.isPresent());

        final Optional<Map<Class<?>, String>> japaneseValues = results
                .allValuesFor(Optional.of(IsoLanguage.forLanguageCode("ja").get()));
        Assert.assertTrue(japaneseValues.isPresent());
        Assert.assertEquals(0, japaneseValues.get().size());

        russianValues.ifPresent(map ->
        {
            Assert.assertNotNull(map.get(NameTag.class));
            Assert.assertEquals("nyet", map.get(NameTag.class));
        });
    }

    @Test
    public void allValuesForDefaultWithDataTest()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet().allLanguages()
                .findIn(this.taggable);
        final Optional<Map<Class<?>, String>> defaultValues = results
                .allValuesFor(Optional.empty());
        Assert.assertTrue(defaultValues.isPresent());

        defaultValues.ifPresent(map ->
        {
            Assert.assertNotNull(map.get(AlternativeNameTag.class));
            Assert.assertEquals("Real Test", map.get(AlternativeNameTag.class));
        });
    }

    @Test
    public void allValuesNotPresentForCountryNotRequested()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet()
                .withLanguage(IsoLanguage.forLanguageCode("ru").get()).findIn(this.taggable);
        final Optional<Map<Class<?>, String>> japaneseValues = results
                .allValuesFor(Optional.of(IsoLanguage.forLanguageCode("ja").get()));
        Assert.assertFalse(japaneseValues.isPresent());
    }

    @Test
    public void allValuesPresentForCountryRequestedWithNoData()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet().allLanguages()
                .findIn(this.taggable);
        final Optional<Map<Class<?>, String>> japaneseValues = results
                .allValuesFor(Optional.of(IsoLanguage.forLanguageCode("ja").get()));
        Assert.assertTrue(japaneseValues.isPresent());
        Assert.assertEquals(0, japaneseValues.get().size());
    }

    @Test
    public void flattenTest()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet()
                .withLanguage(IsoLanguage.forLanguageCode("ru").get()).findIn(this.taggable);
        final Map<String, String> flattenedResults = results.flatten();
        Assert.assertEquals(3, flattenedResults.size());
    }

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
    public void standardTestInRussian()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet()
                .withLanguage(IsoLanguage.forLanguageCode("ru").get()).findIn(this.taggable);
        Assert.assertTrue(results.valueFor(Optional.empty(), AlternativeNameTag.class).isPresent());
        Assert.assertEquals("Real Test",
                results.valueFor(Optional.empty(), AlternativeNameTag.class).get());
        Assert.assertTrue(results
                .valueFor(Optional.of(IsoLanguage.forLanguageCode("ru").get()), NameTag.class)
                .isPresent());
        Assert.assertEquals("nyet", results
                .valueFor(Optional.of(IsoLanguage.forLanguageCode("ru").get()), NameTag.class)
                .get());
    }

    @Test
    public void standardTestNoLanguages()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet().findIn(this.taggable);
        Assert.assertTrue(results.valueFor(Optional.empty(), AlternativeNameTag.class).isPresent());
        Assert.assertEquals("Real Test",
                results.valueFor(Optional.empty(), AlternativeNameTag.class).get());
    }

    @Test
    public void testLanguagesInValues()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet()
                .findInWithMyLanguages(this.taggable);
        Assert.assertTrue(results.valueFor(Optional.empty(), AlternativeNameTag.class).isPresent());
        Assert.assertEquals("Real Test",
                results.valueFor(Optional.empty(), AlternativeNameTag.class).get());
        Assert.assertTrue(results
                .valueFor(Optional.of(IsoLanguage.forLanguageCode("ru").get()), NameTag.class)
                .isPresent());
        Assert.assertEquals("nyet", results
                .valueFor(Optional.of(IsoLanguage.forLanguageCode("ru").get()), NameTag.class)
                .get());
    }

    @Test
    public void testLanguagesInValuesNoLanguages()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet()
                .findInWithMyLanguages(this.taggable);
        Assert.assertTrue(results.valueFor(Optional.empty(), AlternativeNameTag.class).isPresent());
        Assert.assertEquals("Real Test",
                results.valueFor(Optional.empty(), AlternativeNameTag.class).get());
    }
}
