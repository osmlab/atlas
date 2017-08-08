package org.openstreetmap.atlas.geography.atlas.multi;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Used for atlas generation for test cases in {@link MultiAtlasBorderFixerTest}
 *
 * @author mkalender
 */
public class MultiAtlasBorderFixerTestRule extends CoreTestRule
{
    public static final String POINT_1_ID = "1234567891000000";
    public static final String POINT_2_ID = "2234567891000000";
    public static final String POINT_3_ID = "3234567891000000";
    public static final String POINT_4_ID = "4234567891000000";
    public static final String POINT_5_ID = "5234567891000000";
    public static final String POINT_6_ID = "6234567891000000";
    public static final String POINT_7_ID = "7234567891000000";
    public static final String POINT_8_ID = "8234567891000000";

    public static final long POINT_1_ID_LONG = Long.parseLong(POINT_1_ID);
    public static final long POINT_2_ID_LONG = Long.parseLong(POINT_2_ID);
    public static final long POINT_3_ID_LONG = Long.parseLong(POINT_3_ID);
    public static final long POINT_4_ID_LONG = Long.parseLong(POINT_4_ID);
    public static final long POINT_5_ID_LONG = Long.parseLong(POINT_5_ID);
    public static final long POINT_6_ID_LONG = Long.parseLong(POINT_6_ID);
    public static final long POINT_7_ID_LONG = Long.parseLong(POINT_7_ID);
    public static final long POINT_8_ID_LONG = Long.parseLong(POINT_8_ID);

