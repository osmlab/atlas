package org.openstreetmap.atlas.utilities.testing;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Verifies that we can create a TestAtlas from an input file
 *
 * @author cstaylor
 */
public class AtlasFromTextTestCase
{
    @Rule
    public AtlasFromTextTestCaseRule setup = new AtlasFromTextTestCaseRule();

    @Test
    public void verify()
    {
        Assert.assertNotNull(this.setup.atlas());
    }
}
