package org.openstreetmap.atlas.geography.atlas.change.diff;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * @author lcram
 */
public class AtlasDiffTestRule extends CoreTestRule
{
    private static final String ONE = "15.420563,-61.336198";
    private static final String TWO = "15.429499,-61.332850";
    private static final String TWO_BIS = "15.3907,-61.3112";
    private static final String THREE = "15.4855,-61.3041";
    private static final String FOUR = "15.4809,-61.3366";
    private static final String FIVE = "15.4852,-61.3816";
    private static final String SIX = "15.4781,-61.3949";
    private static final String SEVEN = "15.4145,-61.3826";
    private static final String EIGHT = "15.4073,-61.3749";
    private static final String NINE = "15.4075,-61.3746";
    private static final String TEN = "15.4081,-61.3741";

    @TestAtlas(loadFromJosmOsmResource = "DiffAtlas1.josm.osm")
    private Atlas simpleAtlas1;

    @TestAtlas(loadFromJosmOsmResource = "DiffAtlas2.josm.osm")
    private Atlas simpleAtlas2;

    @TestAtlas(

            nodes = {

                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1",
                            "tag2=value2" })

            }

    )
    private Atlas differentTags1;

    @TestAtlas(

            nodes = {

                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1",
                            "tag2=value2" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1",
                            "tag2=value2" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            }

    )
    private Atlas differentTags2;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE), tags = { "tag1=value1" }),
                    @Node(id = "2", coordinates = @Loc(value = TWO), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" }),
                    @Node(id = "9", coordinates = @Loc(value = NINE), tags = { "tag1=value1" })

            },

            edges = {

                    @Edge(id = "99", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=secondary" }),
                    @Edge(id = "100", coordinates = { @Loc(value = SIX),
                            @Loc(value = SEVEN) }, tags = { "highway=secondary" }),
                    @Edge(id = "101", coordinates = { @Loc(value = SEVEN),
                            @Loc(value = EIGHT) }, tags = { "highway=secondary" })

            }

    )
    private Atlas differentNodeAndEdgeProperties1;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE), tags = { "tag1=value1" }),
                    @Node(id = "2", coordinates = @Loc(value = TWO), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" }),
                    @Node(id = "9", coordinates = @Loc(value = TEN), tags = { "tag1=value1" })

            },

            edges = {

                    @Edge(id = "99", coordinates = { @Loc(value = ONE), @Loc(value = THREE),
                            @Loc(value = TWO) }, tags = { "highway=secondary" }),
                    @Edge(id = "100", coordinates = { @Loc(value = SEVEN),
                            @Loc(value = EIGHT) }, tags = { "highway=secondary" }),
                    @Edge(id = "101", coordinates = { @Loc(value = SIX),
                            @Loc(value = SEVEN) }, tags = { "highway=secondary" })

            }

    )
    private Atlas differentNodeAndEdgeProperties2;

    @TestAtlas(

            areas = {

                    @Area(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "landuse=residential" })

            },

            lines = {

                    @Line(id = "1", coordinates = { @Loc(value = FOUR), @Loc(value = FIVE),
                            @Loc(value = SIX) }, tags = { "power=line" })

            },

            points = {

                    @Point(id = "7", coordinates = @Loc(value = SEVEN)),
                    @Point(id = "8", coordinates = @Loc(value = EIGHT))

            }

    )
    private Atlas differentPointLineArea1;

    @TestAtlas(

            areas = {

                    @Area(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = FOUR),
                            @Loc(value = THREE) }, tags = { "landuse=residential" })

            },

            lines = {

                    @Line(id = "1", coordinates = { @Loc(value = FOUR), @Loc(value = FIVE),
                            @Loc(value = SEVEN) }, tags = { "power=line" })

            },

            points = {

                    @Point(id = "7", coordinates = @Loc(value = NINE)),
                    @Point(id = "8", coordinates = @Loc(value = TEN))

            }

    )
    private Atlas differentPointLineArea2;

    @TestAtlas(

            nodes = {

                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            },

            lines = {

                    @Line(id = "6", coordinates = { @Loc(value = ONE), @Loc(value = TWO) })

            },

            relations = {

                    @Relation(id = "31", tags = { "type=relation" }, members = {

                            @Member(id = "5", role = "a", type = "node"),

                    }),

                    @Relation(id = "32", tags = { "type=relation" }, members = {

                            @Member(id = "7", role = "a", type = "node"),
                            @Member(id = "8", role = "a", type = "node")

                    }),

                    @Relation(id = "33", tags = { "type=relation" }, members = {

                            @Member(id = "6", role = "a", type = "node"),

                    })

            }

    )
    private Atlas differentRelations1;

    @TestAtlas(

            nodes = {

                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            },

            lines = {

                    @Line(id = "6", coordinates = { @Loc(value = ONE), @Loc(value = TWO) })

            },

            relations = {

                    @Relation(id = "31", tags = { "type=relation" }, members = {

                            @Member(id = "5", role = "a", type = "node"),
                            @Member(id = "7", role = "a", type = "node")

                    }),

                    @Relation(id = "32", tags = { "type=relation" }, members = {

                            @Member(id = "7", role = "a", type = "node"),

                    }),

                    @Relation(id = "33", tags = { "type=relation" }, members = {

                            @Member(id = "6", role = "b", type = "line"),

                    })

            }

    )
    private Atlas differentRelations2;

    @TestAtlas(

            nodes = {

                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            },

            relations = {

                    @Relation(id = "31", tags = { "type=relation" }, members = {

                            @Member(id = "5", role = "a", type = "node"),
                            @Member(id = "7", role = "a", type = "node")

                    }),

                    @Relation(id = "32", tags = { "type=relation" }, members = {

                            @Member(id = "7", role = "a", type = "node"),
                            @Member(id = "8", role = "a", type = "node")

                    }),

                    @Relation(id = "33", tags = { "type=relation" }, members = {

                            @Member(id = "6", role = "a", type = "node"),

                    })

            }

    )
    private Atlas differentRelations3;

    @TestAtlas(

            nodes = {

                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            },

            relations = {

                    @Relation(id = "31", tags = { "type=relation" }, members = {

                            @Member(id = "5", role = "b", type = "node"),
                            @Member(id = "7", role = "a", type = "node")

                    }),

                    @Relation(id = "32", tags = { "type=relation" }, members = {

                            @Member(id = "8", role = "a", type = "node")

                    })

            }

    )
    private Atlas differentRelations4;

    public Atlas differentNodeAndEdgeProperties1()
    {
        return this.differentNodeAndEdgeProperties1;
    }

    public Atlas differentNodeAndEdgeProperties2()
    {
        return this.differentNodeAndEdgeProperties2;
    }

    public Atlas differentPointLineArea1()
    {
        return this.differentPointLineArea1;
    }

    public Atlas differentPointLineArea2()
    {
        return this.differentPointLineArea2;
    }

    public Atlas differentRelations1()
    {
        return this.differentRelations1;
    }

    public Atlas differentRelations2()
    {
        return this.differentRelations2;
    }

    public Atlas differentRelations3()
    {
        return this.differentRelations3;
    }

    public Atlas differentRelations4()
    {
        return this.differentRelations4;
    }

    public Atlas differentTags1()
    {
        return this.differentTags1;
    }

    public Atlas differentTags2()
    {
        return this.differentTags2;
    }

    public Atlas simpleAtlas1()
    {
        return this.simpleAtlas1;
    }

    public Atlas simpleAtlas2()
    {
        return this.simpleAtlas2;
    }
}
