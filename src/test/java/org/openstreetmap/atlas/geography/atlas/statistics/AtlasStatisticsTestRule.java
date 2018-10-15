package org.openstreetmap.atlas.geography.atlas.statistics;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test rule for {@link AtlasStatisticsTest}
 *
 * @author matthieun
 */
public class AtlasStatisticsTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "addressAtlas.josm.osm")
    private Atlas addressAtlas;

    @TestAtlas(loadFromJosmOsmResource = "waterAtlas.josm.osm")
    private Atlas waterAtlas;

    @TestAtlas(loadFromJosmOsmResource = "ferryAtlas.josm.osm")
    private Atlas ferryAtlas;

    @TestAtlas(loadFromJosmOsmResource = "refsAtlas.josm.osm")
    private Atlas refsAtlas;

    public Atlas getAddressAtlas()
    {
        return this.addressAtlas;
    }

    public Atlas getFerryAtlas()
    {
        return this.ferryAtlas;
    }

    public Atlas getRefsAtlas()
    {
        return this.refsAtlas;
    }

    public Atlas getWaterAtlas()
    {
        return this.waterAtlas;
    }
}
