package org.openstreetmap.atlas.tags;

import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test case for listing explicitly defined languages in a localized tag
 *
 * @author cstaylor
 */
public class GetTagsTestCase
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void hasAdditionalTagsToo()
    {
        final TestTaggable taggable = new TestTaggable(
                Maps.hashMap("name", "Some Name", "car", "123"));
        final Optional<Set<IsoLanguage>> possibleLanguages = taggable.languagesFor(NameTag.class);
        Assert.assertTrue(possibleLanguages.isPresent());
        Assert.assertTrue(possibleLanguages.get().isEmpty());
    }

    @Test
    public void hasNoLanguagesForName()
    {
        final TestTaggable taggable = new TestTaggable(Maps.hashMap("name", "Some Name"));
        final Optional<Set<IsoLanguage>> possibleLanguages = taggable.languagesFor(NameTag.class);
        Assert.assertTrue(possibleLanguages.isPresent());
        Assert.assertTrue(possibleLanguages.get().isEmpty());
    }

    @Test
    public void hasNoRussianForName()
    {
        final TestTaggable taggable = new TestTaggable(
                Maps.hashMap("name", "Some Name", "name:en", "Nyet!"));
        final Optional<Set<IsoLanguage>> possibleLanguages = taggable.languagesFor(NameTag.class);
        Assert.assertTrue(possibleLanguages.isPresent());
        Assert.assertEquals(1, possibleLanguages.get().size());
        Assert.assertFalse(
                possibleLanguages.get().contains(IsoLanguage.forLanguageCode("ru").get()));
    }

    @Test
    public void hasRussianForName()
    {
        final TestTaggable taggable = new TestTaggable(
                Maps.hashMap("name", "Some Name", "name:ru", "Nyet!"));
        final Optional<Set<IsoLanguage>> possibleLanguages = taggable.languagesFor(NameTag.class);
        Assert.assertTrue(possibleLanguages.isPresent());
        Assert.assertEquals(1, possibleLanguages.get().size());
        Assert.assertTrue(
                possibleLanguages.get().contains(IsoLanguage.forLanguageCode("ru").get()));
    }

    @Test
    public void hasWeirdLangauge()
    {
        final TestTaggable taggable = new TestTaggable(
                Maps.hashMap("name", "Some Name", "name:klingon", "Nyet!"));
        final Optional<Set<IsoLanguage>> possibleLanguages = taggable.languagesFor(NameTag.class);
        Assert.assertTrue(possibleLanguages.isPresent());
        Assert.assertTrue(possibleLanguages.get().isEmpty());
    }

    @Test
    public void nonLocalizableTag()
    {
        this.thrown.expect(CoreException.class);
        final TestTaggable taggable = new TestTaggable(
                Maps.hashMap("name", "Some Name", "name:ru", "Nyet!"));
        @SuppressWarnings("unused")
        final Optional<Set<IsoLanguage>> possibleLanguages = taggable
                .languagesFor(MilitaryTag.class);
    }
}
