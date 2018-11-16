package org.openstreetmap.atlas.geography.atlas.bloated;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * @author matthieun
 */
public class BloatedTestRule extends CoreTestRule
{
    public static final String POINT_1_LOCATION = "37.331417,-122.0304871";
    public static final String POINT_2_LOCATION = "37.333364,-122.0200268";

    @TestAtlas(

            nodes = { @Node(id = "1", coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = "2", coordinates = @Loc(value = POINT_2_LOCATION)) },

            edges = { @Edge(id = "3", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION) }) },

            relations = { @Relation(id = "22", members = {
                    @Member(id = "1", type = "node", role = "node role"),
                    @Member(id = "3", type = "edge", role = "edge role") }) }

    )
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
