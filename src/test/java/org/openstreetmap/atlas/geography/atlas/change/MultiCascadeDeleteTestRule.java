package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author Yazad Khambata
 */
public class MultiCascadeDeleteTestRule extends CoreTestRule
{
    private static final String LOC_A = "1,1";
    private static final String LOC_B = "2,2";
    private static final String LOC_C = "3,3";

    public static final String strNodeA = "1";
    public static final String strNodeB = "2";
    public static final String strNodeC = "3";

    public static final String strEdgeA = "1";
    public static final String strEdgeB = "2";

    public static final Long nodeA = Long.valueOf(strNodeA);
    public static final Long nodeB = Long.valueOf(strNodeB);
    public static final Long nodeC = Long.valueOf(strNodeC);

    public static final Long edgeA = Long.valueOf(strEdgeA);
    public static final Long edgeB = Long.valueOf(strEdgeB);

    @TestAtlas(
            nodes = {
                    @TestAtlas.Node(id = strNodeA, coordinates = @TestAtlas.Loc(value = LOC_A)),
                    @TestAtlas.Node(id = strNodeB, coordinates = @TestAtlas.Loc(value = LOC_B)),
                    @TestAtlas.Node(id = strNodeC, coordinates = @TestAtlas.Loc(value = LOC_C)),
            },

            edges = {
                    @TestAtlas.Edge(id = strEdgeA, coordinates = { @TestAtlas.Loc(value = LOC_A), @TestAtlas.Loc(value = LOC_B) }),
                    @TestAtlas.Edge(id = strEdgeB, coordinates = { @TestAtlas.Loc(value = LOC_B), @TestAtlas.Loc(value = LOC_C) })
            }
        )
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
