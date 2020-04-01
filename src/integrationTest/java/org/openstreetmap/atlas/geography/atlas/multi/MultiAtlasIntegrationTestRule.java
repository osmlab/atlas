package org.openstreetmap.atlas.geography.atlas.multi;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class MultiAtlasIntegrationTestRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "DEU_11-1084-708.atlas.txt.gz")
    private Atlas atlas1;
    @TestAtlas(loadFromTextResource = "DEU_11-1084-709.atlas.txt.gz")
    private Atlas atlas2;

    public Atlas getAtlas1()
    {
        return this.atlas1;
    }

    public Atlas getAtlas2()
    {
        return this.atlas2;
    }
}
