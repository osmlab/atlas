package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.locale.IsoLanguage;

/**
 * Test cases for the LocalizedTagNameWithOptionalDate class
 *
 * @author cstaylor
 */
public class LocalizedTagNameWithOptionalDateTestCase
{
    @Test
    public void singleItemName()
    {
        final LocalizedTagNameWithOptionalDate name = new LocalizedTagNameWithOptionalDate("name");
        Assert.assertEquals("name", name.getName());
        Assert.assertFalse(name.getLanguage().isPresent());
        Assert.assertFalse(name.getDateRange().isPresent());
    }

    @Test
    public void threePartItemName()
    {
        final LocalizedTagNameWithOptionalDate name = new LocalizedTagNameWithOptionalDate(
                "name:maybe:not");
        Assert.assertEquals("name:maybe:not", name.getName());
        Assert.assertFalse(name.getLanguage().isPresent());
        Assert.assertFalse(name.getDateRange().isPresent());
    }

    @Test
    public void threePartItemNameInRussian()
    {
        final LocalizedTagNameWithOptionalDate name = new LocalizedTagNameWithOptionalDate(
                "name:maybe:not:ru");
        Assert.assertEquals("name:maybe:not", name.getName());
        Assert.assertTrue(name.getLanguage().isPresent());
        Assert.assertEquals(IsoLanguage.forLanguageCode("ru").get().getLanguageCode(),
                name.getLanguage().get().getLanguageCode());
        Assert.assertFalse(name.getDateRange().isPresent());
    }

    @Test
    public void threePartItemNameInRussianWithDateRange()
    {
        final LocalizedTagNameWithOptionalDate name = new LocalizedTagNameWithOptionalDate(
                "name:maybe:not:ru:1939-1945");
        Assert.assertEquals("name:maybe:not", name.getName());
        Assert.assertTrue(name.getLanguage().isPresent());
        Assert.assertEquals(IsoLanguage.forLanguageCode("ru").get().getLanguageCode(),
                name.getLanguage().get().getLanguageCode());
        Assert.assertTrue(name.getDateRange().isPresent());
    }

    @Test
    public void threePartItemNameWithDateRange()
    {
        final LocalizedTagNameWithOptionalDate name = new LocalizedTagNameWithOptionalDate(
                "name:maybe:not:1939-1945");
        Assert.assertEquals("name:maybe:not", name.getName());
        Assert.assertFalse(name.getLanguage().isPresent());
        Assert.assertTrue(name.getDateRange().isPresent());
    }
}
