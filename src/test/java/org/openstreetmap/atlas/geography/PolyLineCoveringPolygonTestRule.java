package org.openstreetmap.atlas.geography;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link PolyLineCoveringPolygonTest} data
 *
 * @author mgostintsev
 */
public class PolyLineCoveringPolygonTestRule extends CoreTestRule
{
    // Rectangle #1
    private static final String OUTER_TOP_LEFT = "39.9978684, 116.2741473";
    private static final String OUTER_TOP_RIGHT = "39.9979242, 116.2747418";
    private static final String OUTER_BOTTOM_RIGHT = "39.9975751, 116.2748088";
    private static final String OUTER_BOTTOM_LEFT = "39.9975152, 116.2741904";
    private static final String OUTER_ABOVE_BOTTOM_LEFT = "39.9976033, 116.2741749";

    // Polygon #2, fully contained within Rectangle #1
    private static final String INNER_TOP_LEFT = "39.9977887, 116.2743726";
    private static final String INNER_TOP_RIGHT = "39.9977948, 116.2744396";
    private static final String INNER_BOTTOM_RIGHT = "39.9977096, 116.2744528";

    // Three points, fully outside of Rectangle #1 and #2
    private static final String POINT_OUTSIDE_OUTER = "39.9977319, 116.2740253";
    private static final String SECOND_POINT_OUTSIDE_OUTER = "39.9976067, 116.2740461";
    private static final String THIRD_POINT_OUTSIDE_OUTER = "39.9980675, 116.274285";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = OUTER_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = OUTER_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = OUTER_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = OUTER_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = POINT_OUTSIDE_OUTER)),
                    @Node(id = "6", coordinates = @Loc(value = SECOND_POINT_OUTSIDE_OUTER)),

            }, edges = { @Edge(id = "159019301", coordinates = { @Loc(value = POINT_OUTSIDE_OUTER),
                    @Loc(value = SECOND_POINT_OUTSIDE_OUTER) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = {
                            @Loc(value = SECOND_POINT_OUTSIDE_OUTER),
                            @Loc(value = POINT_OUTSIDE_OUTER) }, tags = { "highway=tertiary" })

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_TOP_RIGHT), @Loc(value = OUTER_BOTTOM_RIGHT),
                            @Loc(value = OUTER_BOTTOM_LEFT) }, tags = { "building=yes" }) })
    private Atlas polyLinesFullyOutsidePolygon;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = OUTER_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = OUTER_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = OUTER_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = OUTER_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = INNER_TOP_LEFT)),
                    @Node(id = "6", coordinates = @Loc(value = SECOND_POINT_OUTSIDE_OUTER)),
                    @Node(id = "7", coordinates = @Loc(value = THIRD_POINT_OUTSIDE_OUTER)),

            }, edges = { @Edge(id = "159019301", coordinates = {
                    @Loc(value = THIRD_POINT_OUTSIDE_OUTER), @Loc(value = INNER_TOP_LEFT),
                    @Loc(value = SECOND_POINT_OUTSIDE_OUTER) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = {
                            @Loc(value = SECOND_POINT_OUTSIDE_OUTER), @Loc(value = INNER_TOP_LEFT),
                            @Loc(value = THIRD_POINT_OUTSIDE_OUTER) }, tags = {
                                    "highway=tertiary" })

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_TOP_RIGHT), @Loc(value = OUTER_BOTTOM_RIGHT),
                            @Loc(value = OUTER_BOTTOM_LEFT) }, tags = { "building=yes" }) })
    private Atlas polyLinesCuttingThroughPolygon;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = OUTER_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = OUTER_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = OUTER_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = OUTER_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = SECOND_POINT_OUTSIDE_OUTER)),

            }, edges = { @Edge(id = "159019301", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                    @Loc(value = SECOND_POINT_OUTSIDE_OUTER) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = {
                            @Loc(value = SECOND_POINT_OUTSIDE_OUTER),
                            @Loc(value = OUTER_TOP_LEFT) }, tags = { "highway=tertiary" })

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_TOP_RIGHT), @Loc(value = OUTER_BOTTOM_RIGHT),
                            @Loc(value = OUTER_BOTTOM_LEFT) }, tags = { "building=yes" }) })
    private Atlas polyLinesTouchingPolygonVertex;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = OUTER_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = OUTER_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = OUTER_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = OUTER_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = OUTER_ABOVE_BOTTOM_LEFT)),
                    @Node(id = "6", coordinates = @Loc(value = SECOND_POINT_OUTSIDE_OUTER))

            }, edges = {
                    @Edge(id = "159019301", coordinates = { @Loc(value = OUTER_ABOVE_BOTTOM_LEFT),
                            @Loc(value = SECOND_POINT_OUTSIDE_OUTER) }, tags = {
                                    "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = {
                            @Loc(value = SECOND_POINT_OUTSIDE_OUTER),
                            @Loc(value = OUTER_ABOVE_BOTTOM_LEFT) }, tags = { "highway=tertiary" })

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_TOP_RIGHT), @Loc(value = OUTER_BOTTOM_RIGHT),
                            @Loc(value = OUTER_BOTTOM_LEFT),
                            @Loc(OUTER_ABOVE_BOTTOM_LEFT) }, tags = { "building=yes" }) })
    private Atlas polyLinesTouchingPolygonBoundary;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = OUTER_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = OUTER_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = OUTER_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = OUTER_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = INNER_TOP_LEFT)),
                    @Node(id = "6", coordinates = @Loc(value = INNER_TOP_RIGHT)),
                    @Node(id = "7", coordinates = @Loc(value = INNER_BOTTOM_RIGHT)),
                    @Node(id = "8", coordinates = @Loc(value = POINT_OUTSIDE_OUTER)) }, edges = {
                            @Edge(id = "159019301", coordinates = { @Loc(value = INNER_TOP_LEFT),
                                    @Loc(value = INNER_TOP_RIGHT),
                                    @Loc(value = INNER_BOTTOM_RIGHT) }, tags = {
                                            "highway=tertiary" }),
                            @Edge(id = "-159019301", coordinates = {
                                    @Loc(value = INNER_BOTTOM_RIGHT), @Loc(value = INNER_TOP_RIGHT),
                                    @Loc(value = INNER_TOP_LEFT) }, tags = { "highway=tertiary" })

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_TOP_RIGHT), @Loc(value = OUTER_BOTTOM_RIGHT),
                            @Loc(value = OUTER_BOTTOM_LEFT) }, tags = { "building=yes" }) })
    private Atlas polyLinesFullyInsidePolygon;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = OUTER_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = OUTER_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = OUTER_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = OUTER_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = INNER_TOP_RIGHT)),
                    @Node(id = "6", coordinates = @Loc(value = SECOND_POINT_OUTSIDE_OUTER))

            }, edges = {
                    @Edge(id = "159019301", coordinates = {
                            @Loc(value = SECOND_POINT_OUTSIDE_OUTER),
                            @Loc(value = INNER_TOP_RIGHT) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = { @Loc(value = INNER_TOP_RIGHT),
                            @Loc(value = SECOND_POINT_OUTSIDE_OUTER) }, tags = {
                                    "highway=tertiary" })

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_TOP_RIGHT), @Loc(value = OUTER_BOTTOM_RIGHT),
                            @Loc(value = OUTER_BOTTOM_LEFT) }, tags = { "building=yes" }) })
    private Atlas polyLinesHalfwayInsidePolygon;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = OUTER_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = OUTER_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = OUTER_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = OUTER_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = OUTER_ABOVE_BOTTOM_LEFT))

            }, edges = {
                    @Edge(id = "159019301", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_ABOVE_BOTTOM_LEFT) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = { @Loc(value = OUTER_ABOVE_BOTTOM_LEFT),
                            @Loc(value = OUTER_TOP_LEFT) }, tags = { "highway=tertiary" })

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_TOP_RIGHT), @Loc(value = OUTER_BOTTOM_RIGHT),
                            @Loc(value = OUTER_BOTTOM_LEFT),
                            @Loc(OUTER_ABOVE_BOTTOM_LEFT) }, tags = { "building=yes" }) })
    private Atlas polyLinesAsPartOfPolygonBoundary;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = OUTER_TOP_LEFT)),
                    @Node(id = "2", coordinates = @Loc(value = OUTER_TOP_RIGHT)),
                    @Node(id = "3", coordinates = @Loc(value = OUTER_BOTTOM_RIGHT)),
                    @Node(id = "4", coordinates = @Loc(value = OUTER_BOTTOM_LEFT)),
                    @Node(id = "5", coordinates = @Loc(value = OUTER_ABOVE_BOTTOM_LEFT))

            }, edges = {
                    @Edge(id = "159019301", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_TOP_RIGHT), @Loc(value = OUTER_BOTTOM_RIGHT),
                            @Loc(value = OUTER_BOTTOM_LEFT), @Loc(value = OUTER_ABOVE_BOTTOM_LEFT),
                            @Loc(value = OUTER_TOP_LEFT) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_ABOVE_BOTTOM_LEFT), @Loc(value = OUTER_BOTTOM_LEFT),
                            @Loc(value = OUTER_BOTTOM_RIGHT), @Loc(value = OUTER_TOP_RIGHT),
                            @Loc(value = OUTER_TOP_LEFT) }, tags = { "highway=tertiary" })

            }, areas = {

                    @Area(id = "1", coordinates = { @Loc(value = OUTER_TOP_LEFT),
                            @Loc(value = OUTER_TOP_RIGHT), @Loc(value = OUTER_BOTTOM_RIGHT),
                            @Loc(value = OUTER_BOTTOM_LEFT),
                            @Loc(OUTER_ABOVE_BOTTOM_LEFT) }, tags = { "building=yes" }) })
    private Atlas polyLinesAsEntirePolygonBoundary;

    public Atlas getPolyLinesAsEntirePolygonBoundaryAtlas()
    {
        return this.polyLinesAsEntirePolygonBoundary;
    }

    public Atlas getPolyLinesAsPartOfPolygonBoundaryAtlas()
    {
        return this.polyLinesAsPartOfPolygonBoundary;
    }

    public Atlas getPolyLinesCuttingThroughPolygonAtlas()
    {
        return this.polyLinesCuttingThroughPolygon;
    }

    public Atlas getPolyLinesFullyInsidePolygonAtlas()
    {
        return this.polyLinesFullyInsidePolygon;
    }

    public Atlas getPolyLinesFullyOutsidePolygonAtlas()
    {
        return this.polyLinesFullyOutsidePolygon;
    }

    public Atlas getPolyLinesHalfwayInsidePolygonAtlas()
    {
        return this.polyLinesHalfwayInsidePolygon;
    }

    public Atlas getPolyLinesTouchingPolygonBoundaryAtlas()
    {
        return this.polyLinesTouchingPolygonBoundary;
    }

    public Atlas getPolyLinesTouchingPolygonVertexAtlas()
    {
        return this.polyLinesTouchingPolygonVertex;
    }

}
