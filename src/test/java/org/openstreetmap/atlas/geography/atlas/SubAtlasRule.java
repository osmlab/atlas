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
public class SubAtlasRule extends CoreTestRule
{
    private static final String ONE = "37.780574, -122.472852";
    private static final String TWO = "37.780592, -122.472242";
    private static final String THREE = "37.780724, -122.472249";
    private static final String FOUR = "37.780825, -122.471896";
    private static final String FIVE = "37.780835, -122.471896";

    private static final String SIX = "37.045982,-121.7539795";
    private static final String SIX_PRIME = "37.0459867,-121.7539853";
    private static final String SEVEN = "37.0459913,-121.7539913";

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
                    @Relation(id = "4", tags = {
                            "type=inside_because_of_relation_inside" }, members = {
                                    @Member(id = "1", role = "inside", type = "relation"),
                                    @Member(id = "1", role = "outside", type = "line") }),
                    @Relation(id = "5", tags = {
                            "type=inside_because_of_level_2_relation_inside" }, members = {
                                    @Member(id = "4", role = "inside", type = "relation") })

            })
    private Atlas atlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR))

            }, edges = {

                    @Edge(id = "0", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=trunk", "fixme=please" }),
                    @Edge(id = "1", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=trunk" })

            }, relations = {

                    // Must have a node that wasn't indexed by the above edges.
                    @Relation(id = "0", tags = { "type=outside" }, members = {
                            @Member(id = "1", role = "to", type = "node"),
                            @Member(id = "4", role = "via", type = "node") })

            })
    private Atlas nestedUnindexedNodeWithinRelationAtlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR), tags = { "type=excluded" }),
                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "type=excluded" })

            }, edges = {

                    @Edge(id = "0", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=trunk", "fixme=please" }),
                    @Edge(id = "1", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=trunk" })

            }, relations = {

                    @Relation(id = "8", tags = { "type=excluded" }, members = {
                            @Member(id = "1", role = "to", type = "node"),
                            @Member(id = "2", role = "via", type = "node") }),
                    @Relation(id = "9", tags = { "type=multipolygon" }, members = {
                            @Member(id = "8", role = "something", type = "relation"),
                            @Member(id = "1", role = "to", type = "node"),
                            @Member(id = "4", role = "excluded", type = "node") })

            })
    private Atlas filteredOutMemberRelationAtlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "6", coordinates = @Loc(value = SIX)),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN)),

            }, edges = {

                    @Edge(id = "12", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=trunk" }),

                    @Edge(id = "67", coordinates = { @Loc(value = SIX), @Loc(value = SIX_PRIME),
                            @Loc(value = SEVEN) }, tags = { "highway=primary" }),
                    @Edge(id = "-67", coordinates = { @Loc(value = SEVEN), @Loc(value = SIX_PRIME),
                            @Loc(value = SIX) }, tags = { "highway=primary" }),

                    @Edge(id = "76", coordinates = { @Loc(value = SEVEN), @Loc(value = SIX_PRIME),
                            @Loc(value = SIX) }, tags = { "highway=primary" }),
                    @Edge(id = "-76", coordinates = { @Loc(value = SIX), @Loc(value = SIX_PRIME),
                            @Loc(value = SEVEN) }, tags = { "highway=primary" })

            })
    private Atlas atlasWithEdgeAlongBoundary;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),
                    @Node(id = "6", coordinates = @Loc(value = SIX)),

            }, edges = {

                    @Edge(id = "0", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=residential" }),
                    @Edge(id = "1", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=residential" }),
                    @Edge(id = "2", coordinates = { @Loc(value = FOUR),
                            @Loc(value = FIVE) }, tags = { "highway=trunk" }),
                    @Edge(id = "3", coordinates = { @Loc(value = FIVE),
                            @Loc(value = SIX) }, tags = { "highway=trunk" })

            }, points = {

                    @Point(id = "0", coordinates = @Loc(value = ONE), tags = { "addr:street=coco",
                            "addr:housenumber=25" }),
                    @Point(id = "1", coordinates = @Loc(value = TWO), tags = {
                            "fixme=wrong name" }),
                    @Point(id = "2", coordinates = @Loc(value = THREE), tags = { "landuse=basin" }),
                    @Point(id = "3", coordinates = @Loc(value = FOUR), tags = { "amenity=school" })

            }, areas = {

                    @Area(id = "0", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "addr:housenumber=25" }),
                    @Area(id = "1", coordinates = { @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "natural=water", "water=lake" })

            }, relations = {

                    @Relation(id = "1", tags = { "type=restriction",
                            "restriction=no_u_turn" }, members = {
                                    @Member(id = "0", role = "from", type = "edge"),
                                    @Member(id = "2", role = "via", type = "node"),
                                    @Member(id = "1", role = "to", type = "edge") }),
                    @Relation(id = "2", tags = { "type=route", "route=bus" }, members = {
                            @Member(id = "2", role = "", type = "edge"),
                            @Member(id = "3", role = "", type = "edge") })

            })
    private Atlas hardCutPredicateAtlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getAtlasWithEdgeAlongBoundary()
    {
        return this.atlasWithEdgeAlongBoundary;
    }

    public Atlas getFilteredOutMemberRelationAtlas()
    {
        return this.filteredOutMemberRelationAtlas;
    }

    public Atlas getHardCutPredicateAtlas()
    {
        return this.hardCutPredicateAtlas;
    }

    public Atlas getNodeNestedWithinRelationAtlas()
    {
        return this.nestedUnindexedNodeWithinRelationAtlas;
    }
}
