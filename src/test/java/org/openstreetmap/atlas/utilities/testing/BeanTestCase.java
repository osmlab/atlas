package org.openstreetmap.atlas.utilities.testing;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test case demonstrating how to use Bean annotations for testing
 *
 * @author cstaylor
 */
public class BeanTestCase
{
    @Rule
    public BeanTestCaseRule setup = new BeanTestCaseRule();

    @Test
    public void verify()
    {
        Assert.assertEquals("Christopher Taylor", this.setup.bean().getName());
        Assert.assertEquals("123 Main Street", this.setup.bean().getStreet());
    }
}
