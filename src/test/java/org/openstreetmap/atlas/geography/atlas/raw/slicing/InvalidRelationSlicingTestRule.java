package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * {@link InvalidMultipolygonSlicingTest} test data.
 *
 * @author mgostintsev
 */
public class InvalidRelationSlicingTestRule extends CoreTestRule
{
    private static final String LOCATION_1 = "6.94567556723,-8.33813693927";
    private static final String LOCATION_2 = "6.92331354425,-8.33803726312";
    private static final String LOCATION_3 = "6.92321459489,-8.29956226825";
    private static final String LOCATION_4 = "6.94854495346,-8.30125676284";
    private static final String LOCATION_5 = "6.9456756,-8.3321111";
    private static final String LOCATION_6 = "6.9232146,-8.3321111";
    private static final String LOCATION_7 = "6.94567556723,-8.33013693927";
    private static final String LOCATION_8 = "6.92331354425,-8.33013693927";
    private static final String LOCATION_9 = "6.9452756,-8.3370369";
    private static final String LOCATION_10 = "6.9452756,-8.3330369";
    private static final String LOCATION_11 = "6.9402756,-8.3330369";
    private static final String LOCATION_12 = "6.9402756,-8.3370369";
    private static final String LOCATION_13 = "6.9233135,-8.3390373";
    private static final String LOCATION_14 = "6.9213135,-8.3390373";
    private static final String LOCATION_15 = "6.9213135,-8.3380373";
    private static final String LOCATION_16 = "6.9456756,-8.3121111";
    private static final String LOCATION_17 = "6.9232146,-8.3121111";
    private static final String LOCATION_18 = "6.9452756,-8.3230369";
    private static final String LOCATION_19 = "6.9402756,-8.3230369";

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_2)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_3)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_4)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_2), @Loc(value = LOCATION_3), @Loc(value = LOCATION_4),
                    @Loc(value = LOCATION_1) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "inner", type = "line") })

            })
    private Atlas innerWithoutOuterAcrossBoundary;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_5)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_6)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_2)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_5), @Loc(value = LOCATION_6), @Loc(value = LOCATION_2),
                    @Loc(value = LOCATION_1) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "inner", type = "line") })

            })
    private Atlas innerWithoutOuterInOneCountry;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_5)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_6)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_2)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_5), @Loc(value = LOCATION_6),
                    @Loc(value = LOCATION_2) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line") })

            })
    private Atlas openMultiPolygonInOneCountry;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_2)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_3)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_4)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_2), @Loc(value = LOCATION_3),
                    @Loc(value = LOCATION_4) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line") })

            })
    private Atlas openMultiPolygonAcrossBoundary;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_5)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_6)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_2)),
                    @Point(id = "108759000000", coordinates = @Loc(value = LOCATION_4)),
                    @Point(id = "108751000000", coordinates = @Loc(value = LOCATION_3)),
                    @Point(id = "108753000000", coordinates = @Loc(value = LOCATION_7)),
                    @Point(id = "108755000000", coordinates = @Loc(value = LOCATION_8)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_5), @Loc(value = LOCATION_6), @Loc(value = LOCATION_2),
                    @Loc(value = LOCATION_1) }, tags = { "building=yes" }),
                    @Line(id = "108769000000", coordinates = { @Loc(value = LOCATION_7),
                            @Loc(value = LOCATION_8), @Loc(value = LOCATION_3),
                            @Loc(value = LOCATION_4) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line"),
                            @Member(id = "108769000000", role = "outer", type = "line") })

            })
    private Atlas closedAndNonClosedMembersRelation;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_5)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_6)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_2)),
                    @Point(id = "108751000000", coordinates = @Loc(value = LOCATION_9)),
                    @Point(id = "108753000000", coordinates = @Loc(value = LOCATION_10)),
                    @Point(id = "108755000000", coordinates = @Loc(value = LOCATION_11)),
                    @Point(id = "108757000000", coordinates = @Loc(value = LOCATION_12)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_5), @Loc(value = LOCATION_6), @Loc(value = LOCATION_2),
                    @Loc(value = LOCATION_1) }, tags = { "building=yes" }),
                    @Line(id = "108769000000", coordinates = { @Loc(value = LOCATION_9),
                            @Loc(value = LOCATION_10), @Loc(value = LOCATION_11),
                            @Loc(value = LOCATION_12),
                            @Loc(value = LOCATION_9) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "inner", type = "line"),
                            @Member(id = "108769000000", role = "outer", type = "line") })

            })
    private Atlas innerOutsideOuterInOneCountry;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_5)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_6)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_2)),
                    @Point(id = "108751000000", coordinates = @Loc(value = LOCATION_9)),
                    @Point(id = "108753000000", coordinates = @Loc(value = LOCATION_18)),
                    @Point(id = "108755000000", coordinates = @Loc(value = LOCATION_19)),
                    @Point(id = "108757000000", coordinates = @Loc(value = LOCATION_12)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_5), @Loc(value = LOCATION_6), @Loc(value = LOCATION_2),
                    @Loc(value = LOCATION_1) }, tags = { "building=yes" }),
                    @Line(id = "108769000000", coordinates = { @Loc(value = LOCATION_9),
                            @Loc(value = LOCATION_18), @Loc(value = LOCATION_19),
                            @Loc(value = LOCATION_12),
                            @Loc(value = LOCATION_9) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "inner", type = "line"),
                            @Member(id = "108769000000", role = "outer", type = "line") })

            })
    private Atlas intersectingInnerAndOuterMemberRelation;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_5)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_6)),
                    @Point(id = "108751000000", coordinates = @Loc(value = LOCATION_9)),
                    @Point(id = "108753000000", coordinates = @Loc(value = LOCATION_10)),
                    @Point(id = "108755000000", coordinates = @Loc(value = LOCATION_11)),
                    @Point(id = "108757000000", coordinates = @Loc(value = LOCATION_12)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_13)),
                    @Point(id = "108759000000", coordinates = @Loc(value = LOCATION_14)),
                    @Point(id = "108761000000", coordinates = @Loc(value = LOCATION_15)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_5), @Loc(value = LOCATION_6), @Loc(value = LOCATION_13),
                    @Loc(value = LOCATION_14), @Loc(value = LOCATION_15),
                    @Loc(value = LOCATION_1) }, tags = { "building=yes" }),
                    @Line(id = "108769000000", coordinates = { @Loc(value = LOCATION_9),
                            @Loc(value = LOCATION_10), @Loc(value = LOCATION_11),
                            @Loc(value = LOCATION_12),
                            @Loc(value = LOCATION_9) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line"),
                            @Member(id = "108769000000", role = "inner", type = "line") })

            })
    private Atlas selfIntersectingOuterMemberRelation;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_16)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_17)),
                    @Point(id = "108751000000", coordinates = @Loc(value = LOCATION_9)),
                    @Point(id = "108753000000", coordinates = @Loc(value = LOCATION_10)),
                    @Point(id = "108755000000", coordinates = @Loc(value = LOCATION_11)),
                    @Point(id = "108757000000", coordinates = @Loc(value = LOCATION_12)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_13)),
                    @Point(id = "108759000000", coordinates = @Loc(value = LOCATION_14)),
                    @Point(id = "108761000000", coordinates = @Loc(value = LOCATION_15)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_16), @Loc(value = LOCATION_17), @Loc(value = LOCATION_13),
                    @Loc(value = LOCATION_14), @Loc(value = LOCATION_15),
                    @Loc(value = LOCATION_1) }, tags = { "building=yes" }),
                    @Line(id = "108769000000", coordinates = { @Loc(value = LOCATION_9),
                            @Loc(value = LOCATION_10), @Loc(value = LOCATION_11),
                            @Loc(value = LOCATION_12),
                            @Loc(value = LOCATION_9) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line"),
                            @Member(id = "108769000000", role = "inner", type = "line") })

            })
    private Atlas selfIntersectingOuterMemberRelationAcrossBoundary;

    public Atlas getInnerOutsideOuterRelationAtlas()
    {
        return this.innerOutsideOuterInOneCountry;
    }

    public Atlas getInnerWithoutOuterAcrossBoundaryAtlas()
    {
        return this.innerWithoutOuterAcrossBoundary;
    }

    public Atlas getInnerWithoutOuterInOneCountryAtlas()
    {
        return this.innerWithoutOuterInOneCountry;
    }

    public Atlas getIntersectingInnerAndOuterMembersAtlas()
    {
        return this.intersectingInnerAndOuterMemberRelation;
    }

    public Atlas getOpenMultiPolygonAcrossBoundaryAtlas()
    {
        return this.openMultiPolygonAcrossBoundary;
    }

    public Atlas getOpenMultiPolygonInOneCountryAtlas()
    {
        return this.openMultiPolygonInOneCountry;
    }

    public Atlas getRelationWithOneClosedAndOneOpenMemberAtlas()
    {
        return this.closedAndNonClosedMembersRelation;
    }

    public Atlas getSelfIntersectingOuterMemberRelationAcrossBoundaryAtlas()
    {
        return this.selfIntersectingOuterMemberRelationAcrossBoundary;
    }

    public Atlas getSelfIntersectingOuterMemberRelationAtlas()
    {
        return this.selfIntersectingOuterMemberRelation;
    }
}
