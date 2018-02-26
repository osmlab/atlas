package org.openstreetmap.atlas.utilities.filters;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test Rule for {@link AtlasEntityPolygonsFilterTest}
 *
 * @author jklamer
 */
public class AtlasEntityPolygonsFilterTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "includeExcludePolygonArrangements.osm")
    private Atlas includeExcludeArrangements;
    @TestAtlas(loadFromJosmOsmResource = "multiPolygons.osm")
    private Atlas multiPolygons;
    @TestAtlas(loadFromJosmOsmResource = "testCounts.osm")
    private Atlas testCounts;
    @TestAtlas(loadFromJosmOsmResource = "testForms.osm")
    private Atlas testForm;

    public Atlas getIncludeExcludeArrangements()
    {
        return this.includeExcludeArrangements;
    }

    public Atlas getMultiPolygons()
    {
        return this.multiPolygons;
    }

    public Atlas getTestCounts()
    {
        return this.testCounts;
    }

    public Atlas getTestForm()
    {
        return this.testForm;
    }
}
