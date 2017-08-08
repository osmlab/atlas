package org.openstreetmap.atlas.geography.atlas.items.complex.boundaries;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * @author matthieun
 */
public class ComplexBoundaryTestRule extends CoreTestRule
{
    private static final String ONE = "19.2202841, -70.5305208";
    private static final String TWO = "19.2203577, -70.5309603";
    private static final String THREE = "19.2201964, -70.5305353";
    private static final String FOUR = "19.2202682, -70.5309606";
    private static final String FIVE = "19.2203771, -70.5310792";
    private static final String SIX = "19.2202916, -70.5310805";
    private static final String SEVEN = "19.2215199, -70.5307007";
    private static final String EIGHT = "19.2215288, -70.5307861";
    private static final String NINE = "19.2205727, -70.5322487";
    private static final String TEN = "19.220495, -70.5322645";
    private static final String ELEVEN = "19.2189432, -70.5311005";
    private static final String TWELVE = "19.2189336, -70.5310225";

    @TestAtlas(

            areas = {

                    @Area(id = "1", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "boundary=administrative",
                                    "admin_level=3", "name=state1" }),
                    @Area(id = "2", coordinates = { @Loc(value = FOUR), @Loc(value = FIVE),
                            @Loc(value = SIX) }, tags = { "boundary=administrative",
                                    "admin_level=4", "name=region2" }) },

            lines = {

                    @Line(id = "3", coordinates = { @Loc(value = SEVEN), @Loc(value = EIGHT),
                            @Loc(value = NINE) }),
                    @Line(id = "4", coordinates = { @Loc(value = NINE), @Loc(value = TEN),
                            @Loc(value = ELEVEN), @Loc(value = TWELVE), @Loc(value = SEVEN) }),
                    @Line(id = "7", coordinates = { @Loc(value = TEN), @Loc(value = ELEVEN),
                            @Loc(value = TWELVE), @Loc(value = SEVEN) })

            },

            relations = {

                    @Relation(id = "5", members = {
                            @Member(id = "2", role = "subarea", type = "area"),
                            @Member(id = "3", role = "outer", type = "line"),
                            @Member(id = "4", role = "outer", type = "line") }, tags = {
                                    "type=boundary", "boundary=administrative", "admin_level=3",
                                    "name=state5" }),
                    @Relation(id = "6", members = {
                            @Member(id = "1", role = "outer", type = "area"),
                            @Member(id = "5", role = "subarea", type = "relation") }, tags = {
                                    "type=boundary", "boundary=administrative", "admin_level=2",
                                    "name=country6" }),
                    // Line 3 and 7 do not match, hence relation 8 and parent 9 should be invalid.
                    @Relation(id = "8", members = {
                            @Member(id = "3", role = "outer", type = "line"),
                            @Member(id = "7", role = "outer", type = "line") }, tags = {
                                    "type=boundary", "boundary=administrative", "admin_level=5",
                                    "name=city8" }),
                    @Relation(id = "9", members = {
                            @Member(id = "8", role = "subarea", type = "relation"),
                            @Member(id = "3", role = "outer", type = "line"),
                            @Member(id = "4", role = "outer", type = "line") }, tags = {
                                    "type=boundary", "boundary=administrative", "admin_level=4",
                                    "name=county9" })

            }

    )
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
