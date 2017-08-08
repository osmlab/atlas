package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.tags.annotations.validation.BaseTagTestCase;

/**
 * Test case for verifying both the height tag and the length validator
 *
 * @author cstaylor
 */
public class HeightTagTestCase extends BaseTagTestCase
{
    @Rule
    public final ExpectedException expected = ExpectedException.none();

    @Test
    public void testEmptyHeight()
    {
        Assert.assertFalse(validators().isValidFor(HeightTag.KEY, ""));
    }

    @Test
    public void testEnglish()
    {
        Assert.assertTrue(validators().isValidFor(HeightTag.KEY, "12'5\""));
    }

    @Test
    public void testEnglishBlank()
    {
        Assert.assertTrue(validators().isValidFor(HeightTag.KEY, "12'5\"  "));
    }

    @Test
    public void testEnglishPrecedingBlank()
    {
        Assert.assertTrue(validators().isValidFor(HeightTag.KEY, " 12'5\""));
    }

    @Test
    public void testEnglishWrong()
    {
        Assert.assertFalse(validators().isValidFor(HeightTag.KEY, "12'5\"a"));
    }

    @Test
    public void testFeetOnly()
    {
        Assert.assertTrue(validators().isValidFor(HeightTag.KEY, "12'"));
    }

    @Test
    public void testFeetOnlyBlank()
    {
        Assert.assertTrue(validators().isValidFor(HeightTag.KEY, "12' "));
    }

    @Test
    public void testFeetWrong()
    {
        Assert.assertFalse(validators().isValidFor(HeightTag.KEY, "12'a"));
    }

    @Test
    public void testInchesOnly()
    {
        Assert.assertTrue(validators().isValidFor(HeightTag.KEY, "5\""));
    }

    @Test
    public void testMeters()
    {
        Assert.assertTrue(validators().isValidFor(HeightTag.KEY, "12.5 m"));
    }

    @Test
    public void testNonsenseValue()
    {
        Assert.assertFalse(validators().isValidFor(HeightTag.KEY, "PPAP"));
    }

    @Test
    public void testNonsenseValue2()
    {
        Assert.assertFalse(
                validators().isValidFor(HeightTag.KEY, "Estacion de Servicio \"Los Arrayanes\""));
    }

    @Test
    public void testNullHeight()
    {
        this.expected.expect(NullPointerException.class);
        Assert.assertFalse(validators().isValidFor(HeightTag.KEY, null));
    }

    @Test
    public void testNumericOnly()
    {
        Assert.assertTrue(validators().isValidFor(HeightTag.KEY, "12.5"));
    }

    @Test
    public void testNumericOnlyWithBlanks()
    {
        Assert.assertTrue(validators().isValidFor(HeightTag.KEY, "  12.5  "));
    }

}
