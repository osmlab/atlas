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
 * {@link RelationSlicingTest} test data.
 *
 * @author mgostintsev
 */
public class RelationSlicingTestRule extends CoreTestRule
{
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

            lines = { @Line(id = "214599000000", coordinates = { @Loc(value = LOCATION_4),
                    @Loc(value = LOCATION_3), @Loc(value = LOCATION_2), @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_4) }, tags = { "leisure=park" }),
                    @Line(id = "214590000000", coordinates = { @Loc(value = LOCATION_8),
                            @Loc(value = LOCATION_7), @Loc(value = LOCATION_6),
                            @Loc(value = LOCATION_5),
                            @Loc(value = LOCATION_8) }, tags = { "leisure=park" }),
                    @Line(id = "214581000000", coordinates = { @Loc(value = LOCATION_12),
                            @Loc(value = LOCATION_11), @Loc(value = LOCATION_10),
                            @Loc(value = LOCATION_9),
                            @Loc(value = LOCATION_12) }, tags = { "leisure=park" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "leisure=park" }, members = {
                            @Member(id = "214599000000", role = "outer", type = "line"),
                            @Member(id = "214590000000", role = "outer", type = "line"),
                            @Member(id = "214581000000", role = "outer", type = "line") })

            })
    private Atlas simpleMultiPolygon;

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
                            @Loc(value = LOCATION_27),
                            @Loc(value = LOCATION_26) }, tags = { "highway=primary" }),
                    @Line(id = "106035000000", coordinates = { @Loc(value = LOCATION_21),
                            @Loc(value = LOCATION_28),
                            @Loc(value = LOCATION_26) }, tags = { "highway=secondary" }),
                    @Line(id = "106034000000", coordinates = { @Loc(value = LOCATION_24),
                            @Loc(value = LOCATION_25),
                            @Loc(value = LOCATION_22) }, tags = { "highway=secondary" }),
                    @Line(id = "106033000000", coordinates = { @Loc(value = LOCATION_24),
                            @Loc(value = LOCATION_29), @Loc(value = LOCATION_23),
                            @Loc(value = LOCATION_22) }, tags = { "highway=primary" }) },

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

            lines = { @Line(id = "106036000000", coordinates = { @Loc(value = LOCATION_28),
                    @Loc(value = LOCATION_21), @Loc(value = LOCATION_27), @Loc(value = LOCATION_26),
                    @Loc(value = LOCATION_28) }, tags = { "highway=primary" }),

                    @Line(id = "106034000000", coordinates = { @Loc(value = LOCATION_23),
                            @Loc(value = LOCATION_29), @Loc(value = LOCATION_24),
                            @Loc(value = LOCATION_25), @Loc(value = LOCATION_22),
                            @Loc(value = LOCATION_23) }, tags = { "highway=secondary" }) },

            relations = {

                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "natural=water" }, members = {
                                    @Member(id = "106034000000", role = "outer", type = "line"),
                                    @Member(id = "106036000000", role = "inner", type = "line") })

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

            lines = { @Line(id = "108768000000", coordinates = { @Loc(value = LOCATION_13),
                    @Loc(value = LOCATION_14), @Loc(value = LOCATION_15), @Loc(value = LOCATION_16),
                    @Loc(value = LOCATION_13) }, tags = { "building=yes" }),
                    @Line(id = "108770000000", coordinates = { @Loc(value = LOCATION_17),
                            @Loc(value = LOCATION_18), @Loc(value = LOCATION_19),
                            @Loc(value = LOCATION_20),
                            @Loc(value = LOCATION_17) }, tags = { "building=yes" }) },

            relations = {

                    @Relation(tags = { "type=multipolygon", "building=yes" }, members = {
                            @Member(id = "108768000000", role = "outer", type = "line"),
                            @Member(id = "108770000000", role = "inner", type = "line") })

            })
    private Atlas multiPolygonWithHole;

    @TestAtlas(points = { @Point(id = "1", coordinates = @Loc(value = LOCATION_13)),
            @Point(id = "2", coordinates = @Loc(value = LOCATION_14))

    }, relations = {
            @Relation(id = "3", tags = { "bugs=no" }, members = {
                    @Member(id = "1", role = "member", type = "point") }),
            @Relation(id = "4", tags = { "bugs=no" }, members = {
                    @Member(id = "2", role = "member", type = "point") }),
            @Relation(id = "5", tags = { "bugs=no" }, members = {
                    @Member(id = "3", role = "member", type = "relation"),
                    @Member(id = "4", role = "member", type = "relation") }) })
    private Atlas relationWithOnlyRelationsAsMembers;

    @TestAtlas(points = { @Point(id = "214775000000", coordinates = @Loc(value = LOCATION_31)),
            @Point(id = "214774000000", coordinates = @Loc(value = LOCATION_32)),
            @Point(id = "214768000000", coordinates = @Loc(value = LOCATION_38)) },

            lines = { @Line(id = "214778000000", coordinates = { @Loc(value = LOCATION_38),
                    @Loc(value = LOCATION_31),
                    @Loc(value = LOCATION_32) }, tags = { "highway=residential" }) },

            relations = {

                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "natural=water" }, members = {
                                    @Member(id = "214778000000", role = "outer", type = "line") }) })
    private Atlas singleOuterWaterSpanningTwoAtlases1;

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

                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "natural=water" }, members = {
                                    @Member(id = "214777000000", role = "outer", type = "line") }) })
    private Atlas singleOuterWaterSpanningTwoAtlases2;

    @TestAtlas(points = { @Point(id = "214775000000", coordinates = @Loc(value = LOCATION_31)),
            @Point(id = "214774000000", coordinates = @Loc(value = LOCATION_32)),
            @Point(id = "214768000000", coordinates = @Loc(value = LOCATION_38)) },

            lines = { @Line(id = "214778000000", coordinates = { @Loc(value = LOCATION_38),
                    @Loc(value = LOCATION_31),
                    @Loc(value = LOCATION_32) }, tags = { "highway=residential" }) },

            relations = {

                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "building=yes" }, members = {
                                    @Member(id = "214778000000", role = "outer", type = "line") }) })
    private Atlas singleOuterNonWaterSpanningTwoAtlases1;

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

                    @Relation(id = "214805000000", tags = { "type=multipolygon",
                            "building=yes" }, members = {
                                    @Member(id = "214777000000", role = "outer", type = "line") }) })
    private Atlas singleOuterNonWaterSpanningTwoAtlases2;

    public Atlas getComplexMultiPolygonWithHoleUsingClosedLinesAtlas()
    {
        return this.complexMuliPolygonWithHoleUsingClosedLines;
    }

    public Atlas getComplexMultiPolygonWithHoleUsingOpenLinesAtlas()
    {
        return this.complexMuliPolygonWithHoleUsingOpenLines;
    }

    public Atlas getRelationWithOnlyRelationsAsMembers()
    {
        return this.relationWithOnlyRelationsAsMembers;
    }

    public Atlas getSimpleMultiPolygonAtlas()
    {
        return this.simpleMultiPolygon;
    }

    public Atlas getSimpleMultiPolygonWithHoleAtlas()
    {
        return this.multiPolygonWithHole;
    }

    public Atlas getSingleOuterMadeOfOpenLinesSpanningTwoCountriesAtlas()
    {
        return this.singleOuterMadeOfOpenLinesSpanningTwoCountries;
    }

    public Atlas getSingleOuterNonWaterSpanningTwoAtlases1()
    {
        return this.singleOuterNonWaterSpanningTwoAtlases1;
    }

    public Atlas getSingleOuterNonWaterSpanningTwoAtlases2()
    {
        return this.singleOuterNonWaterSpanningTwoAtlases2;
    }

    public Atlas getSingleOuterWaterSpanningTwoAtlases1()
    {
        return this.singleOuterWaterSpanningTwoAtlases1;
    }

    public Atlas getSingleOuterWaterSpanningTwoAtlases2()
    {
        return this.singleOuterWaterSpanningTwoAtlases2;
    }
}
