package org.openstreetmap.atlas.geography.atlas.statistics.coverage.poi;

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
 * @author matthieun
 */
public class CountCoverageTestCaseRule extends CoreTestRule
{
    private static final String ONE = "37.780574, -122.472852";
    private static final String TWO = "37.780592, -122.472242";
    private static final String THREE = "37.780724, -122.472249";

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

            }, areas = { @Area(id = "0", coordinates = { @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE) }, tags = { "addr:housenumber=25" }), @Area(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE) }, tags = { "natural=water", "water=lake" }), @Area(id = "2", coordinates = { @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE) }, tags = { "natural=water" }), @Area(id = "3", coordinates = { @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE) }, tags = { "amenity=hospital" }), @Area(id = "4", coordinates = { @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE) }, tags = { "amenity=unknown", "aeroway=aerodrome" }), @Area(id = "5", coordinates = { @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE) }, tags = { "addr:housenumber=30", "addr:street=wardell" })

            }, lines = {

                    @Line(id = "0", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "railway=station", "FIXME=0" }),
                    @Line(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "train=yes", "public_transport=stop_position" })

            }, points = {

                    @Point(id = "0", coordinates = @Loc(value = ONE), tags = { "addr:street=coco",
                            "addr:housenumber=25" }),
                    @Point(id = "1", coordinates = @Loc(value = ONE), tags = {
                            "fixme=wrong name" }),
                    @Point(id = "2", coordinates = @Loc(value = ONE), tags = { "landuse=basin" }),
                    @Point(id = "3", coordinates = @Loc(value = ONE), tags = { "amenity=school" })

            }, relations = {

                    @Relation(id = "1", tags = { "type=restriction",
                            "restriction=no_left_turn" }, members = {
                                    @Member(id = "0", role = "from", type = "edge"),
                                    @Member(id = "2", role = "via", type = "node"),
                                    @Member(id = "1", role = "to", type = "edge") })

            })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
