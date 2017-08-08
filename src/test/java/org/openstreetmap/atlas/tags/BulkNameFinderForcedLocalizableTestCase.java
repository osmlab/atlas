package org.openstreetmap.atlas.tags;

import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.Taggable.TagSearchOption;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.BulkNameFinder;
import org.openstreetmap.atlas.tags.names.BulkNameFinder.BulkFindResults;
import org.openstreetmap.atlas.tags.names.InternationallyKnownAsTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test case verifying that we can optionally force a tag value search to localize a normally
 * non-localizable tag
 *
 * @author cstaylor
 */
public class BulkNameFinderForcedLocalizableTestCase
{
    private static final Optional<IsoLanguage> RUSSIAN = IsoLanguage.forLanguageCode("ru");

    private Taggable localizedDataForNonLocalizableKey;

    @Test
    public void forceLocalized()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet()
                .withLanguage(RUSSIAN.get()).forceLocalized()
                .findIn(this.localizedDataForNonLocalizableKey);
        Assert.assertTrue(results.valueFor(RUSSIAN, InternationallyKnownAsTag.class).isPresent());
        Assert.assertEquals("nyet",
                results.valueFor(RUSSIAN, InternationallyKnownAsTag.class).get());
    }

    @Test
    public void forceLocalizedWithoutExplicitLanguageList()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet().findInWithMyLanguages(
                this.localizedDataForNonLocalizableKey, TagSearchOption.FORCE_ALL_LOCALIZED_ONLY);
        Assert.assertTrue(results.valueFor(RUSSIAN, InternationallyKnownAsTag.class).isPresent());
        Assert.assertEquals("nyet",
                results.valueFor(RUSSIAN, InternationallyKnownAsTag.class).get());
    }

    @Test
    public void nonLocalized()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet()
                .withLanguage(RUSSIAN.get()).findIn(this.localizedDataForNonLocalizableKey);
        Assert.assertFalse(results.valueFor(RUSSIAN, InternationallyKnownAsTag.class).isPresent());
    }

    @Test
    public void nonLocalizedWithoutExplicitLanguageList()
    {
        final BulkFindResults results = BulkNameFinder.createStandardSet()
                .findInWithMyLanguages(this.localizedDataForNonLocalizableKey);
        Assert.assertFalse(results.valueFor(RUSSIAN, InternationallyKnownAsTag.class).isPresent());
    }

    @Before
    public void setUp()
    {
        final Map<String, String> data = Maps
                .hashMap(Validators.localizeKeyName(InternationallyKnownAsTag.class, RUSSIAN,
                        TagSearchOption.FORCE_ALL_LOCALIZED_ONLY).get(), "nyet");
        this.localizedDataForNonLocalizableKey = new TestTaggable(data);
    }
}
