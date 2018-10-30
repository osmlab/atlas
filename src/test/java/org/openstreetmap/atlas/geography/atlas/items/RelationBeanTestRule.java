package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Test Rule for {@link RelationBeanTest}
 *
 * @author jklamer
 */
public class RelationBeanTestRule extends CoreTestRule
{

    @TestAtlas(nodes = { @Node(id = "1"), @Node(id = "2"), @Node(id = "3"), @Node(id = "4"),
            @Node(id = "5"), @Node(id = "6") }, relations = {
                    @Relation(id = "1", members = {
                            @Member(id = "1", role = "outside", type = "node") }),
                    @Relation(id = "2", members = {
                            @Member(id = "1", role = "inside", type = "node"),
                            @Member(id = "2", role = "outside", type = "node"),
                            @Member(id = "6", role = "outside", type = "node") }),
                    @Relation(id = "3", members = {
                            @Member(id = "3", role = "outside", type = "node"),
                            @Member(id = "4", role = "front side", type = "node"),
                            @Member(id = "5", role = "outside", type = "node") }) })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
