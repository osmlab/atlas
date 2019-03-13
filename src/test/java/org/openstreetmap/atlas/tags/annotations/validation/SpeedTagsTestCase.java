package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.MaxSpeedBackwardTag;
import org.openstreetmap.atlas.tags.MaxSpeedForwardTag;
import org.openstreetmap.atlas.tags.MaxSpeedTag;
import org.openstreetmap.atlas.tags.MinSpeedTag;

/**
 * Test case verifying that speed tags can be properly validated.
 *
 * @author mgostintsev
 */
public class SpeedTagsTestCase extends BaseTagTestCase
{
    @Test
    public void testInvalidValue()
    {
        Assert.assertFalse(validators().getValidatorFor(MaxSpeedTag.KEY).isValid(" "));
        Assert.assertFalse(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("mph"));
        Assert.assertFalse(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("-60 kph"));
        Assert.assertFalse(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("-60"));
        Assert.assertFalse(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("60 km/h"));
        Assert.assertFalse(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("sixty"));
        Assert.assertFalse(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("sixty kph"));
        Assert.assertFalse(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("sixty mph"));
        Assert.assertFalse(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("sixty knots"));
    }

    @Test
    public void testValidValues()
    {
        Assert.assertTrue(validators().canValidate(MaxSpeedTag.KEY));
        Assert.assertTrue(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("60"));
        Assert.assertTrue(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("60 kph"));
        Assert.assertTrue(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("60kph"));
        Assert.assertTrue(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("60 knots"));
        Assert.assertTrue(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("60mph"));
        Assert.assertTrue(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("60 mph"));
        Assert.assertTrue(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("none"));
        Assert.assertTrue(validators().getValidatorFor(MaxSpeedTag.KEY).isValid("RO:urban"));
        Assert.assertTrue(validators().canValidate(MinSpeedTag.KEY));
        Assert.assertTrue(validators().getValidatorFor(MinSpeedTag.KEY).isValid("60"));
        Assert.assertTrue(validators().getValidatorFor(MinSpeedTag.KEY).isValid("60 kph"));
        Assert.assertTrue(validators().getValidatorFor(MinSpeedTag.KEY).isValid("60kph"));
        Assert.assertTrue(validators().getValidatorFor(MinSpeedTag.KEY).isValid("60 knots"));
        Assert.assertTrue(validators().getValidatorFor(MinSpeedTag.KEY).isValid("60mph"));
        Assert.assertTrue(validators().getValidatorFor(MinSpeedTag.KEY).isValid("60 mph"));
        Assert.assertTrue(validators().getValidatorFor(MinSpeedTag.KEY).isValid("none"));
        Assert.assertTrue(validators().getValidatorFor(MinSpeedTag.KEY).isValid("RO:urban"));
        Assert.assertTrue(validators().canValidate(MaxSpeedBackwardTag.KEY));
        Assert.assertTrue(validators().canValidate(MaxSpeedForwardTag.KEY));
    }

}
