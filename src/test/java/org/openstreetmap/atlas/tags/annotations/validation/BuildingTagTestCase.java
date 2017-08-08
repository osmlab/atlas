package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.BuildingTag;

/**
 * Simple test case verifying that BuildTag.isBuilding works
 *
 * @author cstaylor
 */
public class BuildingTagTestCase
{
    @Test
    public void testIsBuilding()
    {
        Assert.assertTrue(BuildingTag.isBuilding("ComMERcial"));
    }

    @Test
    public void testIsNotBuilding()
    {
        Assert.assertFalse(BuildingTag.isBuilding("Pyramids"));
    }
}
