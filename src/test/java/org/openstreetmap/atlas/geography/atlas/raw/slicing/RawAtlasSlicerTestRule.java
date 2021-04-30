package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * {@link RawAtlasSlicerTest} test data.
 *
 * @author mgostintsev
 */
public class RawAtlasSlicerTestRule extends CoreTestRule
{
    // Single crossing across boundary
    private static final String LIBERIA_END = "7.2,-8.4";
    private static final String ON_LIBERIA_AND_IVORY_COAST_BORDER = "7.2, -8.3174953";
    private static final String IVORY_COAST_END = "7.2,-8.2";

    // Zig-Zagging Line across boundary
    private static final String IVORY_COAST_1 = "6.983833,-8.28705";
    private static final String LIBERIA_1 = "6.98573,-8.29511";
    private static final String IVORY_COAST_2 = "6.98019,-8.29400";
    private static final String IVORY_COAST_3 = "6.98237,-8.29694";
    private static final String LIBERIA_2 = "6.98365,-8.30103";

    // Vertices for Line outside of boundaries
    private static final String OUTSIDE_ALL_COUNTRIES_1 = "14.2,-8.4";
    private static final String OUTSIDE_ALL_COUNTRIES_2 = "14.2,-8.2";

    // Area spanning Ivory Coast and Liberia
    private static final String AREA_CIV_SIDE_1 = "6.92911,-8.30680";
    private static final String AREA_CIV_SIDE_2 = "6.91758,-8.30635";
    private static final String AREA_LBR_SIDE_3 = "6.91758,-8.32742";
    private static final String AREA_LBR_SIDE_4 = "6.92843,-8.32731";

    // Area fully inside Ivory Coast
    private static final String CIV_TOP_RIGHT = "7.8551653,-8.4276384";
    private static final String CIV_BOTTOM_RIGHT = "7.8550903,-8.4276536";
    private static final String CIV_BOTTOM_LEFT = "7.8551026,-8.4277151";
    private static final String CIV_TOP_LEFT = "7.8551775,-8.4276999";

    private static final String LOCATION_1 = "6.89143764171,-8.32869925339";

    private static final String LOCATION_2 = "6.8834659547,-8.32827100636";

    private static final String LOCATION_3 = "6.88378482475,-8.33892365125";

    private static final String LOCATION_4 = "6.892181659,-8.33721066313";

    private static final String LOCATION_5 = "6.89154392997,-8.32623683297";

    private static final String LOCATION_6 = "6.88957759339,-8.31547712632";

    private static final String LOCATION_7 = "6.89946233792,-8.31702952181";

    private static final String LOCATION_8 = "6.89707088638,-8.32591564769";

    private static final String LOCATION_9 = "6.89845261541,-8.33469471182";

    private static final String LOCATION_10 = "6.89366969006,-8.34154666432";

    private static final String LOCATION_11 = "6.89733660382,-8.34780977714";
    private static final String LOCATION_12 = "6.90440463282,-8.34261728189";
    private static final String LOCATION_13 = "6.94567556723,-8.33813693927";
    private static final String LOCATION_14 = "6.92331354425,-8.33803726312";
    private static final String LOCATION_15 = "6.92321459489,-8.29956226825";
    private static final String LOCATION_16 = "6.94854495346,-8.30125676284";
    private static final String LOCATION_17 = "6.93934306671,-8.32996349477";
    private static final String LOCATION_18 = "6.93093258278,-8.32896673324";
    private static final String LOCATION_19 = "6.93142732129,-8.31341725345";
    private static final String LOCATION_20 = "6.9411240911,-8.31441401497";
    private static final String LOCATION_21 = "6.86563875322,-8.3252389315";
    private static final String LOCATION_22 = "6.86322809383,-8.32322875353";

    private static final String LOCATION_23 = "6.8623840025,-8.32698252672";
    private static final String LOCATION_24 = "6.86676181589,-8.32504439815";
    private static final String LOCATION_25 = "6.86541700168,-8.32328639305";
    private static final String LOCATION_26 = "6.86367875216,-8.32594501551";
    private static final String LOCATION_27 = "6.86482327931,-8.32636290197";
    private static final String LOCATION_28 = "6.86431539572,-8.32451123267";
    private static final String LOCATION_29 = "6.86573174578,-8.32812090707";
    private static final String LOCATION_30 = "6.87133198688,-8.33750935353";

    private static final String LOCATION_31 = "6.88017051964,-8.33197681515";
    private static final String LOCATION_32 = "6.87441616889,-8.32877230817";
    private static final String LOCATION_33 = "6.8837848,-8.3389237";
    private static final String LOCATION_34 = "6.87142836787,-8.33437857895";
    private static final String LOCATION_35 = "6.87193436775,-8.33149050006";
    private static final String LOCATION_36 = "6.883466,-8.328271";
    private static final String LOCATION_37 = "6.87407883746,-8.34039743242";
    private static final String LOCATION_38 = "6.87822317845,-8.33993631058";
    private static final String LOCATION_39 = "6.876712, -8.331375";

    private static final String LOCATION_40 = "6.94567556723,-8.33813693927";
    private static final String LOCATION_41 = "6.92331354425,-8.33803726312";
    private static final String LOCATION_42 = "6.92321459489,-8.29956226825";
    private static final String LOCATION_43 = "6.94854495346,-8.30125676284";
    private static final String LOCATION_45 = "6.9456756,-8.3321111";
    private static final String LOCATION_46 = "6.9232146,-8.3321111";
    private static final String LOCATION_47 = "6.94567556723,-8.33013693927";
    private static final String LOCATION_48 = "6.92331354425,-8.33013693927";
    private static final String LOCATION_49 = "6.9452756,-8.3370369";
    private static final String LOCATION_50 = "6.9452756,-8.3330369";
    private static final String LOCATION_51 = "6.9402756,-8.3330369";
    private static final String LOCATION_52 = "6.9402756,-8.3370369";
    private static final String LOCATION_53 = "6.9233135,-8.3390373";
    private static final String LOCATION_54 = "6.9213135,-8.3390373";
    private static final String LOCATION_55 = "6.9213135,-8.3380373";
    private static final String LOCATION_56 = "6.9456756,-8.3121111";
    private static final String LOCATION_57 = "6.9232146,-8.3121111";
    private static final String LOCATION_58 = "6.9452756,-8.3230369";
    private static final String LOCATION_59 = "6.9402756,-8.3230369";

