package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test cases for fetching localized tag values
 *
 * @author cstaylor
 */
public class LocalizedTaggableTestCase
{
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void failOnNonTagClass()
    {
        this.exception.expect(IllegalArgumentException.class);
        new TestTaggable(NameTag.KEY, "privet").getTag(String.class,
                Optional.of(IsoLanguage.forLanguageCode("ru").get()));
        Assert.fail("Shouldn't have gotten here");
    }

    @Test
    public void failOnNullTagClass()
    {
        this.exception.expect(IllegalArgumentException.class);
        new TestTaggable(NameTag.KEY, "dah").getTag(null,
                Optional.of(IsoLanguage.forLanguageCode("ru").get()));
        Assert.fail("Shouldn't have gotten here");
    }

    @Test
    public void findDefaultNameFromRussianAndDefaults()
    {
        final Optional<String> value = new TestTaggable(
                Maps.hashMap(NameTag.KEY + ":ru", "dah", NameTag.KEY, "nyet")).getTag(NameTag.KEY);
        Assert.assertTrue(value.isPresent());
        Assert.assertEquals("nyet", value.get());
    }

    @Test
    public void findDefaultNameWhenRequestingRussianName()
    {
        final Optional<String> value = new TestTaggable(NameTag.KEY, "karta").getTag(NameTag.class,
                Optional.of(IsoLanguage.forLanguageCode("ru").get()));
        Assert.assertTrue(value.isPresent());
        Assert.assertEquals("karta", value.get());
    }

    @Test
    public void findNoNameWhenRequestingDefaultName()
    {
        final Optional<String> value = new TestTaggable(NameTag.KEY + ":ru", "babushka")
                .getTag(NameTag.KEY);
        Assert.assertFalse(value.isPresent());
    }

    @Test
    public void findRussianName()
    {
        final Optional<String> value = new TestTaggable(NameTag.KEY + ":ru", "kak dela?")
                .getTag(NameTag.class, Optional.of(IsoLanguage.forLanguageCode("ru").get()));
        Assert.assertTrue(value.isPresent());
        Assert.assertEquals("kak dela?", value.get());
    }

    @Test
    public void findRussianNameFromRussianAndDefaults()
    {
        final Optional<String> value = new TestTaggable(
                Maps.hashMap(NameTag.KEY + ":ru", "dah", NameTag.KEY, "nyet"))
                .getTag(NameTag.class, Optional.of(IsoLanguage.forLanguageCode("ru").get()));
        Assert.assertTrue(value.isPresent());
        Assert.assertEquals("dah", value.get());
    }

    @Test
    public void ignoreLocalizationOnNonLocalizableTagClass()
    {
        final Optional<String> value = new TestTaggable(AmenityTag.BANK).getTag(AmenityTag.class,
                Optional.of(IsoLanguage.forLanguageCode("ru").get()));
        Assert.assertTrue(value.isPresent());
        Assert.assertEquals("bank", value.get());
    }
}
