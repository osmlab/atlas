package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author matthieun
 */
public class ChangeAtlasTestRule extends CoreTestRule
{
    private static final String ONE = "15.420563,-61.336198";
    private static final String TWO = "15.429499,-61.332850";
    private static final String THREE = "15.4855,-61.3041";
    private static final String FOUR = "15.4809,-61.3366";
    private static final String FIVE = "15.4852,-61.3816";
    private static final String SIX = "15.4781,-61.3949";
    private static final String SEVEN = "15.4145,-61.3826";
    private static final String EIGHT = "15.4073,-61.3749";
    private static final String NINE = "15.4075,-61.3746";
    private static final String TEN = "15.4081,-61.3741";
    private static final String ELEVEN = "15.4111,-62.3741";

    @TestAtlas(loadFromJosmOsmResource = "ChangeAtlasTest.josm.osm")
    private Atlas atlas;

    @TestAtlas(loadFromJosmOsmResource = "ChangeAtlasTestEdge.josm.osm")
    private Atlas atlasEdge;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE), tags = { "tag1=value1" }),
                    @Node(id = "2", coordinates = @Loc(value = TWO), tags = { "tag1=value1" }),
                    @Node(id = "3", coordinates = @Loc(value = THREE), tags = { "tag1=value1" }),
                    @Node(id = "4", coordinates = @Loc(value = FOUR), tags = { "tag1=value1" })

            },

            edges = {

                    @Edge(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=secondary" }),
                    @Edge(id = "-1", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=secondary" }),
                    @Edge(id = "2", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=secondary" }),
                    @Edge(id = "-2", coordinates = { @Loc(value = THREE),
                            @Loc(value = TWO) }, tags = { "highway=secondary" }),
                    @Edge(id = "3", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=secondary" }),
                    @Edge(id = "-3", coordinates = { @Loc(value = FOUR),
                            @Loc(value = THREE) }, tags = { "highway=secondary" })

            }

    )
    private Atlas differentNodeAndEdgeProperties1;

    @TestAtlas(

            nodes = {

                    @Node(id = "2", coordinates = @Loc(value = TWO), tags = { "tag1=value1" }),
                    @Node(id = "3", coordinates = @Loc(value = THREE), tags = { "tag1=value1" }),
                    @Node(id = "4", coordinates = @Loc(value = FOUR), tags = { "tag1=value1" })

            },

            edges = {

                    @Edge(id = "2", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=secondary" }),
                    @Edge(id = "-2", coordinates = { @Loc(value = THREE),
                            @Loc(value = TWO) }, tags = { "highway=secondary" }),
                    @Edge(id = "3", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=secondary" }),
                    @Edge(id = "-3", coordinates = { @Loc(value = FOUR),
                            @Loc(value = THREE) }, tags = { "highway=secondary" })

            }

    )
    private Atlas differentNodeAndEdgeProperties2;

    public Atlas differentNodeAndEdgeProperties1()
    {
        return this.differentNodeAndEdgeProperties1;
    }

    public Atlas differentNodeAndEdgeProperties2()
    {
        return this.differentNodeAndEdgeProperties2;
    }

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getAtlasEdge()
    {
        return this.atlasEdge;
    }
}
