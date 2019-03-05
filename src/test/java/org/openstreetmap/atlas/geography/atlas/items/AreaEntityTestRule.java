package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author mgostintsev
 */
public class AreaEntityTestRule extends CoreTestRule
{
    private static final String ONE = "37,-122.00";
    private static final String TWO = "37,-122.01";

    @TestAtlas(

            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE), tags = { "name=abc" }),
                    @Node(id = "2", coordinates = @Loc(value = TWO)), })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
