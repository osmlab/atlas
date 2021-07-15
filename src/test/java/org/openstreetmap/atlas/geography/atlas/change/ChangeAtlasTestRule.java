package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * @author matthieun
 */
public class ChangeAtlasTestRule extends CoreTestRule
{
    private static final String ONE = "15.420563,-61.336198";
    private static final String TWO = "15.429499,-61.332850";
    private static final String THREE = "15.4855,-61.3041";
    private static final String FOUR = "15.4809,-61.3366";

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

    @TestAtlas(

            points = {

                    @Point(id = "1", coordinates = @Loc(value = ONE), tags = { "a=1", "b=2" })

            }

    )
    private Atlas tagAtlas;

    @TestAtlas(

            points = {

                    @Point(id = "1", coordinates = @Loc(value = ONE), tags = { "a=1", "b=2" }),
                    @Point(id = "2", coordinates = @Loc(value = TWO), tags = { "a=1", "b=2" }),
                    @Point(id = "3", coordinates = @Loc(value = THREE), tags = { "a=1", "b=2" }),
                    @Point(id = "4", coordinates = @Loc(value = FOUR), tags = { "a=1", "b=2" })

            },

            relations = {

                    @Relation(id = "1", tags = { "type=relation" }, members = {

                            @Member(id = "1", role = "b", type = "point"),
                            @Member(id = "2", role = "b", type = "point"),
                            @Member(id = "3", role = "b", type = "point"),
                            @Member(id = "4", role = "b", type = "point"),

                    })

            }

    )
    private Atlas pointAtlas;

    @TestAtlas(points = { @Point(id = "1000000", coordinates = @Loc(value = ONE)),
            @Point(id = "2000000", coordinates = @Loc(value = TWO)),
            @Point(id = "3000000", coordinates = @Loc(value = THREE)) }, lines = {
                    @Line(id = "1000000", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "name=Something" }) })
    private Atlas geometryChangeAtlas;

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

    /**
     * Get an atlas designed to test geometry changes
     *
     * @return The geometry atlas
     */
    public Atlas getGeometryChangeAtlas()
    {
        return this.geometryChangeAtlas;
    }

    public Atlas getPointAtlas()
    {
        return this.pointAtlas;
    }

    public Atlas getTagAtlas()
    {
        return this.tagAtlas;
    }
}
