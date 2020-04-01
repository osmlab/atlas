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
    public static final String strEdgeAB = "1";
    public static final String strEdgeBC = "2";
    public static final String strRelationX = "1";

    public static final Long nodeA = Long.valueOf(strNodeA);
    public static final Long nodeB = Long.valueOf(strNodeB);
    public static final Long nodeC = Long.valueOf(strNodeC);

    public static final Long edgeAB = Long.valueOf(strEdgeAB);
    public static final Long edgeBC = Long.valueOf(strEdgeBC);

    public static final Long relationX = Long.valueOf(strRelationX);

    @TestAtlas(nodes = {
            @TestAtlas.Node(id = strNodeA, coordinates = @TestAtlas.Loc(value = LOC_A)),
            @TestAtlas.Node(id = strNodeB, coordinates = @TestAtlas.Loc(value = LOC_B)),
            @TestAtlas.Node(id = strNodeC, coordinates = @TestAtlas.Loc(value = LOC_C)), },

            edges = {
                    @TestAtlas.Edge(id = strEdgeAB, coordinates = { @TestAtlas.Loc(value = LOC_A),
                            @TestAtlas.Loc(value = LOC_B) }),
                    @TestAtlas.Edge(id = strEdgeBC, coordinates = { @TestAtlas.Loc(value = LOC_B),
                            @TestAtlas.Loc(value = LOC_C) }), },

            relations = { @TestAtlas.Relation(id = strRelationX, members = {
                    @TestAtlas.Relation.Member(id = strEdgeAB, role = "x", type = "edge"),
                    @TestAtlas.Relation.Member(id = strEdgeBC, role = "y", type = "edge"),
                    @TestAtlas.Relation.Member(id = strNodeB, role = "y", type = "node"), }) })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
