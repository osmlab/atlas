package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.AddressFlatsTag;

/**
 * Test case verifying that Address Flats Tag can be properly validated.
 *
 * @author mgostintsev
 */
public class AddressFlatsTagTestCase extends BaseTagTestCase
{
    @Test
    public void testInvalidValue()
    {
        Assert.assertTrue(validators().canValidate(AddressFlatsTag.KEY));
        Assert.assertFalse(validators().getValidatorFor(AddressFlatsTag.KEY).isValid(" "));
    }

    @Test
    public void testValidValues()
    {
        Assert.assertTrue(validators().canValidate(AddressFlatsTag.KEY));
        Assert.assertTrue(validators().getValidatorFor(AddressFlatsTag.KEY).isValid("7"));
        Assert.assertTrue(validators().getValidatorFor(AddressFlatsTag.KEY).isValid("1-20"));
        Assert.assertTrue(
                validators().getValidatorFor(AddressFlatsTag.KEY).isValid("3-7;10;14;16-18"));
    }
}
