package org.openstreetmap.atlas.geography.atlas;

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
 * @author matthieun
 */
public class BareAtlasTestRule extends CoreTestRule
{
    private static final String ONE = "37.780574, -122.472852";
    private static final String TWO = "37.780592, -122.472242";
    private static final String THREE = "37.780724, -122.472249";
    private static final String FOUR = "37.780825, -122.471896";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE))

            }, edges = {

            @Edge(id = "0", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                    "highway=trunk", "fixme=please" }),
            @Edge(id = "1", coordinates = { @Loc(value = TWO),
                    @Loc(value = THREE) }, tags = { "highway=trunk" })

    }, areas = {

            @Area(id = "0", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                    @Loc(value = THREE) }, tags = { "addr:housenumber=25" }),
            @Area(id = "1", coordinates = { @Loc(value = TWO), @Loc(value = THREE),
                    @Loc(value = FOUR) }, tags = { "natural=water", "water=lake" })

    }, lines = {

            @Line(id = "0", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                    "railway=station", "FIXME=0" }),
            @Line(id = "1", coordinates = { @Loc(value = TWO),
                    @Loc(value = FOUR) }, tags = { "train=yes",
                    "public_transport=stop_position" })

    }, points = {

            @Point(id = "0", coordinates = @Loc(value = ONE), tags = { "addr:street=coco",
                    "addr:housenumber=25" }),
            @Point(id = "1", coordinates = @Loc(value = TWO), tags = {
                    "fixme=wrong name" }),
            @Point(id = "2", coordinates = @Loc(value = THREE), tags = { "landuse=basin" }),
            @Point(id = "3", coordinates = @Loc(value = FOUR), tags = { "amenity=school" })

    }, relations = {

            @Relation(id = "1", tags = { "type=restriction",
                    "restriction=no_left_turn" }, members = {
                    @Member(id = "0", role = "from", type = "edge"),
                    @Member(id = "2", role = "via", type = "node"),
                    @Member(id = "1", role = "to", type = "edge") }),
            @Relation(id = "2", tags = { "type=half_inside" }, members = {
                    @Member(id = "0", role = "inside", type = "area"),
                    @Member(id = "1", role = "outside", type = "line") }),
            @Relation(id = "3", tags = { "type=outside" }, members = {
                    @Member(id = "1", role = "outside", type = "area"),
                    @Member(id = "1", role = "outside", type = "line") }),
            @Relation(id = "5", tags = {
                    "type=inside_because_of_relation_inside" }, members = {
                    @Member(id = "1", role = "inside", type = "relation"),
                    @Member(id = "1", role = "outside", type = "line") }),
            @Relation(id = "4", tags = {
                    "type=inside_because_of_level_2_relation_inside" }, members = {
                    @Member(id = "5", role = "inside", type = "relation") })

    })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
