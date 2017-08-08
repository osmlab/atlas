package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author mgostintsev
 */
public class AreaTestRule extends CoreTestRule
{
    private static final String ONE = "37.33,-122.00";
    private static final String TWO = "37.33,-122.03";
    private static final String THREE = "37.32,-122.03";
    private static final String FOUR = "37.32,-122.00";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR))

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE), @Loc(value = FOUR) }, tags = { "building=yes" }),
                    @Area(id = "2", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), }, tags = { "building=yes" }),

            })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
