package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author Yazad Khambata
 */
public class WithinTestRule extends CoreTestRule
{

    private static final String ID_STR = "1";
    public static final Long ID = Long.valueOf(ID_STR);

    private static final String NODE = "12.6559437,116.9700514";

    private static final String EDGE_1 = "10.1883391,149.4092916";
    private static final String EDGE_2 = "19.9880592,137.0582643";
    private static final String EDGE_3 = "13.4555177,148.4934186";

    private static final String AREA_1 = "19.7435337,122.1343008";
    private static final String AREA_2 = "10.7633736,112.9680864";
    private static final String AREA_3 = "16.3182722,138.9217453";
    private static final String AREA_4 = "11.535563,135.6781525";
    private static final String AREA_5 = "19.7435337,122.1343008";
    private static final String AREA_6 = "17.9736782,111.9741523";
    private static final String AREA_7 = AREA_1;

    @TestAtlas(

            nodes = {
                    @TestAtlas.Node(id = WithinTestRule.ID_STR, coordinates = @TestAtlas.Loc(value = NODE)),

                    @TestAtlas.Node(id = "2", coordinates = @TestAtlas.Loc(value = EDGE_1)),
                    @TestAtlas.Node(id = "3", coordinates = @TestAtlas.Loc(value = EDGE_2)),
                    @TestAtlas.Node(id = "4", coordinates = @TestAtlas.Loc(value = EDGE_3)),

                    @TestAtlas.Node(id = "5", coordinates = @TestAtlas.Loc(value = AREA_1)),
                    @TestAtlas.Node(id = "6", coordinates = @TestAtlas.Loc(value = AREA_2)),
                    @TestAtlas.Node(id = "7", coordinates = @TestAtlas.Loc(value = AREA_3)),
                    @TestAtlas.Node(id = "8", coordinates = @TestAtlas.Loc(value = AREA_4)),
                    @TestAtlas.Node(id = "9", coordinates = @TestAtlas.Loc(value = AREA_5)),
                    @TestAtlas.Node(id = "10", coordinates = @TestAtlas.Loc(value = AREA_6)),
                    @TestAtlas.Node(id = "11", coordinates = @TestAtlas.Loc(value = AREA_7)) }, edges = {
                            @TestAtlas.Edge(id = "1", coordinates = {
                                    @TestAtlas.Loc(value = EDGE_1), @TestAtlas.Loc(value = EDGE_2),
                                    @TestAtlas.Loc(value = EDGE_3) }) }, areas = {
                                            @TestAtlas.Area(id = "1", coordinates = {
                                                    @TestAtlas.Loc(value = AREA_1),
                                                    @TestAtlas.Loc(value = AREA_2),
                                                    @TestAtlas.Loc(value = AREA_3),
                                                    @TestAtlas.Loc(value = AREA_4),
                                                    @TestAtlas.Loc(value = AREA_5),
                                                    @TestAtlas.Loc(value = AREA_6),
                                                    @TestAtlas.Loc(value = AREA_7) }) }, relations = {
                                                            @TestAtlas.Relation(id = "1", members = {
                                                                    @TestAtlas.Relation.Member(id = "1", role = "some role 1", type = "node"),
                                                                    @TestAtlas.Relation.Member(id = "1", role = "some role 2", type = "edge"),
                                                                    @TestAtlas.Relation.Member(id = "1", role = "some role 3", type = "area"), }) })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
