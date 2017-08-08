package org.openstreetmap.atlas.geography.atlas.packed;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.RelationTypeTag;
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
public class PackedRelationTestCaseRule extends CoreTestRule
{
    private static final String RELATION_IDENTIFIER_VALUE = "1";
    public static final long RELATION_IDENTIFIER = Long.parseLong(RELATION_IDENTIFIER_VALUE);

    private static final String ONE = "37.780574, -122.472852";
    private static final String TWO = "37.780592, -122.472242";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO))

            }, edges = {

                    @Edge(id = "102", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=trunk" })

            }, relations = {

                    @Relation(id = RELATION_IDENTIFIER_VALUE, tags = { "type=restriction",
                            "restriction=no_left_turn" }, members = {
                                    @Member(id = "102", role = RelationTypeTag.RESTRICTION_ROLE_FROM, type = "edge"),
                                    @Member(id = "2", role = RelationTypeTag.RESTRICTION_ROLE_VIA, type = "node"),
                                    @Member(id = "102", role = RelationTypeTag.RESTRICTION_ROLE_TO, type = "edge") })

            })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
