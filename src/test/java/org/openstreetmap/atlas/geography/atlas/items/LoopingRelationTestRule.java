package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * @author matthieun
 */
public class LoopingRelationTestRule extends CoreTestRule
{
    private static final String ONE = "37.780574, -122.472852";
    private static final String TWO = "37.780592, -122.472242";
    private static final String THREE = "37.780724, -122.472249";
    private static final String FOUR = "37.780825, -122.471896";

    @TestAtlas(

            areas = {

                    @Area(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "name=First" })

            },

            relations = {

                    @Relation(id = "3", tags = { "type=outside" }, members = {
                            @Member(id = "1", role = "outside", type = "area") }),
                    @Relation(id = "4", tags = { "type=inside" }, members = {
                            @Member(id = "1", role = "outside", type = "area"),
                            @Member(id = "3", role = "outside", type = "relation") })

            }

    )
    private Atlas atlas1;

    @TestAtlas(

            areas = {

                    @Area(id = "2", coordinates = { @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "name=Second" })

            },

            relations = {

                    @Relation(id = "4", tags = { "type=inside" }, members = {
                            @Member(id = "2", role = "outside", type = "area") }),
                    @Relation(id = "3", tags = { "type=outside" }, members = {
                            @Member(id = "2", role = "outside", type = "area"),
                            @Member(id = "4", role = "outside", type = "relation") })

            }

    )
    private Atlas atlas2;

    public Atlas getAtlas1()
    {
        return this.atlas1;
    }

    public Atlas getAtlas2()
    {
        return this.atlas2;
    }
}
