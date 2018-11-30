package org.openstreetmap.atlas.geography.atlas.change.diff;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * @author lcram
 */
public class AtlasDiffTestRule extends CoreTestRule
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
    private static final String NINE = "15.4075,-61.3746";

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

                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            }

    )
    private Atlas differentNodeLocations1;

    @TestAtlas(

            nodes = {

                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = NINE), tags = { "tag1=value1" })

            }

    )
    private Atlas differentNodeLocations2;

    @TestAtlas(

            nodes = {

                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            },

            relations = {

                    @Relation(id = "31", tags = { "type=relation" }, members = {

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
    private Atlas differentParentRelations1;

    @TestAtlas(

            nodes = {

                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            },

            relations = {

                    @Relation(id = "31", tags = { "type=relation" }, members = {

                            @Member(id = "7", role = "a", type = "node")

                    }),

                    @Relation(id = "32", tags = { "type=relation" }, members = {

                            @Member(id = "8", role = "a", type = "node")

                    })

            }

    )
    private Atlas differentParentRelations2;

    public Atlas differentNodeLocations1()
    {
        return this.differentNodeLocations1;
    }

    public Atlas differentNodeLocations2()
    {
        return this.differentNodeLocations2;
    }

    public Atlas differentParentRelations1()
    {
        return this.differentParentRelations1;
    }

    public Atlas differentParentRelations2()
    {
        return this.differentParentRelations2;
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
