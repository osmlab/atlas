package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.OpeningHoursTag;

/**
 * Testing various inputs for the OpeningHoursTag, since it uses regular expressions
 *
 * @author cstaylor
 */
public class OpeningHoursTagTestCase extends BaseTagTestCase
{
    @Test
    public void testDateAndTime()
    {
        Assert.assertTrue(validators().canValidate(OpeningHoursTag.KEY));
        Assert.assertTrue(
                validators().getValidatorFor(OpeningHoursTag.KEY).isValid("Mo-Fr 08:00-22:00"));
    }

    @Test
    public void testIllegalValue()
    {
        Assert.assertTrue(validators().canValidate(OpeningHoursTag.KEY));
        Assert.assertFalse(validators().getValidatorFor(OpeningHoursTag.KEY).isValid("Garbage"));
    }

    @Test
    public void testTimeOnly()
    {
        Assert.assertTrue(validators().canValidate(OpeningHoursTag.KEY));
        Assert.assertTrue(validators().getValidatorFor(OpeningHoursTag.KEY).isValid("08:00-22:00"));
    }

    @Test
    public void testTwentyFourSeven()
    {
        Assert.assertTrue(validators().canValidate(OpeningHoursTag.KEY));
        Assert.assertTrue(validators().getValidatorFor(OpeningHoursTag.KEY).isValid("24/7"));
    }
}