    public static final String POINT_1_LOCATION = "37.331417,-122.0304871";
    public static final String POINT_2_LOCATION = "37.333364,-122.0200268";
    public static final String POINT_3_LOCATION = "37.335310,-122.0095660";
    public static final String POINT_4_LOCATION = "37.337310,-121.0905660";
    public static final String POINT_5_LOCATION = "37.339310,-121.0895660";
    public static final String POINT_6_LOCATION = "37.341310,-121.0705660";
    public static final String POINT_7_LOCATION = "37.343310,-121.0695660";
    public static final String POINT_8_LOCATION = "37.345310,-121.0595660";

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = { @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION) }) })
    private Atlas oneWaySubAtlas1From1To3;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }) })
    private Atlas oneWaySubAtlas2From1To3;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = { @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION),
                    @Loc(value = POINT_3_LOCATION) }, tags = { "first=one", "second=two" }) })
    private Atlas oneWaySubAtlas1From1To3WithTags;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }, tags = { "first=one", "second=two" }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }, tags = { "first=one", "second=two",
                                    "third=three" }) })
    private Atlas oneWaySubAtlas2From1To3WithTags;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "223456789120000", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_1_LOCATION) }),
                    @Edge(id = "323456789120000", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_5_LOCATION) }) })
    private Atlas oneWaySubAtlas1From1To5WithOuterConnections;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "223456789120000", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_1_LOCATION) }),
                    @Edge(id = "323456789120000", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_5_LOCATION) }) })
    private Atlas oneWaySubAtlas2From1To5WithOuterConnections;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = { @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION) }) })
    private Atlas oneWaySubAtlas1From1To3WithOneWayInnerConnection;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "223456789120000", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }) })
    private Atlas oneWaySubAtlas2From1To4WithOneWayInnerConnection;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "223456789120000", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "-223456789120000", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }) })
    private Atlas oneWaySubAtlas2From1To4WithTwoWayInnerConnection;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = { @Edge(id = "123456789120000", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION) }) })
    private Atlas oneWaySubAtlas1From1To3WithInconsistentIds;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }) })
    private Atlas oneWaySubAtlas2From1To3WithInconsistentIds;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)),
                    @Node(id = POINT_7_ID, coordinates = @Loc(value = POINT_7_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120000", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "223456789120001", coordinates = { @Loc(value = POINT_5_LOCATION),
                            @Loc(value = POINT_6_LOCATION), @Loc(value = POINT_7_LOCATION) }) })
    private Atlas oneWaySubAtlas1From1To3WithInconsistentRoads;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)),
                    @Node(id = POINT_6_ID, coordinates = @Loc(value = POINT_6_LOCATION)),
                    @Node(id = POINT_7_ID, coordinates = @Loc(value = POINT_7_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "223456789120001", coordinates = { @Loc(value = POINT_5_LOCATION),
                            @Loc(value = POINT_6_LOCATION) }),
                    @Edge(id = "223456789120002", coordinates = { @Loc(value = POINT_6_LOCATION),
                            @Loc(value = POINT_7_LOCATION) }) })
    private Atlas oneWaySubAtlas2From1To3WithInconsistentRoads;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)),
                    @Node(id = POINT_7_ID, coordinates = @Loc(value = POINT_7_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120000", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "223456789120001", coordinates = { @Loc(value = POINT_5_LOCATION),
                            @Loc(value = POINT_6_LOCATION), @Loc(value = POINT_7_LOCATION) }), },
            // relations
            relations = { @Relation(id = "887654321", members = {
                    @Member(id = "123456789120000", type = "edge", role = "") }) })
    private Atlas oneWaySubAtlas1WithInconsistentRoadsAndARelation;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)),
                    @Node(id = POINT_6_ID, coordinates = @Loc(value = POINT_6_LOCATION)),
                    @Node(id = POINT_7_ID, coordinates = @Loc(value = POINT_7_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "223456789120001", coordinates = { @Loc(value = POINT_5_LOCATION),
                            @Loc(value = POINT_6_LOCATION) }),
                    @Edge(id = "223456789120002", coordinates = { @Loc(value = POINT_6_LOCATION),
                            @Loc(value = POINT_7_LOCATION) }) },
            // relations
            relations = { @Relation(id = "987654321", members = {
                    @Member(id = "123456789120001", type = "edge", role = "some role"),
                    @Member(id = "123456789120002", type = "edge", role = "some role") }) })
    private Atlas oneWaySubAtlas2WithInconsistentRoadsAndARelation;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = { @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION) }) },
            // relations
            relations = { @Relation(id = "887654321", members = {
                    @Member(id = "123456789120001", type = "edge", role = "") }) })
    private Atlas oneWaySubAtlas1From1To3WithRelations;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }) },
            // relations
            relations = { @Relation(id = "987654321", members = {
                    @Member(id = "123456789120001", type = "edge", role = "some role"),
                    @Member(id = "123456789120002", type = "edge", role = "some role") }) })
    private Atlas oneWaySubAtlas2From1To3WithRelations;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }) })
    private Atlas oneWaySubAtlas1From1To4;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION), @Loc(value = POINT_4_LOCATION) }) })
    private Atlas oneWaySubAtlas2From1To4;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "-123456789120001", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_1_LOCATION) }) })
    private Atlas twoWaySubAtlas1From1To3;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "-123456789120001", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_1_LOCATION) }),
                    @Edge(id = "-123456789120002", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }) })
    private Atlas twoWaySubAtlas2From1To3;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)),
                    @Node(id = POINT_6_ID, coordinates = @Loc(value = POINT_6_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "-123456789120001", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_1_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION), @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "-123456789120002", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_3_LOCATION), @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "223456789120000", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "323456789120000", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_6_LOCATION) }) })
    private Atlas aSubAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "-123456789120001", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_1_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "-123456789120002", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "223456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "223456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }) })
    private Atlas anotherSubAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_7_ID, coordinates = @Loc(value = POINT_7_LOCATION)),
                    @Node(id = POINT_8_ID, coordinates = @Loc(value = POINT_8_LOCATION)) },
            // edges
            edges = { @Edge(id = "423456789120000", coordinates = { @Loc(value = POINT_7_LOCATION),
                    @Loc(value = POINT_8_LOCATION) }) })
    private Atlas aThirdSubAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "123456789120004", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "123456789120005", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_5_LOCATION) }) })
    private Atlas threeWaySubAtlas1;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "123456789120004", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "123456789120005", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_5_LOCATION) }) })
    private Atlas threeWaySubAtlas2;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "123456789120004", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "123456789120005", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "123456789120006", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_5_LOCATION) }) })
    private Atlas threeWayWithLoopSubAtlas1;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "123456789120004", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "123456789120005", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_3_LOCATION) }),
                    @Edge(id = "123456789120006", coordinates = { @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_5_LOCATION) }) })
    private Atlas threeWayWithLoopSubAtlas2;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_2_LOCATION),
                            @Loc(value = POINT_3_LOCATION), @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "123456789120003", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_5_LOCATION) }) })
    private Atlas oneWayWithDuplicateNodeSubAtlas1;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_3_LOCATION), @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "123456789120004", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_5_LOCATION) }) })
    private Atlas oneWayWithDuplicateNodeSubAtlas2;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                            @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION),
                            @Loc(value = POINT_4_LOCATION) }),
                    @Edge(id = "123456789120003", coordinates = { @Loc(value = POINT_4_LOCATION),
                            @Loc(value = POINT_5_LOCATION) }) })
    private Atlas oneWaySubAtlas1From1To4WithAnInnerLocation;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)),
                    @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)),
                    @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)) },
            // edges
            edges = { @Edge(id = "123456789120000", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION), @Loc(value = POINT_3_LOCATION),
                    @Loc(value = POINT_4_LOCATION), @Loc(value = POINT_5_LOCATION) }) })
    private Atlas oneWaySubAtlas2From1To4WithAnInnerLocation;

    public Atlas anotherSubAtlas()
    {
        return this.anotherSubAtlas;
    }

    public Atlas aSubAtlas()
    {
        return this.aSubAtlas;
    }

    public Atlas aThirdSubAtlas()
    {
        return this.aThirdSubAtlas;
    }

    public Atlas oneWaySubAtlas1From1To3()
    {
        return this.oneWaySubAtlas1From1To3;
    }

    public Atlas oneWaySubAtlas1From1To3WithInconsistentIds()
    {
        return this.oneWaySubAtlas1From1To3WithInconsistentIds;
    }

    public Atlas oneWaySubAtlas1From1To3WithInconsistentRoads()
    {
        return this.oneWaySubAtlas1From1To3WithInconsistentRoads;
    }

    public Atlas oneWaySubAtlas1From1To3WithOneWayInnerConnection()
    {
        return this.oneWaySubAtlas1From1To3WithOneWayInnerConnection;
    }

    public Atlas oneWaySubAtlas1From1To3WithRelations()
    {
        return this.oneWaySubAtlas1From1To3WithRelations;
    }

    public Atlas oneWaySubAtlas1From1To3WithTags()
    {
        return this.oneWaySubAtlas1From1To3WithTags;
    }

    public Atlas oneWaySubAtlas1From1To4()
    {
        return this.oneWaySubAtlas1From1To4;
    }

    public Atlas oneWaySubAtlas1From1To4WithInnerLocations()
    {
        return this.oneWaySubAtlas1From1To4WithAnInnerLocation;
    }

    public Atlas oneWaySubAtlas1From1To5WithOuterConnections()
    {
        return this.oneWaySubAtlas1From1To5WithOuterConnections;
    }

    public Atlas oneWaySubAtlas1WithInconsistentRoadsAndARelation()
    {
        return this.oneWaySubAtlas1WithInconsistentRoadsAndARelation;
    }

    public Atlas oneWaySubAtlas2From1To3()
    {
        return this.oneWaySubAtlas2From1To3;
    }

    public Atlas oneWaySubAtlas2From1To3WithInconsistentIds()
    {
        return this.oneWaySubAtlas2From1To3WithInconsistentIds;
    }

    public Atlas oneWaySubAtlas2From1To3WithInconsistentRoads()
    {
        return this.oneWaySubAtlas2From1To3WithInconsistentRoads;
    }

    public Atlas oneWaySubAtlas2From1To3WithRelations()
    {
        return this.oneWaySubAtlas2From1To3WithRelations;
    }

    public Atlas oneWaySubAtlas2From1To3WithTags()
    {
        return this.oneWaySubAtlas2From1To3WithTags;
    }

    public Atlas oneWaySubAtlas2From1To4()
    {
        return this.oneWaySubAtlas2From1To4;
    }

    public Atlas oneWaySubAtlas2From1To4WithInnerLocations()
    {
        return this.oneWaySubAtlas2From1To4WithAnInnerLocation;
    }

    public Atlas oneWaySubAtlas2From1To4WithOneWayInnerConnection()
    {
        return this.oneWaySubAtlas2From1To4WithOneWayInnerConnection;
    }

    public Atlas oneWaySubAtlas2From1To4WithTwoWayInnerConnection()
    {
        return this.oneWaySubAtlas2From1To4WithTwoWayInnerConnection;
    }

    public Atlas oneWaySubAtlas2From1To5WithOuterConnections()
    {
        return this.oneWaySubAtlas2From1To5WithOuterConnections;
    }

    public Atlas oneWaySubAtlas2WithInconsistentRoadsAndARelation()
    {
        return this.oneWaySubAtlas2WithInconsistentRoadsAndARelation;
    }

    public Atlas oneWayWithDuplicateNodeSubAtlas1()
    {
        return this.oneWayWithDuplicateNodeSubAtlas1;
    }

    public Atlas oneWayWithDuplicateNodeSubAtlas2()
    {
        return this.oneWayWithDuplicateNodeSubAtlas2;
    }

    public Atlas threeWaySubAtlas1()
    {
        return this.threeWaySubAtlas1;
    }

    public Atlas threeWaySubAtlas2()
    {
        return this.threeWaySubAtlas2;
    }

    public Atlas threeWayWithLoopSubAtlas1()
    {
        return this.threeWayWithLoopSubAtlas1;
    }

    public Atlas threeWayWithLoopSubAtlas2()
    {
        return this.threeWayWithLoopSubAtlas2;
    }

    public Atlas twoWaySubAtlas1From1To3()
    {
        return this.twoWaySubAtlas1From1To3;
    }

    public Atlas twoWaySubAtlas2From1To3()
    {
        return this.twoWaySubAtlas2From1To3;
    }
}
