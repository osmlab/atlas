package org.openstreetmap.atlas.geography.atlas.items.complex;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Unit test rules for {@link RelationOrAreaToMultiPolygonConverter}.
 *
 * @author bbreithaupt
 */
public class RelationOrAreaToMultiPolygonConverterTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "InnerOuterMultiPolygon.osm")
    private Atlas innerOuterMultiPolygonAtlas;

    public Atlas innerOuterMultiPolygonAtlas()
    {
        return this.innerOuterMultiPolygonAtlas;
    }
}
