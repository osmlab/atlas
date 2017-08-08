package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.LastEditUserIdentifierTag;

/**
 * Test case for LastEditUserIdentifier tag
 *
 * @author cstaylor
 */
public class LastEditUserIdentifierTestCase extends BaseTagTestCase
{
    @Test
    public void bad()
    {
        Assert.assertFalse(validators().isValidFor(LastEditUserIdentifierTag.KEY, "Nope"));
    }

    @Test
    public void good()
    {
        Assert.assertTrue(validators().isValidFor(LastEditUserIdentifierTag.KEY, "1846410"));
    }
}
