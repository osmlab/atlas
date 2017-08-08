package org.openstreetmap.atlas.geography;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link PolygonCoveringPolygonTest} data
 *
 * @author mgostintsev
 */
public class PolygonCoveringPolygonTestRule extends CoreTestRule
{
    // Rectangle #1
    private static final String OUTER_TOP_LEFT = "39.9978684, 116.2741473";
    private static final String OUTER_TOP_RIGHT = "39.9979242, 116.2747418";
    private static final String OUTER_BOTTOM_RIGHT = "39.9975751, 116.2748088";
    private static final String OUTER_BOTTOM_LEFT = "39.9975152, 116.2741904";

    // Rectangle #2, fully contained within Rectangle #1
    private static final String INNER_2_TOP_LEFT = "39.9977887, 116.2743726";
    private static final String INNER_2_TOP_RIGHT = "39.9977948, 116.2744396";
    private static final String INNER_2_BOTTOM_RIGHT = "39.9977096, 116.2744528";
    private static final String INNER_2_BOTTOM_LEFT = "39.9977035, 116.2743859";

    // Rectangle #3, fully contained within Rectangle #1, to the right of Rectangle #2
    private static final String INNER_3_TOP_LEFT = "39.9977983, 116.274481";
    private static final String INNER_3_TOP_RIGHT = "39.9978044, 116.2745479";
    private static final String INNER_3_BOTTOM_RIGHT = "39.9977192, 116.2745612";
    private static final String INNER_3_BOTTOM_LEFT = "39.9977131, 116.2744943";

    // Rectangle #4, independent of others
    private static final String TOP_LEFT = "39.9978998, 116.2744895";
    private static final String TOP_RIGHT = "39.9979242, 116.2747418";
    private static final String BOTTOM_RIGHT = "39.9978469, 116.2747545";
    private static final String BOTTOM_LEFT = "39.9978225, 116.2745022";

    // Rectangle #5, covers a corner of #4
    private static final String CORNER_INTERSECT_TOP_LEFT = "39.9978381, 116.2744597";
    private static final String CORNER_INTERSECT_TOP_RIGHT = "39.9978487, 116.2745362";
    private static final String CORNER_INTERSECT_BOTTOM_RIGHT = "39.9977605, 116.2745571";
    private static final String CORNER_INTERSECT_BOTTOM_LEFT = "39.9977499, 116.2744806";

    // Rectangle #6, covers a side of #4
    private static final String SIDE_INTERSECT_TOP_LEFT = "39.9978772, 116.274453";
    private static final String SIDE_INTERSECT_TOP_RIGHT = "39.9978857, 116.2745267";
    private static final String SIDE_INTERSECT_BOTTOM_RIGHT = "39.9978464, 116.2745345";
    private static final String SIDE_INTERSECT_BOTTOM_LEFT = "39.9978379, 116.2744607";

