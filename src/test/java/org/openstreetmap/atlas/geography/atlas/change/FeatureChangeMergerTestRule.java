package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

public class FeatureChangeMergerTestRule extends CoreTestRule
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

    @TestAtlas(

            nodes = {

                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            },

            lines = {

                    @Line(id = "12", coordinates = { @Loc(value = ONE), @Loc(value = TWO) })

            },

            relations = {

                    @Relation(id = "31", tags = { "type=relation" }, members = {

                            @Member(id = "5", role = "a", type = "node"),
                            @Member(id = "5", role = "a", type = "node"),
                            @Member(id = "5", role = "a", type = "node"),
                            @Member(id = "6", role = "b", type = "node"),

                    }),

            }

    )
    private Atlas atlas1;

    public Atlas atlas1()
    {
        return this.atlas1;
    }
}
