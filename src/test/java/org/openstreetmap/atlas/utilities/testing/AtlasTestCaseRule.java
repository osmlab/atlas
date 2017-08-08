package org.openstreetmap.atlas.utilities.testing;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * Example test case rule showing how annotation processing can simplify test code
 *
 * @author cstaylor
 */
public class AtlasTestCaseRule extends CoreTestRule
{
    @TestAtlas(areas = { @Area(id = "1234", tags = { "name=17", "building=apartments",
            "addr:street=Expreso V Centenario" }, coordinates = { @Loc("18.4762695,-69.9118829"),
                    @Loc(lon = -69.3320129, lat = 19.2025913) }) })
    private Atlas atlas;

    @TestAtlas(areas = { @Area(tags = { "name=17", "building=apartments",
            "addr:street=Expreso V Centenario" }, coordinates = {
                    @Loc(lon = -69.9118829, lat = 18.4762695),
                    @Loc(lon = -69.3320129, lat = 19.2025913) }) })
    private Atlas atlas2;

    /**
     * A quick way for testing non-geometry related information about an area
     */
    @TestAtlas(areas = { @Area(tags = { "name=42" }) })
    private Atlas atlas3;

    public Atlas atlas()
    {
        return this.atlas;
    }

    public Atlas atlas2()
    {
        return this.atlas2;
    }

    public Atlas atlas3()
    {
        return this.atlas3;
    }
}
