package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * The Geojson representation of the below test atlas files is saved in the test/resources folder,
 * same package
 *
 * @author matthieun
 */
public class SnappedLineItemTestRule extends CoreTestRule
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

                    @TestAtlas.Node(id = "1", coordinates = @TestAtlas.Loc(value = ONE)),
                    @TestAtlas.Node(id = "2", coordinates = @TestAtlas.Loc(value = TWO)),
                    @TestAtlas.Node(id = "3", coordinates = @TestAtlas.Loc(value = THREE)),
                    @TestAtlas.Node(id = "4", coordinates = @TestAtlas.Loc(value = FOUR)),
                    @TestAtlas.Node(id = "5", coordinates = @TestAtlas.Loc(value = FIVE)),
                    @TestAtlas.Node(id = "6", coordinates = @TestAtlas.Loc(value = SIX)),
                    @TestAtlas.Node(id = "7", coordinates = @TestAtlas.Loc(value = SEVEN)),
                    @TestAtlas.Node(id = "8", coordinates = @TestAtlas.Loc(value = EIGHT))

            },

            edges = {

                    @TestAtlas.Edge(id = "1000000", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO) }, tags = { "highway=secondary" }),
                    @TestAtlas.Edge(id = "-1000000", coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = ONE) }, tags = { "highway=secondary" }),
                    @TestAtlas.Edge(id = "2000000", coordinates = { @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, tags = { "highway=secondary" }),
                    @TestAtlas.Edge(id = "3000000", coordinates = { @TestAtlas.Loc(value = THREE),
                            @TestAtlas.Loc(value = FOUR) }, tags = { "highway=secondary" }),
                    @TestAtlas.Edge(id = "4000000", coordinates = { @TestAtlas.Loc(value = FOUR),
                            @TestAtlas.Loc(value = FIVE) }, tags = { "highway=secondary" }),
                    @TestAtlas.Edge(id = "5000000", coordinates = { @TestAtlas.Loc(value = FIVE),
                            @TestAtlas.Loc(value = SIX) }, tags = { "highway=secondary" }),
                    @TestAtlas.Edge(id = "6000000", coordinates = { @TestAtlas.Loc(value = SIX),
                            @TestAtlas.Loc(value = SEVEN) }, tags = { "highway=secondary" }),
                    @TestAtlas.Edge(id = "7000000", coordinates = { @TestAtlas.Loc(value = SEVEN),
                            @TestAtlas.Loc(value = EIGHT) }, tags = { "highway=secondary" }),
                    @TestAtlas.Edge(id = "8000000", coordinates = { @TestAtlas.Loc(value = EIGHT),
                            @TestAtlas.Loc(value = ONE) }, tags = { "highway=secondary" })

            },

            areas = {

                    @TestAtlas.Area(id = "1", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = TWO_BIS) }, tags = { "landuse=residential" }),
                    @TestAtlas.Area(id = "2", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, tags = { "landuse=residential" })

            },

            lines = {

                    @TestAtlas.Line(id = "1", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = TWO_BIS) }, tags = { "power=line" }),
                    @TestAtlas.Line(id = "2", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, tags = { "power=line" })

            },

            points = {

                    @TestAtlas.Point(id = "1", coordinates = @TestAtlas.Loc(value = ONE)),
                    @TestAtlas.Point(id = "2", coordinates = @TestAtlas.Loc(value = TWO)),
                    @TestAtlas.Point(id = "3", coordinates = @TestAtlas.Loc(value = THREE)),
                    @TestAtlas.Point(id = "4", coordinates = @TestAtlas.Loc(value = FOUR)),
                    @TestAtlas.Point(id = "5", coordinates = @TestAtlas.Loc(value = FIVE)),
                    @TestAtlas.Point(id = "6", coordinates = @TestAtlas.Loc(value = SIX)),
                    @TestAtlas.Point(id = "7", coordinates = @TestAtlas.Loc(value = SEVEN)),
                    @TestAtlas.Point(id = "8", coordinates = @TestAtlas.Loc(value = EIGHT))

            },

            relations = {

                    @TestAtlas.Relation(id = "1", tags = { "type=relation" }, members = {

                            @TestAtlas.Relation.Member(id = "1000000", role = "a", type = "edge"),
                            @TestAtlas.Relation.Member(id = "3000000", role = "b", type = "edge")

                    }),

                    @TestAtlas.Relation(id = "2", tags = { "type=relation" }, members = {

                            @TestAtlas.Relation.Member(id = "8", role = "a", type = "point"),
                            @TestAtlas.Relation.Member(id = "1", role = "b", type = "area")

                    }),

                    @TestAtlas.Relation(id = "3", tags = { "type=relation" }, members = {

                            @TestAtlas.Relation.Member(id = "5000000", role = "a", type = "edge"),
                            @TestAtlas.Relation.Member(id = "6000000", role = "b", type = "edge"),
                            @TestAtlas.Relation.Member(id = "1", role = "c", type = "area")

                    })

            }

    )
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
