package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * @author samuelgass
 */
public class RelationFlatteningRule extends CoreTestRule
{

    @TestAtlas(

            nodes = { @Node(id = "1"), @Node(id = "2"), @Node(id = "3"), @Node(id = "4"),
                    @Node(id = "5") },

            relations = {

                    @Relation(id = "6", members = {
                            @Member(id = "1", role = "outside", type = "node") }),
                    @Relation(id = "7", members = {
                            @Member(id = "1", role = "outside", type = "node"),
                            @Member(id = "2", role = "outside", type = "node") }),
                    @Relation(id = "8", members = {
                            @Member(id = "4", role = "outside", type = "node"),
                            @Member(id = "5", role = "outside", type = "node"),
                            @Member(id = "8", role = "outside", type = "relation") }) }

    )
    private Atlas atlas1;

    public Atlas getAtlas1()
    {
        return this.atlas1;
    }

}
