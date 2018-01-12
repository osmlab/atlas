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
import org.openstreetmap.atlas.utilities.testing.FreezeDryFunction;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test cases for the standard set of names
 *
 * @author cstaylor
 */
public class StandardNameFinderTestCase
{
    private static final FreezeDryFunction<NameFinder> FREEZE_DRY = new FreezeDryFunction<>();

    private Taggable taggable;

    @Test
    public void serializedStandardAll() throws Exception
    {
        final Map<Class<?>, String> all = FREEZE_DRY
                .apply(NameFinder.createStandardSet(IsoLanguage.forLanguageCode("en").get()))
                .all(this.taggable);
        Assert.assertEquals(2, all.size());
        Assert.assertEquals("Test", all.get(NameTag.class));
        Assert.assertEquals("Real Test", all.get(AlternativeNameTag.class));
        Assert.assertFalse(all.containsKey(InternationallyKnownAsTag.class));
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
    public void standardAll()
    {
        final Map<Class<?>, String> all = NameFinder
                .createStandardSet(IsoLanguage.forLanguageCode("en").get()).all(this.taggable);
        Assert.assertEquals(2, all.size());
        Assert.assertEquals("Test", all.get(NameTag.class));
        Assert.assertEquals("Real Test", all.get(AlternativeNameTag.class));
        Assert.assertFalse(all.containsKey(InternationallyKnownAsTag.class));
    }
}
