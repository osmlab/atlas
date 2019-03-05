package org.openstreetmap.atlas.geography.atlas;

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
public class AtlasTestRule extends CoreTestRule {
    private static final String ONE = "37.780574, -122.472852";
    private static final String TWO = "37.780592, -122.472242";
    private static final String THREE = "37.780724, -122.472249";
    private static final String FOUR = "37.780825, -122.471896";

    @TestAtlas(
       nodes = {

                    @Node(id = "0", coordinates = @Loc(value = FOUR)),
                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE))

            }, edges = {

            @Edge(id = "0", coordinates = {@Loc(value = ONE), @Loc(value = TWO)}),
            @Edge(id = "1", coordinates = {@Loc(value = TWO), @Loc(value = THREE)})

    }, areas = {

            @Area(id = "0", coordinates = {@Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE)}),
            @Area(id = "1", coordinates = {@Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR)})

    }, lines = {

            @Line(id = "0", coordinates = {@Loc(value = ONE), @Loc(value = TWO)}),
            @Line(id = "1", coordinates = {@Loc(value = TWO), @Loc(value = FOUR)})

    }, points = {

            @Point(id = "0", coordinates = @Loc(value = ONE)),
            @Point(id = "1", coordinates = @Loc(value = TWO)),
            @Point(id = "2", coordinates = @Loc(value = THREE)),
            @Point(id = "3", coordinates = @Loc(value = FOUR))

    }, relations = {

            @Relation(id = "0", members = {
                    @Member(id = "0", role = "from", type = "edge"),
                    @Member(id = "2", role = "via", type = "node"),
                    @Member(id = "1", role = "to", type = "edge")}),
            @Relation(id = "1", members = {
                    @Member(id = "0", role = "inside", type = "area"),
                    @Member(id = "1", role = "outside", type = "line")}),

    })
    private Atlas atlas;

    public Atlas getAtlas() {
        return this.atlas;
    }
}
