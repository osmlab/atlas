package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.DisusedShopTag;
import org.openstreetmap.atlas.tags.ShopTag;

/**
 * Testing the with annotation feature with the disused shop tag
 *
 * @author cstaylor
 */
public class DisusedShopTagTestCase extends BaseTagTestCase
{

    @Test
    public void testInvalidValue()
    {
        Assert.assertTrue(validators().canValidate(DisusedShopTag.KEY));
        Assert.assertFalse(
                validators().getValidatorFor(DisusedShopTag.KEY).isValid("Crisps and Chips"));
    }

    @Test
    public void testValidValues()
    {
        Assert.assertTrue(validators().canValidate(DisusedShopTag.KEY));
        for (final ShopTag tag : ShopTag.values())
        {
            Assert.assertTrue(validators().getValidatorFor(DisusedShopTag.KEY)
                    .isValid(tag.name().toLowerCase()));
        }
    }
}
