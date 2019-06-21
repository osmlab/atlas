package org.openstreetmap.atlas.utilities.checkstyle;

import org.junit.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * @author matthieun
 */
public class ArrangementCheckTest extends AbstractModuleTestSupport
{
    @Test
    public void testCheck() throws Exception
    {
        final DefaultConfiguration checkConfig = createModuleConfig(ArrangementCheck.class);
        verify(checkConfig, getPath("InputConstantNameInner.java"), expected);
    }

    @Override
    protected String getPackageLocation()
    {
        return "org/openstreetmap/atlas/utilities/checkstyle";
    }
}
