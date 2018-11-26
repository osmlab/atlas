package org.openstreetmap.atlas.geography.atlas.walker;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Unit test rule for {@link SimpleEdgeWalker}.
 *
 * @author bbreithaupt
 */
public class SimpleEdgeWalkerTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.9870531970049,-122.885726828789";
    private static final String TEST_2 = "47.9879410278085,-122.885085090084";
    private static final String TEST_3 = "47.9877865096808,-122.886904654096";

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_3)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1000000001", coordinates = {
                            @TestAtlas.Loc(value = TEST_1),
                            @TestAtlas.Loc(value = TEST_2) }, tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "1001000001", coordinates = {
                            @TestAtlas.Loc(value = TEST_2),
                            @TestAtlas.Loc(value = TEST_3) }, tags = { "highway=primary" }),
                    @TestAtlas.Edge(id = "1002000001", coordinates = {
                            @TestAtlas.Loc(value = TEST_3),
                            @TestAtlas.Loc(value = TEST_1) }, tags = { "highway=motorway" }) })
    private Atlas motorwayPrimaryTriangleAtlas;

    public Atlas motorwayPrimaryTriangleAtlas()
    {
        return this.motorwayPrimaryTriangleAtlas;
    }
}