    private static final String LOCATION_60 = "6.9393431,-8.3299635";
    private static final String LOCATION_61 = "6.9393531,-8.3299635";
    private static final String LOCATION_62 = "6.9393531,-8.3299535";
    private static final String LOCATION_63 = "6.9393431,-8.3299535";

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_40)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_41)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_42)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_43)) },

            areas = { @Area(id = "108768000000", coordinates = { @Loc(value = LOCATION_40),
                    @Loc(value = LOCATION_41), @Loc(value = LOCATION_42), @Loc(value = LOCATION_43),
                    @Loc(value = LOCATION_40) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "inner", type = "area") })

            })
    private Atlas innerWithoutOuterAcrossBoundary;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_40)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_45)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_46)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_41)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_40),
                    @Loc(value = LOCATION_45), @Loc(value = LOCATION_46), @Loc(value = LOCATION_41),
                    @Loc(value = LOCATION_40) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "inner", type = "line") })

            })
    private Atlas innerWithoutOuterInOneCountry;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_40)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_45)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_46)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_41)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_40),
                    @Loc(value = LOCATION_45), @Loc(value = LOCATION_46),
                    @Loc(value = LOCATION_41) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line") })

            })
    private Atlas openMultiPolygonInOneCountry;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_40)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_41)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_42)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_43)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_40),
                    @Loc(value = LOCATION_41), @Loc(value = LOCATION_42),
                    @Loc(value = LOCATION_43) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line") })

            })
    private Atlas openMultiPolygonAcrossBoundary;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_40)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_45)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_46)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_41)),
                    @Point(id = "108759000000", coordinates = @Loc(value = LOCATION_43)),
                    @Point(id = "108751000000", coordinates = @Loc(value = LOCATION_42)),
                    @Point(id = "108753000000", coordinates = @Loc(value = LOCATION_47)),
                    @Point(id = "108755000000", coordinates = @Loc(value = LOCATION_48)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_40),
                    @Loc(value = LOCATION_45), @Loc(value = LOCATION_46), @Loc(value = LOCATION_41),
                    @Loc(value = LOCATION_40) }, tags = { "building=yes" }),
                    @Line(id = "108769000000", coordinates = { @Loc(value = LOCATION_47),
                            @Loc(value = LOCATION_48), @Loc(value = LOCATION_42),
                            @Loc(value = LOCATION_43) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line"),
                            @Member(id = "108769000000", role = "outer", type = "line") })

            })
    private Atlas closedAndNonClosedMembersRelation;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_40)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_45)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_46)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_41)),
                    @Point(id = "108751000000", coordinates = @Loc(value = LOCATION_49)),
                    @Point(id = "108753000000", coordinates = @Loc(value = LOCATION_50)),
                    @Point(id = "108755000000", coordinates = @Loc(value = LOCATION_51)),
                    @Point(id = "108757000000", coordinates = @Loc(value = LOCATION_52)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_40),
                    @Loc(value = LOCATION_45), @Loc(value = LOCATION_46), @Loc(value = LOCATION_41),
                    @Loc(value = LOCATION_40) }, tags = { "building=yes" }),
                    @Line(id = "108769000000", coordinates = { @Loc(value = LOCATION_49),
                            @Loc(value = LOCATION_50), @Loc(value = LOCATION_51),
                            @Loc(value = LOCATION_52),
                            @Loc(value = LOCATION_49) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "inner", type = "line"),
                            @Member(id = "108769000000", role = "outer", type = "line") })

            })
    private Atlas innerOutsideOuterInOneCountry;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_40)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_45)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_46)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_41)),
                    @Point(id = "108751000000", coordinates = @Loc(value = LOCATION_49)),
                    @Point(id = "108753000000", coordinates = @Loc(value = LOCATION_58)),
                    @Point(id = "108755000000", coordinates = @Loc(value = LOCATION_59)),
                    @Point(id = "108757000000", coordinates = @Loc(value = LOCATION_52)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_40),
                    @Loc(value = LOCATION_45), @Loc(value = LOCATION_46), @Loc(value = LOCATION_41),
                    @Loc(value = LOCATION_40) }, tags = { "building=yes" }),
                    @Line(id = "108769000000", coordinates = { @Loc(value = LOCATION_49),
                            @Loc(value = LOCATION_58), @Loc(value = LOCATION_59),
                            @Loc(value = LOCATION_52),
                            @Loc(value = LOCATION_49) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "inner", type = "line"),
                            @Member(id = "108769000000", role = "outer", type = "line") })

            })
    private Atlas intersectingInnerAndOuterMemberRelation;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_40)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_45)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_46)),
                    @Point(id = "108751000000", coordinates = @Loc(value = LOCATION_49)),
                    @Point(id = "108753000000", coordinates = @Loc(value = LOCATION_50)),
                    @Point(id = "108755000000", coordinates = @Loc(value = LOCATION_51)),
                    @Point(id = "108757000000", coordinates = @Loc(value = LOCATION_52)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_53)),
                    @Point(id = "108759000000", coordinates = @Loc(value = LOCATION_54)),
                    @Point(id = "108761000000", coordinates = @Loc(value = LOCATION_55)) },

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_40),
                    @Loc(value = LOCATION_45), @Loc(value = LOCATION_46), @Loc(value = LOCATION_53),
                    @Loc(value = LOCATION_54), @Loc(value = LOCATION_55),
                    @Loc(value = LOCATION_40) }, tags = { "building=yes" }),
                    @Line(id = "108769000000", coordinates = { @Loc(value = LOCATION_49),
                            @Loc(value = LOCATION_50), @Loc(value = LOCATION_51),
                            @Loc(value = LOCATION_52),
                            @Loc(value = LOCATION_49) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(id = "1", tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line"),
                            @Member(id = "108769000000", role = "inner", type = "line") })

            })
    private Atlas selfIntersectingOuterMemberRelation;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_40)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_56)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_57)),
                    @Point(id = "108751000000", coordinates = @Loc(value = LOCATION_49)),
                    @Point(id = "108753000000", coordinates = @Loc(value = LOCATION_50)),
                    @Point(id = "108755000000", coordinates = @Loc(value = LOCATION_51)),
                    @Point(id = "108757000000", coordinates = @Loc(value = LOCATION_52)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_53)),
                    @Point(id = "108759000000", coordinates = @Loc(value = LOCATION_54)),
                    @Point(id = "108761000000", coordinates = @Loc(value = LOCATION_55)) },

            areas = {
                    @Area(id = "108768000000", coordinates = { @Loc(value = LOCATION_40),
                            @Loc(value = LOCATION_56), @Loc(value = LOCATION_57),
                            @Loc(value = LOCATION_53), @Loc(value = LOCATION_54),
                            @Loc(value = LOCATION_55), @Loc(value = LOCATION_40) }),
                    @Area(id = "108769000000", coordinates = { @Loc(value = LOCATION_49),
                            @Loc(value = LOCATION_50), @Loc(value = LOCATION_51),
                            @Loc(value = LOCATION_52), @Loc(value = LOCATION_49) }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "area"),
                            @Member(id = "108769000000", role = "inner", type = "area") })

            })
    private Atlas selfIntersectingOuterMemberRelationAcrossBoundary;

    @TestAtlas(

            points = { @Point(id = "1", coordinates = @Loc(value = CIV_TOP_RIGHT)),
                    @Point(id = "2", coordinates = @Loc(value = CIV_BOTTOM_RIGHT)),
                    @Point(id = "3", coordinates = @Loc(value = CIV_BOTTOM_LEFT)),
                    @Point(id = "4", coordinates = @Loc(value = CIV_TOP_LEFT)) },

            lines = { @Line(id = "1", coordinates = { @Loc(value = CIV_TOP_RIGHT),
                    @Loc(value = CIV_BOTTOM_RIGHT), @Loc(value = CIV_BOTTOM_LEFT),
                    @Loc(value = CIV_TOP_LEFT),
                    @Loc(value = CIV_TOP_RIGHT) }, tags = { "building=yes" }) })
    private Atlas closedLineFullyInsideOneCountry;

    @TestAtlas(

            points = { @Point(id = "1", coordinates = @Loc(value = AREA_CIV_SIDE_1)),
                    @Point(id = "2", coordinates = @Loc(value = AREA_CIV_SIDE_2)),
                    @Point(id = "3", coordinates = @Loc(value = AREA_LBR_SIDE_3)),
                    @Point(id = "4", coordinates = @Loc(value = AREA_LBR_SIDE_4)) },

            areas = { @Area(id = "1", coordinates = { @Loc(value = AREA_CIV_SIDE_1),
                    @Loc(value = AREA_LBR_SIDE_4), @Loc(value = AREA_LBR_SIDE_3),
                    @Loc(value = AREA_CIV_SIDE_2),
                    @Loc(value = AREA_CIV_SIDE_1) }, tags = { "building=yes" }) })
    private Atlas closedLineSpanningTwoCountries;

    @TestAtlas(

            points = { @Point(id = "1", coordinates = @Loc(value = AREA_CIV_SIDE_1)),
                    @Point(id = "2", coordinates = @Loc(value = AREA_CIV_SIDE_2)),
                    @Point(id = "3", coordinates = @Loc(value = AREA_LBR_SIDE_3)),
                    @Point(id = "4", coordinates = @Loc(value = AREA_LBR_SIDE_4)) },

            lines = { @Line(id = "1", coordinates = { @Loc(value = AREA_CIV_SIDE_1),
                    @Loc(value = AREA_CIV_SIDE_2), @Loc(value = AREA_LBR_SIDE_3),
                    @Loc(value = AREA_LBR_SIDE_4),
                    @Loc(value = AREA_CIV_SIDE_1) }, tags = { "highway=primary" }) })
    private Atlas closedEdgeSpanningTwoCountries;

    @TestAtlas(

            points = { @Point(id = "1", coordinates = @Loc(value = OUTSIDE_ALL_COUNTRIES_1)),
                    @Point(id = "2", coordinates = @Loc(value = OUTSIDE_ALL_COUNTRIES_2)) },

            lines = { @Line(id = "1", coordinates = { @Loc(value = OUTSIDE_ALL_COUNTRIES_1),
                    @Loc(value = OUTSIDE_ALL_COUNTRIES_2) }, tags = { "highway=primary" }) })
    private Atlas roadFullyOutsideAllBoundaries;

    @TestAtlas(

            points = { @Point(id = "1", coordinates = @Loc(value = IVORY_COAST_END)),
                    @Point(id = "2", coordinates = @Loc(value = ON_LIBERIA_AND_IVORY_COAST_BORDER)) },

            lines = { @Line(id = "1", coordinates = { @Loc(value = IVORY_COAST_END),
                    @Loc(value = ON_LIBERIA_AND_IVORY_COAST_BORDER) }, tags = {
                            "highway=primary" }) })
    private Atlas roadTouchingBoundary;

    @TestAtlas(

            points = { @Point(id = "1", coordinates = @Loc(value = IVORY_COAST_1)),
                    @Point(id = "2", coordinates = @Loc(value = LIBERIA_1)),
                    @Point(id = "3", coordinates = @Loc(value = IVORY_COAST_2)),
                    @Point(id = "4", coordinates = @Loc(value = IVORY_COAST_3)),
                    @Point(id = "5", coordinates = @Loc(value = LIBERIA_2)) },

            lines = { @Line(id = "1", coordinates = { @Loc(value = IVORY_COAST_1),
                    @Loc(value = LIBERIA_1), @Loc(value = IVORY_COAST_2),
                    @Loc(value = IVORY_COAST_3),
                    @Loc(value = LIBERIA_2) }, tags = { "highway=primary" }) })
    private Atlas roadWeavingAcrossBoundary;

    @TestAtlas(

            points = { @Point(id = "1", coordinates = @Loc(value = LIBERIA_END)),
                    @Point(id = "2", coordinates = @Loc(value = IVORY_COAST_END)) },

            lines = { @Line(id = "1000", coordinates = { @Loc(value = LIBERIA_END),
                    @Loc(value = IVORY_COAST_END) }, tags = { "highway=primary" }) })
    private Atlas roadAcrossTwoCountries;

    @TestAtlas(

            points = { @Point(id = "1", coordinates = @Loc(value = IVORY_COAST_3)),
                    @Point(id = "2", coordinates = @Loc(value = IVORY_COAST_2)) },

            lines = { @Line(id = "1", coordinates = { @Loc(value = IVORY_COAST_2),
                    @Loc(value = IVORY_COAST_3) }, tags = { "highway=primary" }) })
    private Atlas roadInsideOneCountry;

    @TestAtlas(

            points = { @Point(id = "1", coordinates = @Loc(value = LIBERIA_END)),
                    @Point(id = "2", coordinates = @Loc(value = ON_LIBERIA_AND_IVORY_COAST_BORDER)),
                    @Point(id = "3", coordinates = @Loc(value = IVORY_COAST_END)) },

            lines = { @Line(id = "1", coordinates = { @Loc(value = LIBERIA_END),
                    @Loc(value = ON_LIBERIA_AND_IVORY_COAST_BORDER),
                    @Loc(value = IVORY_COAST_END) }, tags = { "highway=primary" }) })
    private Atlas roadAcrossTwoCountriesWithPointOnBorder;

    @TestAtlas(

            lines = {

                    @Line(id = "1", coordinates = { @Loc(LIBERIA_1), @Loc(LIBERIA_1) }),
                    @Line(id = "2", coordinates = { @Loc(LIBERIA_1), @Loc(LIBERIA_2) })

            }

    )
    private Atlas singleNodeLine;
    @TestAtlas(

            points = { @Point(id = "214776000000", coordinates = @Loc(value = LOCATION_30)),
                    @Point(id = "214775000000", coordinates = @Loc(value = LOCATION_31)),
                    @Point(id = "214774000000", coordinates = @Loc(value = LOCATION_32)),
                    @Point(id = "214773000000", coordinates = @Loc(value = LOCATION_33)),
                    @Point(id = "214772000000", coordinates = @Loc(value = LOCATION_34)),
                    @Point(id = "214771000000", coordinates = @Loc(value = LOCATION_35)),
                    @Point(id = "214770000000", coordinates = @Loc(value = LOCATION_36)),
                    @Point(id = "214769000000", coordinates = @Loc(value = LOCATION_37)),
                    @Point(id = "214768000000", coordinates = @Loc(value = LOCATION_38)),
                    @Point(id = "214708000000", coordinates = @Loc(value = LOCATION_39)),
                    @Point(id = "214718000000", coordinates = @Loc(value = LOCATION_39)) },

            lines = {
                    @Line(id = "214778000000", coordinates = { @Loc(value = LOCATION_38),
                            @Loc(value = LOCATION_31),
                            @Loc(value = LOCATION_32) }, tags = { "highway=residential" }),
                    @Line(id = "214777000000", coordinates = { @Loc(value = LOCATION_38),
                            @Loc(value = LOCATION_37), @Loc(value = LOCATION_30),
                            @Loc(value = LOCATION_34), @Loc(value = LOCATION_35),
                            @Loc(value = LOCATION_32) }, tags = { "highway=primary" }) },

            relations = {

                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "leisure=park" }, members = {
                                    @Member(id = "214777000000", role = "outer", type = "line"),
                                    @Member(id = "214778000000", role = "outer", type = "line") }) })
    private Atlas singleOuterMadeOfOpenLinesSpanningTwoCountriesWithDuplicatePoints;
    @TestAtlas(

            points = { @Point(id = "214776000000", coordinates = @Loc(value = LOCATION_30)),
                    @Point(id = "214775000000", coordinates = @Loc(value = LOCATION_31)),
                    @Point(id = "214774000000", coordinates = @Loc(value = LOCATION_32)),
                    @Point(id = "214773000000", coordinates = @Loc(value = LOCATION_33)),
                    @Point(id = "214772000000", coordinates = @Loc(value = LOCATION_34)),
                    @Point(id = "214771000000", coordinates = @Loc(value = LOCATION_35)),
                    @Point(id = "214770000000", coordinates = @Loc(value = LOCATION_36)),
                    @Point(id = "214769000000", coordinates = @Loc(value = LOCATION_37)),
                    @Point(id = "214768000000", coordinates = @Loc(value = LOCATION_38)) },

            lines = {
                    @Line(id = "214778000000", coordinates = { @Loc(value = LOCATION_38),
                            @Loc(value = LOCATION_31),
                            @Loc(value = LOCATION_32) }, tags = { "highway=residential" }),
                    @Line(id = "214777000000", coordinates = { @Loc(value = LOCATION_38),
                            @Loc(value = LOCATION_37), @Loc(value = LOCATION_30),
                            @Loc(value = LOCATION_34), @Loc(value = LOCATION_35),
                            @Loc(value = LOCATION_32) }, tags = { "highway=primary" }) },

            relations = {

                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "leisure=park" }, members = {
                                    @Member(id = "214777000000", role = "outer", type = "line"),
                                    @Member(id = "214778000000", role = "outer", type = "line") }) })
    private Atlas singleOuterMadeOfOpenLinesSpanningTwoCountries;
    @TestAtlas(

            points = { @Point(id = "214602000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "214600000000", coordinates = @Loc(value = LOCATION_2)),
                    @Point(id = "214598000000", coordinates = @Loc(value = LOCATION_3)),
                    @Point(id = "214597000000", coordinates = @Loc(value = LOCATION_4)),
                    @Point(id = "214593000000", coordinates = @Loc(value = LOCATION_5)),
                    @Point(id = "214591000000", coordinates = @Loc(value = LOCATION_6)),
                    @Point(id = "214589000000", coordinates = @Loc(value = LOCATION_7)),
                    @Point(id = "214588000000", coordinates = @Loc(value = LOCATION_8)),
                    @Point(id = "214584000000", coordinates = @Loc(value = LOCATION_9)),
                    @Point(id = "214582000000", coordinates = @Loc(value = LOCATION_10)),
                    @Point(id = "214580000000", coordinates = @Loc(value = LOCATION_11)),
                    @Point(id = "214579000000", coordinates = @Loc(value = LOCATION_12)) },

            areas = { @Area(id = "214599000000", coordinates = { @Loc(value = LOCATION_4),
                    @Loc(value = LOCATION_3), @Loc(value = LOCATION_2), @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_4) }, tags = { "leisure=park" }),
                    @Area(id = "214590000000", coordinates = { @Loc(value = LOCATION_8),
                            @Loc(value = LOCATION_7), @Loc(value = LOCATION_6),
                            @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_8) }, tags = { "leisure=park" }),
                    @Area(id = "214581000000", coordinates = { @Loc(value = LOCATION_12),
                            @Loc(value = LOCATION_11), @Loc(value = LOCATION_10),
                            @Loc(value = LOCATION_9),
                            @Loc(value = LOCATION_12) }, tags = { "leisure=park" }) },

            relations = {

                    @Relation(id = "1", tags = { "type=multipolygon", "leisure=park" }, members = {
                            @Member(id = "214599000000", role = "outer", type = "area"),
                            @Member(id = "214590000000", role = "outer", type = "area"),
                            @Member(id = "214581000000", role = "outer", type = "area") })

            })
    private Atlas simpleMultiPolygon;

    @TestAtlas(

            points = { @Point(id = "214602000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "214600000000", coordinates = @Loc(value = LOCATION_2)),
                    @Point(id = "214598000000", coordinates = @Loc(value = LOCATION_3)),
                    @Point(id = "214597000000", coordinates = @Loc(value = LOCATION_4)),
                    @Point(id = "214593000000", coordinates = @Loc(value = LOCATION_5)),
                    @Point(id = "214591000000", coordinates = @Loc(value = LOCATION_6)),
                    @Point(id = "214589000000", coordinates = @Loc(value = LOCATION_7)),
                    @Point(id = "214588000000", coordinates = @Loc(value = LOCATION_8)),
                    @Point(id = "214584000000", coordinates = @Loc(value = LOCATION_9)),
                    @Point(id = "214582000000", coordinates = @Loc(value = LOCATION_10)),
                    @Point(id = "214580000000", coordinates = @Loc(value = LOCATION_11)),
                    @Point(id = "214579000000", coordinates = @Loc(value = LOCATION_12)) },

            areas = { @Area(id = "214599000000", coordinates = { @Loc(value = LOCATION_4),
                    @Loc(value = LOCATION_3), @Loc(value = LOCATION_2), @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_4) }, tags = { "leisure=park" }),
                    @Area(id = "214590000000", coordinates = { @Loc(value = LOCATION_8),
                            @Loc(value = LOCATION_7), @Loc(value = LOCATION_6),
                            @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_8) }, tags = { "leisure=park" }),
                    @Area(id = "214581000000", coordinates = { @Loc(value = LOCATION_12),
                            @Loc(value = LOCATION_11), @Loc(value = LOCATION_10),
                            @Loc(value = LOCATION_9),
                            @Loc(value = LOCATION_12) }, tags = { "leisure=park" }) },

            relations = {

                    @Relation(id = "1", tags = { "type=boundary" }, members = {
                            @Member(id = "214599000000", role = "outer", type = "area"),
                            @Member(id = "214590000000", role = "outer", type = "area"),
                            @Member(id = "214581000000", role = "outer", type = "area") })

            })
    private Atlas simpleBoundaryRelation;

    @TestAtlas(

            points = { @Point(id = "214602000000", coordinates = @Loc(value = LOCATION_1)),
                    @Point(id = "214600000000", coordinates = @Loc(value = LOCATION_2)),
                    @Point(id = "214598000000", coordinates = @Loc(value = LOCATION_3)),
                    @Point(id = "214597000000", coordinates = @Loc(value = LOCATION_4)),
                    @Point(id = "214593000000", coordinates = @Loc(value = LOCATION_5)),
                    @Point(id = "214591000000", coordinates = @Loc(value = LOCATION_6)),
                    @Point(id = "214589000000", coordinates = @Loc(value = LOCATION_7)),
                    @Point(id = "214588000000", coordinates = @Loc(value = LOCATION_8)),
                    @Point(id = "214584000000", coordinates = @Loc(value = LOCATION_9)),
                    @Point(id = "214582000000", coordinates = @Loc(value = LOCATION_10)),
                    @Point(id = "214580000000", coordinates = @Loc(value = LOCATION_11)),
                    @Point(id = "214579000000", coordinates = @Loc(value = LOCATION_12)) },

            areas = { @Area(id = "214599000000", coordinates = { @Loc(value = LOCATION_4),
                    @Loc(value = LOCATION_3), @Loc(value = LOCATION_2), @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_4) }, tags = { "leisure=park" }),
                    @Area(id = "214590000000", coordinates = { @Loc(value = LOCATION_8),
                            @Loc(value = LOCATION_7), @Loc(value = LOCATION_6),
                            @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_8) }, tags = { "leisure=park" }),
                    @Area(id = "214581000000", coordinates = { @Loc(value = LOCATION_12),
                            @Loc(value = LOCATION_11), @Loc(value = LOCATION_10),
                            @Loc(value = LOCATION_9),
                            @Loc(value = LOCATION_12) }, tags = { "leisure=park" }) },

            relations = {

                    @Relation(id = "1", tags = { "type=boundary" }, members = {
                            @Member(id = "214590000000", role = "outer", type = "area") })

            })
    private Atlas simpleBoundaryRelationConsolidate;

    @TestAtlas(

            points = { @Point(id = "106032000000", coordinates = @Loc(value = LOCATION_21)),
                    @Point(id = "106031000000", coordinates = @Loc(value = LOCATION_22)),
                    @Point(id = "106030000000", coordinates = @Loc(value = LOCATION_23)),
                    @Point(id = "106029000000", coordinates = @Loc(value = LOCATION_24)),
                    @Point(id = "106028000000", coordinates = @Loc(value = LOCATION_25)),
                    @Point(id = "106027000000", coordinates = @Loc(value = LOCATION_26)),
                    @Point(id = "106026000000", coordinates = @Loc(value = LOCATION_27)),
                    @Point(id = "106025000000", coordinates = @Loc(value = LOCATION_28)),
                    @Point(id = "106024000000", coordinates = @Loc(value = LOCATION_29)) },

            lines = {
                    @Line(id = "106036000000", coordinates = { @Loc(value = LOCATION_21),
                            @Loc(value = LOCATION_27), @Loc(value = LOCATION_26) }),
                    @Line(id = "106035000000", coordinates = { @Loc(value = LOCATION_21),
                            @Loc(value = LOCATION_28), @Loc(value = LOCATION_26) }),
                    @Line(id = "106034000000", coordinates = { @Loc(value = LOCATION_24),
                            @Loc(value = LOCATION_25), @Loc(value = LOCATION_22) }),
                    @Line(id = "106033000000", coordinates = { @Loc(value = LOCATION_24),
                            @Loc(value = LOCATION_29), @Loc(value = LOCATION_23),
                            @Loc(value = LOCATION_22) }) },

            relations = {

                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "natural=water" }, members = {
                                    @Member(id = "106033000000", role = "outer", type = "line"),
                                    @Member(id = "106034000000", role = "outer", type = "line"),
                                    @Member(id = "106035000000", role = "inner", type = "line"),
                                    @Member(id = "106036000000", role = "inner", type = "line") })

            })
    private Atlas complexMuliPolygonWithHoleUsingOpenLines;
    @TestAtlas(

            points = { @Point(id = "106032000000", coordinates = @Loc(value = LOCATION_21)),
                    @Point(id = "106031000000", coordinates = @Loc(value = LOCATION_22)),
                    @Point(id = "106030000000", coordinates = @Loc(value = LOCATION_23)),
                    @Point(id = "106029000000", coordinates = @Loc(value = LOCATION_24)),
                    @Point(id = "106028000000", coordinates = @Loc(value = LOCATION_25)),
                    @Point(id = "106027000000", coordinates = @Loc(value = LOCATION_26)),
                    @Point(id = "106026000000", coordinates = @Loc(value = LOCATION_27)),
                    @Point(id = "106025000000", coordinates = @Loc(value = LOCATION_28)),
                    @Point(id = "106024000000", coordinates = @Loc(value = LOCATION_29)) },

            areas = { @Area(id = "106036000000", coordinates = { @Loc(value = LOCATION_28),
                    @Loc(value = LOCATION_21), @Loc(value = LOCATION_27), @Loc(value = LOCATION_26),
                    @Loc(value = LOCATION_28) }, tags = { "water=natural" }),

                    @Area(id = "106034000000", coordinates = { @Loc(value = LOCATION_23),
                            @Loc(value = LOCATION_29), @Loc(value = LOCATION_24),
                            @Loc(value = LOCATION_25), @Loc(value = LOCATION_22),
                            @Loc(value = LOCATION_23) }, tags = { "water=natural" }) },

            relations = {

                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "natural=water" }, members = {
                                    @Member(id = "106034000000", role = "outer", type = "area"),
                                    @Member(id = "106036000000", role = "inner", type = "area") })

            })
    private Atlas complexMuliPolygonWithHoleUsingClosedLines;
    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_13)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_14)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_15)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_16)),
                    @Point(id = "108760000000", coordinates = @Loc(value = LOCATION_17)),
                    @Point(id = "108762000000", coordinates = @Loc(value = LOCATION_18)),
                    @Point(id = "108764000000", coordinates = @Loc(value = LOCATION_19)),
                    @Point(id = "108766000000", coordinates = @Loc(value = LOCATION_20)) },

            areas = {
                    @Area(id = "108768000000", coordinates = { @Loc(value = LOCATION_13),
                            @Loc(value = LOCATION_14), @Loc(value = LOCATION_15),
                            @Loc(value = LOCATION_16), @Loc(value = LOCATION_13) }),
                    @Area(id = "108770000000", coordinates = { @Loc(value = LOCATION_17),
                            @Loc(value = LOCATION_18), @Loc(value = LOCATION_19),
                            @Loc(value = LOCATION_20), @Loc(value = LOCATION_17) }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "area"),
                            @Member(id = "108770000000", role = "inner", type = "area") })

            })
    private Atlas multiPolygonWithHole;
    @TestAtlas(points = {
            @Point(id = "1", coordinates = @Loc(value = LOCATION_13), tags = { "leisure=park" }),
            @Point(id = "2", coordinates = @Loc(value = LOCATION_14), tags = { "leisure=park" })

    }, relations = {
            @Relation(id = "3", tags = { "bugs=no" }, members = {
                    @Member(id = "1", role = "member", type = "point") }),
            @Relation(id = "4", tags = { "bugs=no" }, members = {
                    @Member(id = "2", role = "member", type = "point") }),
            @Relation(id = "5", tags = { "bugs=no" }, members = {
                    @Member(id = "3", role = "member", type = "relation"),
                    @Member(id = "4", role = "member", type = "relation") }) })
    private Atlas relationWithOnlyRelationsAsMembers;

    @TestAtlas(points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_13)),
            @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_14)),
            @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_15)),
            @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_16)),
            @Point(id = "108760000000", coordinates = @Loc(value = LOCATION_17)),
            @Point(id = "108762000000", coordinates = @Loc(value = LOCATION_18)),
            @Point(id = "108764000000", coordinates = @Loc(value = LOCATION_19)),
            @Point(id = "108766000000", coordinates = @Loc(value = LOCATION_20)) },

            areas = {
                    @Area(id = "108768000000", coordinates = { @Loc(value = LOCATION_13),
                            @Loc(value = LOCATION_14), @Loc(value = LOCATION_15),
                            @Loc(value = LOCATION_16), @Loc(value = LOCATION_13) }),
                    @Area(id = "108770000000", coordinates = { @Loc(value = LOCATION_17),
                            @Loc(value = LOCATION_18), @Loc(value = LOCATION_19),
                            @Loc(value = LOCATION_20), @Loc(value = LOCATION_17) }),
                    @Area(id = "3", coordinates = { @Loc(value = LOCATION_17),
                            @Loc(value = LOCATION_18), @Loc(value = LOCATION_19),
                            @Loc(value = LOCATION_20), @Loc(value = LOCATION_17) }) },

            relations = {
                    @Relation(id = "5", tags = { "bugs=no" }, members = {
                            @Member(id = "108756000000", role = "member", type = "point") }),
                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "area"),
                            @Member(id = "108770000000", role = "inner", type = "area"),
                            @Member(id = "108752000000", role = "badType", type = "point"),
                            @Member(id = "5", role = "badType", type = "relation"),
                            @Member(id = "3", role = "badRole", type = "area") }) })
    private Atlas relationWithInvalidMultiPolygonMembers;
    @TestAtlas(points = { @Point(id = "214775000000", coordinates = @Loc(value = LOCATION_31)),
            @Point(id = "214774000000", coordinates = @Loc(value = LOCATION_32)),
            @Point(id = "214768000000", coordinates = @Loc(value = LOCATION_38)) },

            lines = { @Line(id = "214778000000", coordinates = { @Loc(value = LOCATION_38),
                    @Loc(value = LOCATION_31),
                    @Loc(value = LOCATION_32) }, tags = { "highway=residential" }) },

            relations = {
                    @Relation(id = "214806000000", tags = {}, members = {
                            @Member(id = "214778000000", role = "", type = "line") }),
                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "natural=water" }, members = {
                                    @Member(id = "214778000000", role = "outer", type = "line") }) })
    private Atlas singleOuterMultiPolygonSpanningTwoAtlases1;

    @TestAtlas(points = { @Point(id = "214776000000", coordinates = @Loc(value = LOCATION_30)),

            @Point(id = "214774000000", coordinates = @Loc(value = LOCATION_32)),

            @Point(id = "214772000000", coordinates = @Loc(value = LOCATION_34)),
            @Point(id = "214771000000", coordinates = @Loc(value = LOCATION_35)),
            @Point(id = "214769000000", coordinates = @Loc(value = LOCATION_37)),
            @Point(id = "214768000000", coordinates = @Loc(value = LOCATION_38)) },

            lines = { @Line(id = "214777000000", coordinates = { @Loc(value = LOCATION_38),
                    @Loc(value = LOCATION_37), @Loc(value = LOCATION_30), @Loc(value = LOCATION_34),
                    @Loc(value = LOCATION_35),
                    @Loc(value = LOCATION_32) }, tags = { "highway=primary" }) },

            relations = {
                    @Relation(id = "214806000000", tags = {}, members = {
                            @Member(id = "214777000000", role = "", type = "line") }),
                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "natural=water" }, members = {
                                    @Member(id = "214777000000", role = "outer", type = "line") }) })
    private Atlas singleOuterMultiPolygonSpanningTwoAtlases2;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_13)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_14)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_15)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_16)),
                    @Point(id = "108760000000", coordinates = @Loc(value = LOCATION_17)),
                    @Point(id = "108762000000", coordinates = @Loc(value = LOCATION_18)),
                    @Point(id = "108764000000", coordinates = @Loc(value = LOCATION_19)),
                    @Point(id = "108766000000", coordinates = @Loc(value = LOCATION_20)) },

            lines = {
                    @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_13),
                            @Loc(value = LOCATION_14), @Loc(value = LOCATION_15),
                            @Loc(value = LOCATION_16), @Loc(value = LOCATION_13) }),
                    @Line(id = "108770000000", coordinates = { @Loc(value = "6.9393431,-8.3299635"),
                            @Loc(value = "6.9309326,-8.3299635 "),
                            @Loc(value = "6.9309326,-8.3134173"),
                            @Loc(value = "6.9393431,-8.3134173"),
                            @Loc(value = "6.9393431,-8.3299635") }),
                    @Line(id = "108760000000", coordinates = { @Loc(value = LOCATION_60),
                            @Loc(value = LOCATION_61), @Loc(value = LOCATION_62),
                            @Loc(value = LOCATION_63), @Loc(value = LOCATION_60) }) },

            relations = {

                    @Relation(id = "1", tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line"),
                            @Member(id = "108770000000", role = "inner", type = "line"),
                            @Member(id = "108760000000", role = "inner", type = "line") })

            })
    private Atlas multiPolygonWithOverlappingSlicedInners;

    @TestAtlas(

            points = { @Point(id = "108752000000", coordinates = @Loc(value = LOCATION_13)),
                    @Point(id = "108754000000", coordinates = @Loc(value = LOCATION_14)),
                    @Point(id = "108756000000", coordinates = @Loc(value = LOCATION_15)),
                    @Point(id = "108758000000", coordinates = @Loc(value = LOCATION_16)),
                    @Point(id = "108760000000", coordinates = @Loc(value = LOCATION_17)),
                    @Point(id = "108762000000", coordinates = @Loc(value = LOCATION_18)),
                    @Point(id = "108764000000", coordinates = @Loc(value = LOCATION_19)),
                    @Point(id = "108766000000", coordinates = @Loc(value = LOCATION_20)) },

            lines = {
                    @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_13),
                            @Loc(value = LOCATION_14), @Loc(value = LOCATION_15),
                            @Loc(value = LOCATION_16), @Loc(value = LOCATION_13) }),
                    @Line(id = "108770000000", coordinates = { @Loc(value = "6.9393431,-8.3299635"),
                            @Loc(value = "6.9309326,-8.3299635 "),
                            @Loc(value = "6.9309326,-8.3234173"),
                            @Loc(value = "6.9393431,-8.3234173"),
                            @Loc(value = "6.9393431,-8.3299635") }),
                    @Line(id = "108760000000", coordinates = { @Loc(value = LOCATION_60),
                            @Loc(value = LOCATION_61), @Loc(value = LOCATION_62),
                            @Loc(value = LOCATION_63), @Loc(value = LOCATION_60) }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line"),
                            @Member(id = "108770000000", role = "inner", type = "line"),
                            @Member(id = "108760000000", role = "inner", type = "line") })

            })
    private Atlas multiPolygonWithOverlappingUnslicedInners;

    public Atlas getClosedEdgeSpanningTwoCountriesAtlas()
    {
        return this.closedEdgeSpanningTwoCountries;
    }

    public Atlas getClosedLineFullyInOneCountryAtlas()
    {
        return this.closedLineFullyInsideOneCountry;
    }

    public Atlas getClosedLineSpanningTwoCountriesAtlas()
    {
        return this.closedLineSpanningTwoCountries;
    }

    public Atlas getComplexMultiPolygonWithHoleUsingClosedLinesAtlas()
    {
        return this.complexMuliPolygonWithHoleUsingClosedLines;
    }

    public Atlas getComplexMultiPolygonWithHoleUsingOpenLinesAtlas()
    {
        return this.complexMuliPolygonWithHoleUsingOpenLines;
    }

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

    public Atlas getMultiPolygonWithOverlappingSlicedInners()
    {
        return this.multiPolygonWithOverlappingSlicedInners;
    }

    public Atlas getMultiPolygonWithOverlappingUnslicedInners()
    {
        return this.multiPolygonWithOverlappingUnslicedInners;
    }

    public Atlas getOpenMultiPolygonAcrossBoundaryAtlas()
    {
        return this.openMultiPolygonAcrossBoundary;
    }

    public Atlas getOpenMultiPolygonInOneCountryAtlas()
    {
        return this.openMultiPolygonInOneCountry;
    }

    public Atlas getRelationWithInvalidMultiPolygonMembers()
    {
        return this.relationWithInvalidMultiPolygonMembers;
    }

    public Atlas getRelationWithOneClosedAndOneOpenMemberAtlas()
    {
        return this.closedAndNonClosedMembersRelation;
    }

    public Atlas getRelationWithOnlyRelationsAsMembers()
    {
        return this.relationWithOnlyRelationsAsMembers;
    }

    public Atlas getRoadAcrossTwoCountriesAtlas()
    {
        return this.roadAcrossTwoCountries;
    }

    public Atlas getRoadAcrossTwoCountriesWithPointOnBorderAtlas()
    {
        return this.roadAcrossTwoCountriesWithPointOnBorder;
    }

    public Atlas getRoadFullyInOneCountryAtlas()
    {
        return this.roadInsideOneCountry;
    }

    public Atlas getRoadOutsideAllBoundariesAtlas()
    {
        return this.roadFullyOutsideAllBoundaries;
    }

    public Atlas getRoadTouchingBoundaryAtlas()
    {
        return this.roadTouchingBoundary;
    }

    public Atlas getRoadWeavingAlongBoundaryAtlas()
    {
        return this.roadWeavingAcrossBoundary;
    }

    public Atlas getSelfIntersectingOuterMemberRelationAcrossBoundaryAtlas()
    {
        return this.selfIntersectingOuterMemberRelationAcrossBoundary;
    }

    public Atlas getSelfIntersectingOuterMemberRelationAtlas()
    {
        return this.selfIntersectingOuterMemberRelation;
    }

    public Atlas getSimpleBoundaryRelationAtlas()
    {
        return this.simpleBoundaryRelation;
    }

    public Atlas getSimpleBoundaryRelationConsolidateAtlas()
    {
        return this.simpleBoundaryRelationConsolidate;
    }

    public Atlas getSimpleMultiPolygonAtlas()
    {
        return this.simpleMultiPolygon;
    }

    public Atlas getSimpleMultiPolygonWithHoleAtlas()
    {
        return this.multiPolygonWithHole;
    }

    public Atlas getSingleNodeLine()
    {
        return this.singleNodeLine;
    }

    public Atlas getSingleOuterMadeOfOpenLinesSpanningTwoCountriesAtlas()
    {
        return this.singleOuterMadeOfOpenLinesSpanningTwoCountries;
    }

    public Atlas getSingleOuterMadeOfOpenLinesSpanningTwoCountriesAtlasWithDuplicatePoints()
    {
        return this.singleOuterMadeOfOpenLinesSpanningTwoCountriesWithDuplicatePoints;
    }

    public Atlas getSingleOuterMultiPolygonSpanningTwoAtlases1()
    {
        return this.singleOuterMultiPolygonSpanningTwoAtlases1;
    }

    public Atlas getSingleOuterMultiPolygonSpanningTwoAtlases2()
    {
        return this.singleOuterMultiPolygonSpanningTwoAtlases2;
    }
}
