package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * {@link LineAndPointSlicingTest} test data.
 *
 * @author mgostintsev
 */
public class LineAndPointSlicingTestRule extends CoreTestRule
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

            lines = { @Line(id = "1", coordinates = { @Loc(value = AREA_CIV_SIDE_1),
                    @Loc(value = AREA_CIV_SIDE_2), @Loc(value = AREA_LBR_SIDE_3),
                    @Loc(value = AREA_LBR_SIDE_4),
                    @Loc(value = AREA_CIV_SIDE_1) }, tags = { "building=yes" }) })
    private Atlas closedLineSpanningTwoCountries;

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

            lines = { @Line(id = "1", coordinates = { @Loc(value = LIBERIA_END),
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

    public Atlas getClosedLineFullyInOneCountryAtlas()
    {
        return this.closedLineFullyInsideOneCountry;
    }

    public Atlas getClosedLineSpanningTwoCountriesAtlas()
    {
        return this.closedLineSpanningTwoCountries;
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
}
