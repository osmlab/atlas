package org.openstreetmap.atlas.geography.atlas.complete;

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
 * @author matthieun
 */
public class CompleteTestRule extends CoreTestRule
{
    public static final String POINT_1_LOCATION = "37.331417,-122.0304871";
    public static final String POINT_2_LOCATION = "37.333364,-122.0200268";

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

    @TestAtlas(

            nodes = { @Node(id = "1", coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = "2", coordinates = @Loc(value = POINT_2_LOCATION)) },

            edges = { @Edge(id = "3", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION) }) },

            areas = { @Area(id = "27", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION) }) },

            lines = { @Line(id = "18", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION) }) },

            points = { @Point(id = "33", coordinates = @Loc(value = POINT_1_LOCATION)) },

            relations = { @Relation(id = "22", members = {
                    @Member(id = "1", type = "node", role = "node role"),
                    @Member(id = "3", type = "edge", role = "edge role") }) }

    )
    private Atlas atlas;

    @TestAtlas(

            points = {

                    @Point(id = "1", coordinates = @Loc(value = ONE)),
                    @Point(id = "2", coordinates = @Loc(value = TWO)),
                    @Point(id = "3", coordinates = @Loc(value = THREE)),
                    @Point(id = "4", coordinates = @Loc(value = FOUR)),
                    @Point(id = "5", coordinates = @Loc(value = FIVE)),
                    @Point(id = "6", coordinates = @Loc(value = SIX)),
                    @Point(id = "7", coordinates = @Loc(value = SEVEN))

            },

            relations = {

                    @Relation(id = "1", tags = { "type=relation" }, members = {

                            @Member(id = "1", role = "a", type = "point"),
                            @Member(id = "2", role = "a", type = "point"),
                            @Member(id = "3", role = "a", type = "point")

                    })

            }

    )
    private Atlas atlas2;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getAtlas2()
    {
        return this.atlas2;
    }
}
