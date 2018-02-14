package org.openstreetmap.atlas.geography;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * {@link PolygonPerfTest} data
 *
 * @author mgostintsev
 */
public class PolygonTestRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "BLR-small.atlas.txt.gz")
    private Atlas complexForestPolygon;

    public Atlas getForestPolygon()
    {
        return this.complexForestPolygon;
    }
}
