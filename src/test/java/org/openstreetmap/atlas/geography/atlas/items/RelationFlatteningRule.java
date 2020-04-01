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
    @TestAtlas(nodes = { @Node(id = "1"), @Node(id = "2"), @Node(id = "3"), @Node(id = "4"),
            @Node(id = "5"), @Node(id = "6") }, relations = {

                    @Relation(id = "6", members = {
                            @Member(id = "1", role = "outside", type = "node") }),
                    @Relation(id = "7", members = {
                            @Member(id = "1", role = "outside", type = "node"),
                            @Member(id = "2", role = "outside", type = "node"),
                            @Member(id = "6", role = "outside", type = "node") }),
                    @Relation(id = "8", members = {
                            @Member(id = "4", role = "outside", type = "node"),
                            @Member(id = "5", role = "outside", type = "node"),
                            @Member(id = "8", role = "outside", type = "relation") }),
                    @Relation(id = "9", members = {
                            @Member(id = "4", role = "outside", type = "node"),
                            @Member(id = "5", role = "outside", type = "node"),
                            @Member(id = "7", role = "outside", type = "relation"),
                            @Member(id = "6", role = "outside", type = "relation") }),
                    @Relation(id = "10", members = {
                            @Member(id = "6", role = "outside", type = "relation"),
                            @Member(id = "3", role = "outside", type = "node"),
                            @Member(id = "9", role = "outside", type = "relation") }),
                    @Relation(id = "2", members = {
                            @Member(id = "3", role = "outside", type = "node"),
                            @Member(id = "4", role = "outside", type = "node"),
                            @Member(id = "6", role = "outside", type = "relation") }),
                    @Relation(id = "1", members = {
                            @Member(id = "5", role = "outside", type = "node"),
                            @Member(id = "2", role = "outside", type = "node"),
                            @Member(id = "2", role = "outside", type = "relation") }) })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

}
