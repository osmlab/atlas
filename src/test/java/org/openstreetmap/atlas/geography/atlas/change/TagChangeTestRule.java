package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author Yazad Khambata
 */
public class TagChangeTestRule extends CoreTestRule
{

    public static final String HELLO_WORLD = "hello=world";
    public static final String CHANGE_ME = "change=me";
    public static final String DELETE_ME = "delete=me";
    public static final String MARS_ROVER = "mars=rover";
    public static final String SINGLETON_EMPTY = "singleton=";

    private static final String ID_STR_1 = "1";
    public static final Long ID_1 = Long.valueOf(ID_STR_1);

    private static final String ID_STR_2 = "2";
    public static final Long ID_2 = Long.valueOf(ID_STR_2);

    private static final String NODE = "12.7,116.5";
    private static final String EDGE_1 = "10.1,149.4";
    private static final String EDGE_2 = "19.9,137.0";
    private static final String EDGE_3 = "13.4,148.4";
    private static final String AREA_1 = "19.7,122.1";
    private static final String AREA_2 = "10.7,112.9";
    private static final String AREA_3 = "16.3,138.9";
    private static final String AREA_4 = "11.5,135.6";
    private static final String AREA_5 = "19.7,122.1";
    private static final String AREA_6 = "17.9,111.9";
    private static final String AREA_7 = AREA_1;
    @TestAtlas(

            nodes = {
                    @TestAtlas.Node(id = ID_STR_1, coordinates = @TestAtlas.Loc(value = NODE), tags = {
                            HELLO_WORLD, CHANGE_ME, DELETE_ME }),

                    @TestAtlas.Node(id = ID_STR_2, coordinates = @TestAtlas.Loc(value = EDGE_1), tags = {
                            MARS_ROVER, SINGLETON_EMPTY }),
                    @TestAtlas.Node(id = "3", coordinates = @TestAtlas.Loc(value = EDGE_2)),
                    @TestAtlas.Node(id = "4", coordinates = @TestAtlas.Loc(value = EDGE_3)),

                    @TestAtlas.Node(id = "5", coordinates = @TestAtlas.Loc(value = AREA_1)),
                    @TestAtlas.Node(id = "6", coordinates = @TestAtlas.Loc(value = AREA_2)),
                    @TestAtlas.Node(id = "7", coordinates = @TestAtlas.Loc(value = AREA_3)),
                    @TestAtlas.Node(id = "8", coordinates = @TestAtlas.Loc(value = AREA_4)),
                    @TestAtlas.Node(id = "9", coordinates = @TestAtlas.Loc(value = AREA_5)),
                    @TestAtlas.Node(id = "10", coordinates = @TestAtlas.Loc(value = AREA_6)),
                    @TestAtlas.Node(id = "11", coordinates = @TestAtlas.Loc(value = AREA_7)) },

            points = {
                    @TestAtlas.Point(id = ID_STR_1, coordinates = @TestAtlas.Loc(value = NODE), tags = {
                            HELLO_WORLD, CHANGE_ME, DELETE_ME }),

                    @TestAtlas.Point(id = ID_STR_2, coordinates = @TestAtlas.Loc(value = EDGE_1), tags = {
                            MARS_ROVER, SINGLETON_EMPTY }), },

            edges = { @TestAtlas.Edge(id = ID_STR_1, coordinates = { @TestAtlas.Loc(value = EDGE_1),
                    @TestAtlas.Loc(value = EDGE_2),
                    @TestAtlas.Loc(value = EDGE_3) }, tags = { HELLO_WORLD, CHANGE_ME, DELETE_ME }),

                    @TestAtlas.Edge(id = ID_STR_2, coordinates = { @TestAtlas.Loc(value = EDGE_1),
                            @TestAtlas.Loc(value = EDGE_3) }, tags = { MARS_ROVER,
                                    SINGLETON_EMPTY })

            },

            lines = { @TestAtlas.Line(id = ID_STR_1, coordinates = { @TestAtlas.Loc(value = EDGE_1),
                    @TestAtlas.Loc(value = EDGE_2),
                    @TestAtlas.Loc(value = EDGE_3) }, tags = { HELLO_WORLD, CHANGE_ME, DELETE_ME }),

                    @TestAtlas.Line(id = ID_STR_2, coordinates = { @TestAtlas.Loc(value = EDGE_1),
                            @TestAtlas.Loc(value = EDGE_3) }, tags = { MARS_ROVER,
                                    SINGLETON_EMPTY })

            },

            areas = { @TestAtlas.Area(id = ID_STR_1, coordinates = { @TestAtlas.Loc(value = AREA_1),
                    @TestAtlas.Loc(value = AREA_2), @TestAtlas.Loc(value = AREA_3),
                    @TestAtlas.Loc(value = AREA_6),
                    @TestAtlas.Loc(value = AREA_7) }, tags = { HELLO_WORLD, CHANGE_ME, DELETE_ME }),

                    @TestAtlas.Area(id = ID_STR_2, coordinates = { @TestAtlas.Loc(value = AREA_1),
                            @TestAtlas.Loc(value = AREA_4), @TestAtlas.Loc(value = AREA_5),
                            @TestAtlas.Loc(value = AREA_7) }, tags = { MARS_ROVER,
                                    SINGLETON_EMPTY }), },

            relations = { @TestAtlas.Relation(id = ID_STR_1, members = {
                    @TestAtlas.Relation.Member(id = ID_STR_1, role = "some role 1", type = "node"),
                    @TestAtlas.Relation.Member(id = ID_STR_1, role = "some role 2", type = "edge"),
                    @TestAtlas.Relation.Member(id = ID_STR_1, role = "some role 3", type = "area"), }, tags = {
                            HELLO_WORLD, CHANGE_ME, DELETE_ME }),

                    @TestAtlas.Relation(id = ID_STR_2, members = {
                            @TestAtlas.Relation.Member(id = ID_STR_2, role = "some role 1", type = "node"),
                            @TestAtlas.Relation.Member(id = ID_STR_2, role = "some role 2", type = "point"),
                            @TestAtlas.Relation.Member(id = ID_STR_2, role = "some role 3", type = "edge"),
                            @TestAtlas.Relation.Member(id = ID_STR_2, role = "some role 4", type = "line"),
                            @TestAtlas.Relation.Member(id = ID_STR_2, role = "some role 5", type = "area"),
                            @TestAtlas.Relation.Member(id = ID_STR_1, role = "some role 6", type = "relation"), }, tags = {
                                    MARS_ROVER, SINGLETON_EMPTY }) })
    private final Atlas atlas = null;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
