package org.openstreetmap.atlas.geography.atlas.routing;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test data for {@link AllPathsRouterTest}
 *
 * @author mgostintsev
 */
public class RoutingTestRule extends CoreTestRule
{
    private static final String ONE = "37.3022071, -121.8505286";
    private static final String TWO = "37.3012035, -121.8469065";
    private static final String THREE = "37.2956185, -121.8513284";
    private static final String FOUR = "37.3009167, -121.8553179";
    private static final String FIVE = "37.3013126, -121.8529148";
    private static final String SIX = "37.3035398, -121.8592693";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR))

            }, edges = {

                    @Edge(id = "315932590", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=primary" }),
                    @Edge(id = "-315932590", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=primary" }),
                    @Edge(id = "316932590", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE), }, tags = { "highway=primary" }),
                    @Edge(id = "-316932590", coordinates = { @Loc(value = THREE),
                            @Loc(value = TWO), }, tags = { "highway=primary" }),
                    @Edge(id = "317932590", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR), }, tags = { "highway=primary" }),
                    @Edge(id = "-317932590", coordinates = { @Loc(value = FOUR),
                            @Loc(value = THREE), }, tags = { "highway=primary" }),
                    @Edge(id = "318932590", coordinates = { @Loc(value = FOUR),
                            @Loc(value = ONE), }, tags = { "highway=primary" }),
                    @Edge(id = "-318932590", coordinates = { @Loc(value = ONE),
                            @Loc(value = FOUR), }, tags = { "highway=primary" }) })
    private Atlas biDirectionalCyclicalRouteAtlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),
                    @Node(id = "6", coordinates = @Loc(value = SIX))

            }, edges = {

                    @Edge(id = "315932590", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=primary" }),
                    @Edge(id = "316932590", coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE), }, tags = { "highway=primary" }),
                    @Edge(id = "317932590", coordinates = { @Loc(value = ONE),
                            @Loc(value = FOUR), }, tags = { "highway=primary" }),
                    @Edge(id = "318932590", coordinates = { @Loc(value = FIVE),
                            @Loc(value = SIX), }, tags = { "highway=primary" }) })
    private Atlas noPossibleRouteAtlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR))

            }, edges = {

                    @Edge(id = "315932590", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=primary" }),
                    @Edge(id = "316932590", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE), }, tags = { "highway=primary" }),
                    @Edge(id = "317932590", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR), }, tags = { "highway=primary" }) })
    private Atlas singleRouteAtlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),
                    @Node(id = "6", coordinates = @Loc(value = SIX))

            }, edges = {
                    @Edge(id = "314932590", coordinates = { @Loc(value = SIX),
                            @Loc(value = ONE), }, tags = { "highway=primary" }),
                    @Edge(id = "315932590", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=primary" }),
                    @Edge(id = "316932590", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE), }, tags = { "highway=primary" }),
                    @Edge(id = "317932590", coordinates = { @Loc(value = ONE),
                            @Loc(value = FOUR), }, tags = { "highway=primary" }),
                    @Edge(id = "318932590", coordinates = { @Loc(value = FOUR),
                            @Loc(value = THREE), }, tags = { "highway=primary" }),
                    @Edge(id = "319932590", coordinates = { @Loc(value = THREE),
                            @Loc(value = FIVE), }, tags = { "highway=primary" }) })
    private Atlas multipleRoutesAtlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR))

            }, edges = {

                    @Edge(id = "315932590", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=primary" }),
                    @Edge(id = "316932590", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE), }, tags = { "highway=primary" }),
                    @Edge(id = "317932590", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR), }, tags = { "highway=primary" }),
                    @Edge(id = "318932590", coordinates = { @Loc(value = FOUR),
                            @Loc(value = ONE), }, tags = { "highway=primary" }) })
    private Atlas cyclicalRouteAtlas;

    public Atlas getBiDirectionalCyclicRouteAtlas()
    {
        return this.biDirectionalCyclicalRouteAtlas;
    }

    public Atlas getCyclicalRouteAtlas()
    {
        return this.cyclicalRouteAtlas;
    }

    public Atlas getMultipleRoutesAtlas()
    {
        return this.multipleRoutesAtlas;
    }

    public Atlas getNoPossibleRouteAtlas()
    {
        return this.noPossibleRouteAtlas;
    }

    public Atlas getSingleRouteAtlas()
    {
        return this.singleRouteAtlas;
    }
}
