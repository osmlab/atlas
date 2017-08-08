package org.openstreetmap.atlas.geography.atlas.walker;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * @author brian_l_davis
 */
public class OsmWayWalkerTestRule extends CoreTestRule
{
    private static final String FOUR = "37.780744, -122.471797";
    private static final String ONE = "37.780574, -122.472852";
    private static final String THREE = "37.780572, -122.472846";
    private static final String TWO = "37.780724, -122.472249";
    private static final String FIVE = "37.780724, -112.472249";

    @TestAtlas(loadFromTextResource = "OsmWayWalker-Way30647513.atlas.txt")
    private Atlas simpleNetwork;

    @TestAtlas(loadFromTextResource = "OsmWayWalker-Way169884263.atlas.txt")
    private Atlas roundAbout;

    @TestAtlas(

            nodes = { @TestAtlas.Node(id = "101740465", coordinates = @Loc(value = ONE)),
                    @TestAtlas.Node(id = "102740465", coordinates = @Loc(value = TWO)),
                    @TestAtlas.Node(id = "103740465", coordinates = @Loc(value = THREE)),
                    @TestAtlas.Node(id = "104740465", coordinates = @Loc(value = FOUR)),
                    @TestAtlas.Node(id = "105740465", coordinates = @Loc(value = FIVE)) },

            edges = {
                    @Edge(id = "1000001", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=secondary" }),
                    @Edge(id = "1000002", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=secondary" }),
                    @Edge(id = "1000003", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=secondary" }),
                    @Edge(id = "1000004", coordinates = { @Loc(value = FOUR),
                            @Loc(value = FIVE) }, tags = { "highway=secondary" }) })
    private Atlas isolatedEdge;

    public Atlas getIsolatedEdge()
    {
        return this.isolatedEdge;
    }

    public Atlas getRoundAbout()
    {
        return this.roundAbout;
    }

    public Atlas getSimpleNetwork()
    {
        return this.simpleNetwork;
    }
}
