package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author Yazad Khambata
 */
public class CascadeDeleteTestRule extends CoreTestRule
{
    public static final long EDGE_IDENTIFIER = 1L;
    public static final long EDGE_RELATION_IDENTIFIER = 1L;
    public static final long LINE_IDENTIFIER = 1L;
    public static final long LINE_RELATION_IDENTIFIER = 2L;
    public static final long AREA_IDENTIFIER = 0L;
    public static final long AREA_RELATION_IDENTIFIER = 2L;
    public static final long POINT_IDENTIFIER = 1L;
    public static final long POINT_RELATION_IDENTIFIER = 3L;
    public static final long NON_EDGE_NODE_IDENTIFIER = 5L;
    public static final long NON_EDGE_NODE_RELATION_IDENTIFIER = 6L;
    public static final long START_END_EDGE_NODE = 3L;
    public static final long TOP_LEVEL_RELATION_IDENTIFIER = 6L;
    public static final long SUB_RELATION_IDENTIFIER = 5L;
    public static final long PARENT_RELATION_IDENTIFIER = 4L;
    public static final long THE_ONLY_RELATION_MEMBER_POINT_IDENTIFIER = 0L;
    public static final long ONE_MEMBER_RELATION_IDENTIFIER = 7L;

    public static final long NODE_COUNT = 6;
    public static final long POINT_COUNT = 4;
    public static final long EDGE_COUNT = 4;
    public static final long LINE_COUNT = 2;
    public static final long AREA_COUNT = 2;
    public static final long RELATION_COUNT = 7;

    private static final String ONE = "37.780574, -122.472852";
    private static final String TWO = "37.780592, -122.472242";
    private static final String THREE = "37.780724, -122.472249";
    private static final String FOUR = "37.780825, -122.471896";
    private static final String FIVE = "38, -123";
    private static final String SIX = "39, -124";

    private final Map<ItemType, Long> countExpectationMapping = new HashMap()
    {
        private static final long serialVersionUID = 6255547290912151165L;
    
        {
            put(ItemType.NODE, NODE_COUNT);
            put(ItemType.POINT, POINT_COUNT);
            put(ItemType.EDGE, EDGE_COUNT);
            put(ItemType.LINE, LINE_COUNT);
            put(ItemType.AREA, AREA_COUNT);
            put(ItemType.RELATION, RELATION_COUNT);
        }
    };

    @TestAtlas(nodes = { @TestAtlas.Node(id = "1", coordinates = @TestAtlas.Loc(value = ONE)),
            @TestAtlas.Node(id = "2", coordinates = @TestAtlas.Loc(value = TWO)),
            @TestAtlas.Node(id = "3", coordinates = @TestAtlas.Loc(value = THREE)),
            @TestAtlas.Node(id = "4", coordinates = @TestAtlas.Loc(value = FOUR)),
            @TestAtlas.Node(id = "5", coordinates = @TestAtlas.Loc(value = FIVE)),
            @TestAtlas.Node(id = "6", coordinates = @TestAtlas.Loc(value = SIX)) },

            edges = {
                    @TestAtlas.Edge(id = "0", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO) }),
                    @TestAtlas.Edge(id = "1", coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }),
                    @TestAtlas.Edge(id = "-1", coordinates = { @TestAtlas.Loc(value = THREE),
                            @TestAtlas.Loc(value = TWO) }),
                    @TestAtlas.Edge(id = "2", coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = SIX) }) },

            areas = {
                    @TestAtlas.Area(id = "0", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO), @TestAtlas.Loc(value = THREE) }),
                    @TestAtlas.Area(id = "1", coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE), @TestAtlas.Loc(value = FOUR) }) },

            lines = {
                    @TestAtlas.Line(id = "0", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO) }),
                    @TestAtlas.Line(id = "1", coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = FOUR) }) },

            points = { @TestAtlas.Point(id = "0", coordinates = @TestAtlas.Loc(value = ONE)),
                    @TestAtlas.Point(id = "1", coordinates = @TestAtlas.Loc(value = TWO)),
                    @TestAtlas.Point(id = "2", coordinates = @TestAtlas.Loc(value = THREE)),
                    @TestAtlas.Point(id = "3", coordinates = @TestAtlas.Loc(value = FOUR)) },

            relations = {
                    @TestAtlas.Relation(id = "1", members = {
                            @TestAtlas.Relation.Member(id = "0", role = "from", type = "edge"),
                            @TestAtlas.Relation.Member(id = "2", role = "via", type = "node"),
                            @TestAtlas.Relation.Member(id = "1", role = "to", type = "edge"),
                            @TestAtlas.Relation.Member(id = "-1", role = "to", type = "edge") }),

                    @TestAtlas.Relation(id = "2", members = {
                            @TestAtlas.Relation.Member(id = "0", role = "inside", type = "area"),
                            @TestAtlas.Relation.Member(id = "1", role = "outside", type = "line") }),

                    @TestAtlas.Relation(id = "3", tags = { "type=outside" }, members = {
                            @TestAtlas.Relation.Member(id = "1", role = "outside", type = "area"),
                            @TestAtlas.Relation.Member(id = "1", role = "outside", type = "line"),
                            @TestAtlas.Relation.Member(id = "1", role = "outside", type = "point") }),

                    @TestAtlas.Relation(id = "5", members = {
                            @TestAtlas.Relation.Member(id = "1", role = "a", type = "relation"),
                            @TestAtlas.Relation.Member(id = "1", role = "b", type = "line") }),

                    @TestAtlas.Relation(id = "4", members = {
                            @TestAtlas.Relation.Member(id = "5", role = "a", type = "relation"),
                            @TestAtlas.Relation.Member(id = "2", role = "b", type = "node") }),

                    @TestAtlas.Relation(id = "6", members = {
                            @TestAtlas.Relation.Member(id = "5", role = "a", type = "node"),
                            @TestAtlas.Relation.Member(id = "3", role = "b", type = "node"),
                            @TestAtlas.Relation.Member(id = "1", role = "c", type = "edge"),
                            @TestAtlas.Relation.Member(id = "-1", role = "d", type = "edge") }),

                    @TestAtlas.Relation(id = "7", members = {
                            @TestAtlas.Relation.Member(id = "0", role = "a", type = "point"), }),

            })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Map<ItemType, Long> getCountExpectationMapping()
    {
        return new HashMap<>(this.countExpectationMapping);
    }
}
