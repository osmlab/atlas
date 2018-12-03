package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link LengthValidator}.
 *
 * @author bbreithaupt
 */
public class LengthValidatorTest
{
    private static final LengthValidator VALIDATOR = new LengthValidator();

    @Test
    public void validMetersTest()
    {
        Assert.assertTrue(VALIDATOR.isValid("123 m"));
    }

    @Test
    public void validKilometersTest()
    {
        Assert.assertTrue(VALIDATOR.isValid("123.5 km"));
    }

    @Test
    public void validMilesTest()
    {
        Assert.assertTrue(VALIDATOR.isValid("123.54 mi"));
    }

    @Test
    public void validNauticalMilesTest()
    {
        Assert.assertTrue(VALIDATOR.isValid("123.543 nmi"));
    }

    @Test
    public void invalidFeetInchsTest()
    {
        Assert.assertTrue(VALIDATOR.isValid("12'5\""));
    }

    @Test
    public void invalidFeetTest()
    {
        Assert.assertTrue(VALIDATOR.isValid("12'"));
    }

    @Test
    public void invalidInchsTest()
    {
        Assert.assertTrue(VALIDATOR.isValid("5\""));
    }

    @Test
    public void invalidMetersTest()
    {
        Assert.assertFalse(VALIDATOR.isValid("123m"));
    }

    @Test
    public void invalidNumberTest()
    {
        Assert.assertFalse(VALIDATOR.isValid("123s m"));
    }
}
