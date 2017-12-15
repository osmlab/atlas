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

    public Atlas getInnerWithoutOuterAcrossBoundaryAtlas()
    {
        return this.innerWithoutOuterAcrossBoundary;
    }

    public Atlas getInnerWithoutOuterInOneCountryAtlas()
    {
        return this.innerWithoutOuterInOneCountry;
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
}
