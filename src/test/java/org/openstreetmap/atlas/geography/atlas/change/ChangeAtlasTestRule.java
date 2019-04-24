package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * @author matthieun
 */
public class ChangeAtlasTestRule extends CoreTestRule
{
    private static final String ONE = "15.420563,-61.336198";
    private static final String TWO = "15.429499,-61.332850";

    @TestAtlas(loadFromJosmOsmResource = "ChangeAtlasTest.josm.osm")
    private Atlas atlas;

    @TestAtlas(loadFromJosmOsmResource = "ChangeAtlasTestEdge.josm.osm")
    private Atlas atlasEdge;

    @TestAtlas(

            points = {

                    @Point(id = "1", coordinates = @Loc(value = ONE), tags = { "a=1", "b=2" }),

            }

    )
    private Atlas tagAtlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getAtlasEdge()
    {
        return this.atlasEdge;
    }

    public Atlas getTagAtlas()
    {
        return this.tagAtlas;
    }
}
