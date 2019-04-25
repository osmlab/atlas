package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.Location;
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
 * {@link RouteTest} test data.
 *
 * @author mgostintsev
 */
public class RouteTestRule extends CoreTestRule
{
    private static final String ONE = "39.9970447, 116.279489";
    private static final String TWO = "39.9974907, 116.2835146";
    private static final String THREE = "39.9976118, 116.2836054";
    private static final String FOUR = "40.0007062, 116.2829521";
    private static final String FIVE = "40.0082021, 116.2824999";
    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),

            }, edges = {

                    @Edge(id = "159019301", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary" }),
                    @Edge(id = "128620751", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "128620796", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=tertiary", "oneway=yes" }),
                    @Edge(id = "138620888", coordinates = { @Loc(value = THREE),
                            @Loc(value = FIVE) }, tags = { "highway=tertiary", "oneway=yes" }) })
    private Atlas atlas;
    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR))

            }, edges = {

                    @Edge(id = "159019301", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "28620796", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=tertiary", }),
                    @Edge(id = "-28620796", coordinates = { @Loc(value = THREE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary", }),
                    @Edge(id = "138620888", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=tertiary", }),
                    @Edge(id = "-138620888", coordinates = { @Loc(value = FOUR),
                            @Loc(value = THREE) }, tags = { "highway=tertiary", }) })
    private Atlas biDirectionalEdgeAtlas;
    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR))

            }, edges = {

                    @Edge(id = "159019301", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "28620796", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=tertiary", }),
                    @Edge(id = "138620888", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=tertiary", }),
                    @Edge(id = "-138620888", coordinates = { @Loc(value = FOUR),
                            @Loc(value = THREE) }, tags = { "highway=tertiary", }) })
    private Atlas uniDirectionalEdgeAtlas;
    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),

            }, edges = {

                    @Edge(id = "206786592000008", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-206786592000008", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=tertiary" }),
                    @Edge(id = "206786592000007", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "-206786592000007", coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE) }, tags = { "highway=tertiary_link" }) })
    private Atlas routeHashCodeAtlas;
    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),

            }, edges = {

                    @Edge(id = "159019301", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=tertiary" }),
                    @Edge(id = "128620751", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "-128620751", coordinates = { @Loc(value = THREE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "128620796", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=tertiary", "oneway=yes" }),
                    @Edge(id = "-128620796", coordinates = { @Loc(value = FOUR),
                            @Loc(value = THREE) }, tags = { "highway=tertiary", "oneway=yes" }),
                    @Edge(id = "138620888", coordinates = { @Loc(value = FOUR),
                            @Loc(value = FIVE) }, tags = { "highway=tertiary", "oneway=yes" }),
                    @Edge(id = "-138620888", coordinates = { @Loc(value = FIVE),
                            @Loc(value = FOUR) }, tags = { "highway=tertiary", "oneway=yes" }),
                    @Edge(id = "138620889", coordinates = { @Loc(value = TWO),
                            @Loc(value = FIVE) }, tags = { "highway=tertiary", "oneway=yes" }) })

    private Atlas uTurnAtlas;

    @TestAtlas(points = {
            @Point(id = "1", coordinates = @Loc(value = Location.TEST_3_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "2", coordinates = @Loc(value = Location.TEST_2_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "3", coordinates = @Loc(value = Location.TEST_6_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "4", coordinates = @Loc(value = Location.TEST_7_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "5", coordinates = @Loc(value = Location.TEST_4_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "6", coordinates = @Loc(value = Location.TEST_1_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "7", coordinates = @Loc(value = Location.TEST_5_COORDINATES), tags = {
                    "addr:city=Cupertino" }), },

            nodes = {
                    @Node(id = "123", coordinates = @Loc(value = Location.TEST_6_COORDINATES), tags = {
                            "highway=turning_circle" }),
                    @Node(id = "1234", coordinates = @Loc(value = Location.TEST_5_COORDINATES), tags = {
                            "highway=turning_circle" }),
                    @Node(id = "12345", coordinates = @Loc(value = Location.TEST_2_COORDINATES), tags = {
                            "highway=turning_circle" }), },

            lines = {
                    @Line(id = "32", coordinates = { @Loc(value = Location.TEST_5_COORDINATES),
                            @Loc(value = Location.TEST_1_COORDINATES) }, tags = { "power=line" }),
                    @Line(id = "23", coordinates = { @Loc(value = Location.TEST_3_COORDINATES),
                            @Loc(value = Location.TEST_2_COORDINATES),
                            @Loc(value = Location.TEST_4_COORDINATES) }, tags = {
                                    "aeroway=runway" }),
                    @Line(id = "24", coordinates = { @Loc(value = Location.TEST_7_COORDINATES),
                            @Loc(value = Location.TEST_4_COORDINATES),
                            @Loc(value = Location.TEST_6_COORDINATES),
                            @Loc(value = Location.TEST_2_COORDINATES) }, tags = {
                                    "natural=coastline", "waterway=canal" }) },

            edges = {
                    @Edge(id = "9", coordinates = { @Loc(value = Location.TEST_6_COORDINATES),
                            @Loc(value = Location.TEST_5_COORDINATES) }, tags = { "highway=primary",
                                    "name=edge9", "surface=concrete", "lanes=3" }),
                    @Edge(id = "-9", coordinates = { @Loc(value = Location.TEST_5_COORDINATES),
                            @Loc(value = Location.TEST_6_COORDINATES) }, tags = { "highway=primary",
                                    "name=edge_9", "surface=fine_gravel" }),
                    @Edge(id = "98", coordinates = { @Loc(value = Location.TEST_5_COORDINATES),
                            @Loc(value = Location.TEST_2_COORDINATES) }, tags = {
                                    "highway=secondary", "name=edge98", "bridge=movable",
                                    "maxspeed=100" }),
                    @Edge(id = "987", coordinates = { @Loc(value = Location.TEST_2_COORDINATES),
                            @Loc(value = Location.TEST_6_COORDINATES) }, tags = {
                                    "highway=residential", "name=edge987", "tunnel=culvert",
                                    "maxspeed=50 knots" }) },

            areas = {
                    @Area(id = "45", coordinates = { @Loc(value = Location.TEST_6_COORDINATES),
                            @Loc(value = Location.TEST_3_COORDINATES),
                            @Loc(value = Location.TEST_2_COORDINATES) }, tags = {
                                    "leisure=golf_course", "natural=grassland" }),
                    @Area(id = "54", coordinates = { @Loc(value = Location.TEST_5_COORDINATES),
                            @Loc(value = Location.TEST_1_COORDINATES),
                            @Loc(value = Location.TEST_4_COORDINATES),
                            @Loc(value = Location.TEST_6_COORDINATES) }, tags = {
                                    "leisure=swimming_pool", "sport=swimming" }),
                    @Area(id = "4554", coordinates = { @Loc(value = Location.TEST_1_COORDINATES),
                            @Loc(value = Location.TEST_2_COORDINATES),
                            @Loc(value = Location.TEST_3_COORDINATES) }, tags = {
                                    "hello=world" }) },

            relations = {
                    @Relation(id = "1", members = { @Member(id = "9", role = "in", type = "edge"),
                            @Member(id = "1234", role = "node", type = "node"),
                            @Member(id = "-9", role = "out", type = "edge") }, tags = {}),
                    @Relation(id = "2", members = {
                            @Member(id = "45", role = "area", type = "area"),
                            @Member(id = "32", role = "line", type = "line"),
                            @Member(id = "5", role = "pt", type = "point"),
                            @Member(id = "1", role = "rel", type = "relation"),
                            @Member(id = "1234", role = "node", type = "node") }, tags = {}) })
    private Atlas packedAtlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getBiDirectionalEdgeAtlas()
    {
        return this.biDirectionalEdgeAtlas;
    }

    public Atlas getPackedAtlas()
    {
        return this.packedAtlas;
    }

    public Atlas getRouteHashCodeAtlas()
    {
        return this.routeHashCodeAtlas;
    }

    public Atlas getUniDirectionalEdgeAtlas()
    {
        return this.uniDirectionalEdgeAtlas;
    }

    public Atlas getUTurnAtlas()
    {
        return this.uTurnAtlas;
    }
}
