package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * {@link AtlasItemIntersectionTest} test data.
 *
 * @author mgostintsev
 */
public class AtlasItemIntersectionTestRule extends CoreTestRule
{
    private static final String LOCATION_1 = "47.625534, -122.210083";
    private static final String LOCATION_2 = "47.625576, -122.208305";
    private static final String LOCATION_3 = "47.626934, -122.208412";

    @TestAtlas(loadFromTextResource = "intersectionAtlas.atlas.txt")
    private Atlas intersectionAtlas;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = LOCATION_1), @Loc(value = LOCATION_2),
            @Loc(value = LOCATION_3), @Loc(value = LOCATION_1) }) })
    private Atlas noIntersectionAtlas;

    public Atlas getIntersectionAtlas()
    {
        return this.intersectionAtlas;
    }

    public Atlas getNoIntersectionAtlas()
    {
        return this.noIntersectionAtlas;
    }
}
