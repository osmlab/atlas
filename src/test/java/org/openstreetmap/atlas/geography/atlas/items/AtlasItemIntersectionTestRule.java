package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link AtlasItemIntersectionTest} test data.
 *
 * @author mgostintsev
 */
public class AtlasItemIntersectionTestRule extends CoreTestRule
{
    private static final String LOCATION_1 = "47.625534, -122.210083";
    private static final String LOCATION_2 = "47.625576, -122.208305";
    private static final String LOCATION_3 = "47.626934, -122.208412";

    private static final String LOCATION_4 = "47.6183566, -122.2888258";
    private static final String LOCATION_5 = "47.6183727, -122.2889442";
    private static final String LOCATION_6 = "47.6182798, -122.2886447";
    private static final String LOCATION_7 = "47.6182598, -122.2886447";
    private static final String LOCATION_8 = "47.6182898, -122.2886347";
    private static final String LOCATION_9 = "47.6182718, -122.2886647";
    private static final String LOCATION_10 = "47.6182998, -122.2886347";
    private static final String LOCATION_11 = "47.6182218, -122.2886647";

    @TestAtlas(loadFromTextResource = "intersectionAtlas.atlas.txt")
    private Atlas intersectionAtlas;

    @TestAtlas(areas = { @Area(coordinates = { @Loc(value = LOCATION_1), @Loc(value = LOCATION_2),
            @Loc(value = LOCATION_3), @Loc(value = LOCATION_1) }) })
    private Atlas noIntersectionAtlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "4", coordinates = @Loc(value = LOCATION_4)),
                    @Node(id = "5", coordinates = @Loc(value = LOCATION_5)),
                    @Node(id = "6", coordinates = @Loc(value = LOCATION_6)),
                    @Node(id = "7", coordinates = @Loc(value = LOCATION_7)),
                    @Node(id = "8", coordinates = @Loc(value = LOCATION_8)),
                    @Node(id = "9", coordinates = @Loc(value = LOCATION_9)),
                    @Node(id = "10", coordinates = @Loc(value = LOCATION_10)),
                    @Node(id = "11", coordinates = @Loc(value = LOCATION_11))
            // 4, 5
            // 4, 6
            // 4, 7
            // 8, 9
            // 10, 11

            }, edges = {

                    @Edge(id = "0", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_5) }, tags = { "highway=residential" }),
                    @Edge(id = "1", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_6) }, tags = { "highway=residential" }),
                    @Edge(id = "2", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_7) }, tags = { "highway=trunk" }),
                    @Edge(id = "3", coordinates = { @Loc(value = LOCATION_8),
                            @Loc(value = LOCATION_9) }, tags = { "highway=trunk" }),
                    @Edge(id = "4", coordinates = { @Loc(value = LOCATION_10),
                            @Loc(value = LOCATION_11) }, tags = { "highway=trunk" })

            }, lines = {

                    @Line(id = "0", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_5) }, tags = { "railway=station" }),
                    @Line(id = "1", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_6) }, tags = { "railway=station" }),
                    @Line(id = "2", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_7) }, tags = { "railway=station" }),
                    @Line(id = "3", coordinates = { @Loc(value = LOCATION_8),
                            @Loc(value = LOCATION_9) }, tags = { "railway=station" }),
                    @Line(id = "4", coordinates = { @Loc(value = LOCATION_10),
                            @Loc(value = LOCATION_11) }, tags = { "railway=station" }),

            }
    // , areas = {

    // @Area(id = "0", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
    // @Loc(value = THREE) }, tags = { "addr:housenumber=25" }),
    // @Area(id = "1", coordinates = { @Loc(value = TWO), @Loc(value = THREE),
    // @Loc(value = FOUR) }, tags = { "natural=water", "water=lake" })

    // }, relations = {
    //
    // @Relation(id = "1", tags = { "type=restriction",
    // "restriction=no_u_turn" }, members = {
    // @Member(id = "0", role = "from", type = "edge"),
    // @Member(id = "2", role = "via", type = "node"),
    // @Member(id = "1", role = "to", type = "edge") }),
    // @Relation(id = "2", tags = { "type=route", "route=bus" }, members = {
    // @Member(id = "2", role = "", type = "edge"),
    // @Member(id = "3", role = "", type = "edge") })
    //
    // }
    )
    private Atlas withinTestAtlas;

    public Atlas getIntersectionAtlas()
    {
        return this.intersectionAtlas;
    }

    public Atlas getNoIntersectionAtlas()
    {
        return this.noIntersectionAtlas;
    }

    public Atlas getWithinTestAtlas()
    {
        return this.withinTestAtlas;
    }
}
