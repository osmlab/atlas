package org.openstreetmap.atlas.geography.atlas.items.complex.relation;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Test data for {@link ComplexAOIRelationFinderTest}
 * @author sayas01
 */
public class ComplexAOIRelationFinderTestRule extends CoreTestRule
{
    private static final String TEST_VALIDRELATION_0 = "1.43958970913, 103.91306306772";
    private static final String TEST_VALIDRELATION_1 = "1.42773565932, 103.90488839864";
    private static final String TEST_VALIDRELATION_10 = "1.43419031086, 103.89654055711";
    private static final String TEST_VALIDRELATION_11 = "1.41407426416, 103.89600156794";
    private static final String TEST_VALIDRELATION_12 = "1.40850639889, 103.91621366183";
    private static final String TEST_VALIDRELATION_13 = "1.39835848123, 103.92322052105";
    private static final String TEST_VALIDRELATION_14 = "1.41146994174, 103.94549874009";
    private static final String TEST_VALIDRELATION_15 = "1.41155974601, 103.96373454036";
    private static final String TEST_VALIDRELATION_16 = "1.43419031086, 103.97038207346";
    private static final String TEST_VALIDRELATION_17 = "1.45116308784, 103.96454302412";
    private static final String TEST_VALIDRELATION_18 = "1.45233052286, 103.93786306018";
    private static final String TEST_VALIDRELATION_19 = "1.45367756252, 103.91845945004";
    private static final String TEST_VALIDRELATION_2 = "1.41193016458, 103.91459020371";
    private static final String TEST_VALIDRELATION_3 = "1.41624076477, 103.94180915682";
    private static final String TEST_VALIDRELATION_4 = "1.42638860433, 103.94621090171";
    private static final String TEST_VALIDRELATION_5 = "1.43141760561, 103.96399754433";
    private static final String TEST_VALIDRELATION_6 = "1.44273281808, 103.95447540232";
    private static final String TEST_VALIDRELATION_7 = "1.43842226756, 103.93857522179";
    private static final String TEST_VALIDRELATION_8 = "1.44839040324, 103.9256394817";
    private static final String TEST_VALIDRELATION_9 = "1.4507140742, 103.9069610144";

