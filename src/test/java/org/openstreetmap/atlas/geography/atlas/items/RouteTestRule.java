package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link RouteTest} test data.
 *
 * @author mgostintsev
 */
public class RouteTestRule extends CoreTestRule
{
    private static final String ONE = "39.9970447, 116.279489";
    private static final String TWO = "39.9974907, 116.2835146";
    private static final String THREE = "39.9976118, 116.2836054";
    private static final String FOUR = "40.0007062, 116.2829521";
    private static final String FIVE = "40.0082021, 116.2824999";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),

            }, edges = {

                    @Edge(id = "159019301", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary" }),
                    @Edge(id = "128620751", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "128620796", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=tertiary", "oneway=yes" }),
                    @Edge(id = "138620888", coordinates = { @Loc(value = THREE),
                            @Loc(value = FIVE) }, tags = { "highway=tertiary", "oneway=yes" }) })
    private Atlas atlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),

            }, edges = {

                    @Edge(id = "206786592000008", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-206786592000008", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=tertiary" }),
                    @Edge(id = "206786592000007", coordinates = { @Loc(value = THREE),
                            @Loc(value = ONE) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "-206786592000007", coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE) }, tags = { "highway=tertiary_link" }) })
    private Atlas routeHashCodeAtlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),

            }, edges = {

                    @Edge(id = "159019301", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary" }),
                    @Edge(id = "-159019301", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=tertiary" }),
                    @Edge(id = "128620751", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "-128620751", coordinates = { @Loc(value = THREE),
                            @Loc(value = TWO) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "128620796", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=tertiary", "oneway=yes" }),
                    @Edge(id = "-128620796", coordinates = { @Loc(value = FOUR),
                            @Loc(value = THREE) }, tags = { "highway=tertiary", "oneway=yes" }),
                    @Edge(id = "138620888", coordinates = { @Loc(value = FOUR),
                            @Loc(value = FIVE) }, tags = { "highway=tertiary", "oneway=yes" }),
                    @Edge(id = "-138620888", coordinates = { @Loc(value = FIVE),
                            @Loc(value = FOUR) }, tags = { "highway=tertiary", "oneway=yes" }),
                    @Edge(id = "138620889", coordinates = { @Loc(value = TWO),
                            @Loc(value = FIVE) }, tags = { "highway=tertiary", "oneway=yes" }) })

    private Atlas uTurnAtlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getRouteHashCodeAtlas()
    {
        return this.routeHashCodeAtlas;
    }

    public Atlas getUTurnAtlas()
    {
        return this.uTurnAtlas;
    }
}
