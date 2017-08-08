package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.ISOCountryTag;

/**
 * Simple test case for verifying tags using the ValidatorFactory
 *
 * @author cstaylor
 */
public class ISOCountryTagTestCase extends BaseTagTestCase
{
    @Test
    public void testBadCountry()
    {
        Assert.assertTrue(validators().canValidate(ISOCountryTag.KEY));
        Assert.assertFalse(validators().getValidatorFor(ISOCountryTag.KEY).isValid("Timbuktu"));
    }

    @Test
    public void testJapan()
    {
        Assert.assertTrue(validators().canValidate(ISOCountryTag.KEY));
        Assert.assertTrue(validators().getValidatorFor(ISOCountryTag.KEY).isValid("JPN"));
    }
}
