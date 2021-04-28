package org.openstreetmap.atlas.geography.atlas.complete;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * @author Yazad Khambata
 */
public class CompleteItemTypeTestRule extends CoreTestRule
{
    public static final String ONE = "35.3,-128.03";
    public static final String TWO = "37.4,-127.02";

    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)) },

            edges = { @Edge(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }) },

            areas = { @Area(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }) },

            lines = { @Line(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }) },

            points = { @Point(id = "1", coordinates = @Loc(value = ONE)) },

            relations = { @Relation(id = "1", members = {
                    @Member(id = "1", type = "node", role = "node-role"),
                    @Member(id = "1", type = "edge", role = "edge-role") }) })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
