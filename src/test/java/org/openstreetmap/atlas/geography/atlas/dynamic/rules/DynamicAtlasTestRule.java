package org.openstreetmap.atlas.geography.atlas.dynamic.rules;

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
 * The Geojson representation of the below test atlas files is saved in the test/resources folder,
 * same package
 *
 * @author matthieun
 */
public class DynamicAtlasTestRule extends CoreTestRule
{
    // Inside 12-1350-1870
    private static final String ONE = "15.420563,-61.336198";
    private static final String TWO = "15.429499,-61.332850";
    private static final String TWO_BIS = "15.3907,-61.3112";

    // Inside 12-1350-1869
    private static final String THREE = "15.4855,-61.3041";
    private static final String FOUR = "15.4809,-61.3366";

    // Inside 12-1349-1869
    private static final String FIVE = "15.4852,-61.3816";
    private static final String SIX = "15.4781,-61.3949";

    // Inside 12-1349-1870
    private static final String SEVEN = "15.4145,-61.3826";
    private static final String EIGHT = "15.4073,-61.3749";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),
                    @Node(id = "6", coordinates = @Loc(value = SIX)),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN)),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT))

            },

            edges = {

                    @Edge(id = "1000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=secondary" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=secondary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=secondary" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=secondary" }),
                    @Edge(id = "4000000", coordinates = { @Loc(value = FOUR),
                            @Loc(value = FIVE) }, tags = { "highway=secondary" }),
                    @Edge(id = "5000000", coordinates = { @Loc(value = FIVE),
                            @Loc(value = SIX) }, tags = { "highway=secondary" }),
                    @Edge(id = "6000001", coordinates = { @Loc(value = SIX),
                            @Loc(value = SEVEN) }, tags = { "highway=secondary" }),
                    @Edge(id = "7000000", coordinates = { @Loc(value = SEVEN),
                            @Loc(value = EIGHT) }, tags = { "highway=secondary" }),
                    @Edge(id = "8000000", coordinates = { @Loc(value = EIGHT),
                            @Loc(value = ONE) }, tags = { "highway=secondary" })

            },

            areas = {

                    @Area(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = TWO_BIS) }, tags = { "landuse=residential" }),
                    @Area(id = "2", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "landuse=residential" })

            },

            lines = {

                    @Line(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = TWO_BIS) }, tags = { "power=line" }),
                    @Line(id = "2", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "power=line" })

            },

            points = {

                    @Point(id = "1", coordinates = @Loc(value = ONE)),
                    @Point(id = "2", coordinates = @Loc(value = TWO)),
                    @Point(id = "3", coordinates = @Loc(value = THREE)),
                    @Point(id = "4", coordinates = @Loc(value = FOUR)),
                    @Point(id = "5", coordinates = @Loc(value = FIVE)),
                    @Point(id = "6", coordinates = @Loc(value = SIX)),
                    @Point(id = "7", coordinates = @Loc(value = SEVEN)),
                    @Point(id = "8", coordinates = @Loc(value = EIGHT))

            },

            relations = {

                    @Relation(id = "1", tags = { "type=relation" }, members = {

                            @Member(id = "1000000", role = "a", type = "edge"),
                            @Member(id = "3000000", role = "b", type = "edge")

                    }),

                    @Relation(id = "2", tags = { "type=relation" }, members = {

                            @Member(id = "8", role = "a", type = "point"),
                            @Member(id = "1", role = "b", type = "area")

                    }),

                    @Relation(id = "3", tags = { "type=relation" }, members = {

                            @Member(id = "5000000", role = "a", type = "edge"),
                            @Member(id = "6000001", role = "b", type = "edge")

                    })

            }

    )
    private Atlas atlas;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT))

            },

            edges = {

                    @Edge(id = "1000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=secondary" }),
                    @Edge(id = "-1000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=secondary" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=secondary" }),
                    @Edge(id = "8000000", coordinates = { @Loc(value = EIGHT),
                            @Loc(value = ONE) }, tags = { "highway=secondary" })

            },

            areas = {

                    @Area(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = TWO_BIS) }, tags = { "landuse=residential" }),
                    @Area(id = "2", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "landuse=residential" })

            },

            lines = {

                    @Line(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = TWO_BIS) }, tags = { "power=line" }),
                    @Line(id = "2", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "power=line" })

            },

            points = {

                    @Point(id = "1", coordinates = @Loc(value = ONE)),
                    @Point(id = "2", coordinates = @Loc(value = TWO))

            },

            relations = {

                    @Relation(id = "1", tags = { "type=relation" }, members = {

                            @Member(id = "1000000", role = "a", type = "edge")

                    }),

                    @Relation(id = "2", tags = { "type=relation" }, members = {

                            @Member(id = "1", role = "b", type = "area")

                    }),

            }

    )
    private Atlas atlasz12x1350y1870;

    @TestAtlas(

            nodes = {

                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),

            },

            edges = {

                    @Edge(id = "2000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=secondary" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=secondary" }),
                    @Edge(id = "4000000", coordinates = { @Loc(value = FOUR),
                            @Loc(value = FIVE) }, tags = { "highway=secondary" })

            },

            areas = {

                    @Area(id = "2", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "landuse=residential" })

            },

            lines = {

                    @Line(id = "2", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "power=line" })

            },

            points = {

                    @Point(id = "3", coordinates = @Loc(value = THREE)),
                    @Point(id = "4", coordinates = @Loc(value = FOUR))

            },

            relations = {

                    @Relation(id = "1", tags = { "type=relation" }, members = {

                            @Member(id = "3000000", role = "b", type = "edge")

                    })

            }

    )
    private Atlas atlasz12x1350y1869;

    @TestAtlas(

            nodes = {

                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),
                    @Node(id = "6", coordinates = @Loc(value = SIX)),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN))

            },

            edges = {

                    @Edge(id = "4000000", coordinates = { @Loc(value = FOUR),
                            @Loc(value = FIVE) }, tags = { "highway=secondary" }),
                    @Edge(id = "5000000", coordinates = { @Loc(value = FIVE),
                            @Loc(value = SIX) }, tags = { "highway=secondary" }),
                    @Edge(id = "6000000", coordinates = { @Loc(value = SIX),
                            @Loc(value = SEVEN) }, tags = { "highway=secondary" })

            },

            points = {

                    @Point(id = "5", coordinates = @Loc(value = FIVE)),
                    @Point(id = "6", coordinates = @Loc(value = SIX))

            },

            relations = {

                    @Relation(id = "3", tags = { "type=relation" }, members = {

                            @Member(id = "5000000", role = "a", type = "edge"),
                            @Member(id = "6000000", role = "b", type = "edge")

                    })

            }

    )
    private Atlas atlasz12x1349y1869;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "6", coordinates = @Loc(value = SIX)),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN)),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT))

            },

            edges = {

                    @Edge(id = "6000000", coordinates = { @Loc(value = SIX),
                            @Loc(value = SEVEN) }, tags = { "highway=secondary" }),
                    @Edge(id = "7000000", coordinates = { @Loc(value = SEVEN),
                            @Loc(value = EIGHT) }, tags = { "highway=secondary" }),
                    @Edge(id = "8000000", coordinates = { @Loc(value = EIGHT),
                            @Loc(value = ONE) }, tags = { "highway=secondary" })

            },

            points = {

                    @Point(id = "7", coordinates = @Loc(value = SEVEN)),
                    @Point(id = "8", coordinates = @Loc(value = EIGHT))

            },

            relations = {

                    @Relation(id = "2", tags = { "type=relation" }, members = {

                            @Member(id = "8", role = "a", type = "point")

                    }),

                    @Relation(id = "3", tags = { "type=relation" }, members = {

                            @Member(id = "6000000", role = "b", type = "edge")

                    })

            }

    )
    private Atlas atlasz12x1349y1870;

    @TestAtlas(

            nodes = {

                    @Node(id = "10", coordinates = @Loc(value = ONE)),
                    @Node(id = "11", coordinates = @Loc(value = SIX)),
                    @Node(id = "12", coordinates = @Loc(value = SEVEN)),
                    @Node(id = "13", coordinates = @Loc(value = EIGHT))

            },

            relations = {

                    @Relation(id = "31", tags = { "type=relation" }, members = {

                            @Member(id = "11", role = "a", type = "node")

                    }),

                    @Relation(id = "32", tags = { "type=relation" }, members = {

                            @Member(id = "12", role = "a", type = "node"),

                            @Member(id = "13", role = "a", type = "node")

                    }),

                    @Relation(id = "33", tags = { "type=relation" }, members = {

                            @Member(id = "10", role = "a", type = "node"),

                            @Member(id = "11", role = "a", type = "node"),

                            @Member(id = "31", role = "b", type = "relation")

                    }),

                    @Relation(id = "34", tags = { "type=relation" }, members = {

                            @Member(id = "10", role = "a", type = "node"),

                            @Member(id = "13", role = "a", type = "node")

                    }),

                    @Relation(id = "35", tags = { "type=relation" }, members = {

                            @Member(id = "11", role = "a", type = "node"),

                            @Member(id = "12", role = "a", type = "node")

                    }),

                    @Relation(id = "36", tags = { "type=relation" }, members = {

                            @Member(id = "12", role = "a", type = "node"),

                            @Member(id = "34", role = "a", type = "relation")

                    }),

            }

    )
    private Atlas atlasForRelationsTest;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getAtlasForRelationsTest()
    {
        return this.atlasForRelationsTest;
    }

    public Atlas getAtlasz12x1349y1869()
    {
        return this.atlasz12x1349y1869;
    }

    public Atlas getAtlasz12x1349y1870()
    {
        return this.atlasz12x1349y1870;
    }

    public Atlas getAtlasz12x1350y1869()
    {
        return this.atlasz12x1350y1869;
    }

    public Atlas getAtlasz12x1350y1870()
    {
        return this.atlasz12x1350y1870;
    }
}
