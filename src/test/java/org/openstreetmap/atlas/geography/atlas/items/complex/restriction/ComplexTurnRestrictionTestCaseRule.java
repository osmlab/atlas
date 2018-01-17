package org.openstreetmap.atlas.geography.atlas.items.complex.restriction;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * @author matthieun
 */
public class ComplexTurnRestrictionTestCaseRule extends CoreTestRule
{
    private static final String ONE = "37.780574, -122.472852";
    private static final String TWO = "37.780592, -122.472242";
    private static final String THREE = "37.780724, -122.472249";
    private static final String FOUR = "37.780716, -122.472395";
    private static final String FIVE = "37.780572, -122.472846";
    private static final String SIX = "37.780592, -122.472142";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = SIX)),

            }, edges = { @Edge(id = "102", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = { "highway=trunk" }), @Edge(id = "203", coordinates = { @Loc(value = TWO), @Loc(value = THREE) }, tags = { "highway=trunk" }), @Edge(id = "204", coordinates = { @Loc(value = TWO), @Loc(value = FOUR) }, tags = { "highway=trunk" }), @Edge(id = "205", coordinates = { @Loc(value = TWO), @Loc(value = SIX) }, tags = { "highway=trunk" })

            }, relations = {

                    @Relation(id = "1", tags = { "type=restriction",
                            "restriction=no_left_turn" }, members = {
                                    @Member(id = "102", role = "from", type = "edge"),
                                    @Member(id = "2", role = "via", type = "node"),
                                    @Member(id = "203", role = "to", type = "edge") })

            })
    private Atlas atlasNo;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE))

            }, edges = { @Edge(id = "102", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = { "highway=trunk" }), @Edge(id = "-102", coordinates = { @Loc(value = TWO), @Loc(value = ONE) }, tags = { "highway=trunk" }), @Edge(id = "203", coordinates = { @Loc(value = TWO), @Loc(value = THREE) }, tags = { "highway=trunk" }), @Edge(id = "204", coordinates = { @Loc(value = TWO), @Loc(value = FOUR) }, tags = { "highway=trunk" }), @Edge(id = "205", coordinates = { @Loc(value = TWO), @Loc(value = FIVE) }, tags = { "highway=trunk" })

            }, relations = {

                    @Relation(id = "1", tags = { "type=restriction",
                            "restriction=only_left_turn" }, members = {
                                    @Member(id = "102", role = "from", type = "edge"),
                                    @Member(id = "2", role = "via", type = "node"),
                                    @Member(id = "203", role = "to", type = "edge") })

            })
    private Atlas atlasOnly;

    @TestAtlas(loadFromTextResource = "bigNodeWithOnlyTurnRestrictions.txt.gz")
    private Atlas bigNodeWithOnlyTurnRestrictionsAtlas;

    public Atlas getAtlasNo()
    {
        return this.atlasNo;
    }

    public Atlas getAtlasOnly()
    {
        return this.atlasOnly;
    }

    public Atlas getBigNodeWithOnlyTurnRestrictionsAtlas()
    {
        return this.bigNodeWithOnlyTurnRestrictionsAtlas;
    }
}