    // Rectangle #7, touches a corner of #4
    private static final String TOUCHES_CORNER_TOP_LEFT = "39.9978142, 116.2744215";
    private static final String TOUCHES_CORNER_TOP_RIGHT = "39.9978256, 116.2745047";
    private static final String TOUCHES_CORNER_BOTTOM_RIGHT = "39.9977801, 116.2745154";
    private static final String TOUCHES_CORNER_BOTTOM_LEFT = "39.9977686, 116.2744321";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = INNER_2_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = INNER_2_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = INNER_2_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = INNER_2_BOTTOM_LEFT))

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = INNER_2_BOTTOM_LEFT),
                            @Loc(value = INNER_2_TOP_LEFT), @Loc(value = INNER_2_TOP_RIGHT),
                            @Loc(value = INNER_2_BOTTOM_RIGHT) }, tags = { "building=yes" }),
                    @Area(id = "2", coordinates = { @Loc(value = INNER_2_BOTTOM_LEFT),
                            @Loc(value = INNER_2_TOP_LEFT), @Loc(value = INNER_2_TOP_RIGHT),
                            @Loc(value = INNER_2_BOTTOM_RIGHT) }, tags = { "building=yes" }) })
    private Atlas polygonsStackedOnEachOther;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = INNER_2_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = INNER_2_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = INNER_2_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = INNER_2_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = INNER_3_TOP_LEFT)),
                    @Node(id = "6", coordinates = @Loc(value = INNER_3_TOP_RIGHT)),
                    @Node(id = "7", coordinates = @Loc(value = INNER_3_BOTTOM_RIGHT)),
                    @Node(id = "8", coordinates = @Loc(value = INNER_3_BOTTOM_LEFT)),

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = INNER_2_TOP_LEFT),
                            @Loc(value = INNER_2_TOP_RIGHT), @Loc(value = INNER_2_BOTTOM_RIGHT),
                            @Loc(value = INNER_2_BOTTOM_LEFT) }, tags = { "building=yes" }),
                    @Area(id = "2", coordinates = { @Loc(value = INNER_3_TOP_LEFT),
                            @Loc(value = INNER_3_TOP_RIGHT), @Loc(value = INNER_3_BOTTOM_RIGHT),
                            @Loc(value = INNER_3_BOTTOM_LEFT) }, tags = { "building=yes" }) })
    private Atlas nonOverlappingNonTouchingPolygons;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = INNER_2_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = INNER_2_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = INNER_2_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = INNER_2_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = OUTER_TOP_LEFT)),
                    @Node(id = "6", coordinates = @Loc(value = OUTER_TOP_RIGHT)),
                    @Node(id = "7", coordinates = @Loc(value = OUTER_BOTTOM_RIGHT)),
                    @Node(id = "8", coordinates = @Loc(value = OUTER_BOTTOM_LEFT)),

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_TOP_RIGHT), @Loc(value = OUTER_BOTTOM_RIGHT),
                            @Loc(value = OUTER_BOTTOM_LEFT) }, tags = { "building=yes" }),
                    @Area(id = "2", coordinates = { @Loc(value = INNER_2_TOP_LEFT),
                            @Loc(value = INNER_2_TOP_RIGHT), @Loc(value = INNER_2_BOTTOM_RIGHT),
                            @Loc(value = INNER_2_BOTTOM_LEFT) }, tags = { "building=yes" }) })
    private Atlas polygonWithinPolygon;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = INNER_2_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = INNER_2_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = INNER_2_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = INNER_2_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = INNER_3_TOP_RIGHT)),
                    @Node(id = "6", coordinates = @Loc(value = INNER_3_BOTTOM_RIGHT))

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = INNER_2_TOP_LEFT),
                            @Loc(value = INNER_2_TOP_RIGHT), @Loc(value = INNER_2_BOTTOM_RIGHT),
                            @Loc(value = INNER_2_BOTTOM_LEFT) }, tags = { "building=yes" }),
                    @Area(id = "2", coordinates = { @Loc(value = INNER_2_TOP_RIGHT),
                            @Loc(value = INNER_3_TOP_RIGHT), @Loc(value = INNER_3_BOTTOM_RIGHT),
                            @Loc(value = INNER_2_BOTTOM_RIGHT) }, tags = { "building=yes" }) })
    private Atlas polygonsSharingSide;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = TOUCHES_CORNER_TOP_LEFT)),
                    @Node(id = "6", coordinates = @Loc(value = TOUCHES_CORNER_TOP_RIGHT)),
                    @Node(id = "7", coordinates = @Loc(value = TOUCHES_CORNER_BOTTOM_RIGHT)),
                    @Node(id = "8", coordinates = @Loc(value = TOUCHES_CORNER_BOTTOM_LEFT)),

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = TOP_LEFT), @Loc(value = TOP_RIGHT),
                            @Loc(value = BOTTOM_RIGHT),
                            @Loc(value = BOTTOM_LEFT) }, tags = { "building=yes" }),
                    @Area(id = "2", coordinates = { @Loc(value = TOUCHES_CORNER_TOP_LEFT),
                            @Loc(value = TOUCHES_CORNER_TOP_RIGHT),
                            @Loc(value = TOUCHES_CORNER_BOTTOM_RIGHT),
                            @Loc(value = TOUCHES_CORNER_BOTTOM_LEFT) }, tags = {
                                    "building=yes" }) })
    private Atlas polygonsTouchingAtVertex;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = CORNER_INTERSECT_TOP_LEFT)),
                    @Node(id = "6", coordinates = @Loc(value = CORNER_INTERSECT_TOP_RIGHT)),
                    @Node(id = "7", coordinates = @Loc(value = CORNER_INTERSECT_BOTTOM_RIGHT)),
                    @Node(id = "8", coordinates = @Loc(value = CORNER_INTERSECT_BOTTOM_LEFT)),

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = TOP_LEFT), @Loc(value = TOP_RIGHT),
                            @Loc(value = BOTTOM_RIGHT),
                            @Loc(value = BOTTOM_LEFT) }, tags = { "building=yes" }),
                    @Area(id = "2", coordinates = { @Loc(value = CORNER_INTERSECT_TOP_LEFT),
                            @Loc(value = CORNER_INTERSECT_TOP_RIGHT),
                            @Loc(value = CORNER_INTERSECT_BOTTOM_RIGHT),
                            @Loc(value = CORNER_INTERSECT_BOTTOM_LEFT) }, tags = {
                                    "building=yes" }) })
    private Atlas polygonsOverlappingAtCorner;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = SIDE_INTERSECT_TOP_LEFT)),
                    @Node(id = "6", coordinates = @Loc(value = SIDE_INTERSECT_TOP_RIGHT)),
                    @Node(id = "7", coordinates = @Loc(value = SIDE_INTERSECT_BOTTOM_RIGHT)),
                    @Node(id = "8", coordinates = @Loc(value = SIDE_INTERSECT_BOTTOM_LEFT)),

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = TOP_LEFT), @Loc(value = TOP_RIGHT),
                            @Loc(value = BOTTOM_RIGHT),
                            @Loc(value = BOTTOM_LEFT) }, tags = { "building=yes" }),
                    @Area(id = "2", coordinates = { @Loc(value = SIDE_INTERSECT_TOP_LEFT),
                            @Loc(value = SIDE_INTERSECT_TOP_RIGHT),
                            @Loc(value = SIDE_INTERSECT_BOTTOM_RIGHT),
                            @Loc(value = SIDE_INTERSECT_BOTTOM_LEFT) }, tags = {
                                    "building=yes" }) })
    private Atlas polygonsOverlappingAtSide;

    public Atlas getNonOverlappingNonTouchingPolygonsAtlas()
    {
        return this.nonOverlappingNonTouchingPolygons;
    }

    public Atlas getPolygonsOverlappingAtCornerAtlas()
    {
        return this.polygonsOverlappingAtCorner;
    }

    public Atlas getPolygonsOverlappingAtSideAtlas()
    {
        return this.polygonsOverlappingAtSide;
    }

    public Atlas getPolygonsSharingSideAtlas()
    {
        return this.polygonsSharingSide;
    }

    public Atlas getPolygonsStackedOnEachOtherAtlas()
    {
        return this.polygonsStackedOnEachOther;
    }

    public Atlas getPolygonsTouchingAtVertexAtlas()
    {
        return this.polygonsTouchingAtVertex;
    }

    public Atlas getPolygonWithinPolygonAtlas()
    {
        return this.polygonWithinPolygon;
    }

}