    @TestAtlas(points = { @Point(id = "39008", coordinates = @Loc(value = TEST_VALIDRELATION_9)),
            @Point(id = "39009", coordinates = @Loc(value = TEST_VALIDRELATION_10)),
            @Point(id = "39011", coordinates = @Loc(value = TEST_VALIDRELATION_11)),
            @Point(id = "39013", coordinates = @Loc(value = TEST_VALIDRELATION_12)),
            @Point(id = "39015", coordinates = @Loc(value = TEST_VALIDRELATION_13)),
            @Point(id = "38985", coordinates = @Loc(value = TEST_VALIDRELATION_0)),
            @Point(id = "39019", coordinates = @Loc(value = TEST_VALIDRELATION_15)),
            @Point(id = "38988", coordinates = @Loc(value = TEST_VALIDRELATION_1)),
            @Point(id = "39021", coordinates = @Loc(value = TEST_VALIDRELATION_16)),
            @Point(id = "39023", coordinates = @Loc(value = TEST_VALIDRELATION_17)),
            @Point(id = "38992", coordinates = @Loc(value = TEST_VALIDRELATION_2)),
            @Point(id = "39025", coordinates = @Loc(value = TEST_VALIDRELATION_18)),
            @Point(id = "38994", coordinates = @Loc(value = TEST_VALIDRELATION_3)),
            @Point(id = "39027", coordinates = @Loc(value = TEST_VALIDRELATION_19)),
            @Point(id = "38996", coordinates = @Loc(value = TEST_VALIDRELATION_4)),
            @Point(id = "38998", coordinates = @Loc(value = TEST_VALIDRELATION_5)),
            @Point(id = "39017", coordinates = @Loc(value = TEST_VALIDRELATION_14)),
            @Point(id = "39000", coordinates = @Loc(value = TEST_VALIDRELATION_6)),
            @Point(id = "39002", coordinates = @Loc(value = TEST_VALIDRELATION_7)),
            @Point(id = "39004", coordinates = @Loc(value = TEST_VALIDRELATION_8)) },
            areas = {
            @Area(id = "39010", coordinates = { @Loc(value = TEST_VALIDRELATION_9),
                    @Loc(value = TEST_VALIDRELATION_10),
                    @Loc(value = TEST_VALIDRELATION_11),
                    @Loc(value = TEST_VALIDRELATION_12),
                    @Loc(value = TEST_VALIDRELATION_13),
                    @Loc(value = TEST_VALIDRELATION_14),
                    @Loc(value = TEST_VALIDRELATION_15),
                    @Loc(value = TEST_VALIDRELATION_16),
                    @Loc(value = TEST_VALIDRELATION_17),
                    @Loc(value = TEST_VALIDRELATION_18),
                    @Loc(value = TEST_VALIDRELATION_19),
                    @Loc(value = TEST_VALIDRELATION_9) }),
            @Area(id = "38989", coordinates = { @Loc(value = TEST_VALIDRELATION_0),
                    @Loc(value = TEST_VALIDRELATION_1), @Loc(value = TEST_VALIDRELATION_2),
                    @Loc(value = TEST_VALIDRELATION_3), @Loc(value = TEST_VALIDRELATION_4),
                    @Loc(value = TEST_VALIDRELATION_5), @Loc(value = TEST_VALIDRELATION_6),
                    @Loc(value = TEST_VALIDRELATION_7), @Loc(value = TEST_VALIDRELATION_8),
                    @Loc(value = TEST_VALIDRELATION_0) }),
            @Area(id = "38987", coordinates = { @Loc(value = TEST_VALIDRELATION_18),
                    @Loc(value = TEST_VALIDRELATION_17), @Loc(value = TEST_VALIDRELATION_7),
                    @Loc(value = TEST_VALIDRELATION_6) }) },
            relations = {
            @Relation(id = "39190", members = {
                    @Member(id = "39010", type = "area", role = "outer"),
                    @Member(id = "38989", type = "area", role = "inner") }, tags = {
                    "type=multipolygon", "amenity=FESTIVAL_GROUNDS"} ),
            @Relation(id = "39990", members = {
            @Member(id = "38987", type = "area", role = "outer")}, tags = {
            "type=boundary", "landuse=FOREST"}) })
    private Atlas multipolygonAOIRelationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "10020", coordinates = @Loc(value = TEST_VALIDRELATION_14)),
                    @Node(id = "21001", coordinates = @Loc(value = TEST_VALIDRELATION_15)),
                    @Node(id = "31233", coordinates = @Loc(value = TEST_VALIDRELATION_16)) },
            // edges
            edges = {
                    @Edge(id = "12333", coordinates = { @Loc(value = TEST_VALIDRELATION_14),
                            @Loc(value = TEST_VALIDRELATION_15) }, tags = { "highway=road" }),
                    @Edge(id = "23332", coordinates = { @Loc(value = TEST_VALIDRELATION_15),
                            @Loc(value = TEST_VALIDRELATION_16) }, tags = { "highway=road" }),
                    @Edge(id = "31223", coordinates = { @Loc(value = TEST_VALIDRELATION_16),
                            @Loc(value = TEST_VALIDRELATION_14) }, tags = { "highway=road" }) },
            // relations
            relations = { @Relation(id = "89765", members = {
                    @Member(id = "12333", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_FROM),
                    @Member(id = "21001", type = "node", role = RelationTypeTag.RESTRICTION_ROLE_VIA),
                    @Member(id = "31223", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_TO) }, tags = {
                    "restriction=no_u_turn","landuse=VILLAGE" }) })
    private Atlas nonMultipolygonAOIRelationAtlas;


    @TestAtlas(
            areas = { @Area(id = "38987", coordinates = { @Loc(value = TEST_VALIDRELATION_18),
                    @Loc(value = TEST_VALIDRELATION_17), @Loc(value = TEST_VALIDRELATION_7),
                    @Loc(value = TEST_VALIDRELATION_6) }, tags="natural=WOOD") })
    private Atlas nonRelationAOIAtlas;

    public Atlas getMultipolygonAOIRelationAtlas()
    {
        return this.multipolygonAOIRelationAtlas;
    }

    public Atlas getNonMultipolygonAOIRelationAtlas()
    {
        return this.nonMultipolygonAOIRelationAtlas;
    }

    public Atlas getNonRelationAOIAtlas()
    {
        return this.nonRelationAOIAtlas;
    }
}
