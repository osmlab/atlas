package org.openstreetmap.atlas.utilities.conversion;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * @author lcram
 */
public class StringToPredicateConverterTestRule extends CoreTestRule
{
    private static final String ONE = "15.420563,-61.336198";
    private static final String TWO = "15.429499,-61.332850";
    private static final String THREE = "15.4855,-61.3041";

    @TestAtlas(points = {

            @Point(id = "1", coordinates = @Loc(value = ONE), tags = { "foo=bar" }),
            @Point(id = "2", coordinates = @Loc(value = TWO), tags = { "baz=bat" }),
            @Point(id = "3", coordinates = @Loc(value = THREE), tags = { "foo=bar", "baz=bat",
                    "mat=fat" })

    })
    private Atlas atlas1;

    public Atlas getAtlas()
    {
        return this.atlas1;
    }
}
