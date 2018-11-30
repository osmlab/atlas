package org.openstreetmap.atlas.geography.atlas.change.diff;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author lcram
 */
public class AtlasDiffTestRule extends CoreTestRule
{
    // Inside 12-1350-1870
    private static final String ONE = "15.420563,-61.336198";
    private static final String TWO = "15.429499,-61.332850";
    private static final String TWO_BIS = "15.3907,-61.3112";

    // Inside 12-1350-1869
    private static final String THREE = "15.4855,-61.3041";
    private static final String FOUR = "15.4809,-61.3366";

    // Inside 12-1349-1869
    private static final String FIVE = "15.4852,-61.3816";
    private static final String SIX = "15.4781,-61.3949";

    // Inside 12-1349-1870
    private static final String SEVEN = "15.4145,-61.3826";
    private static final String EIGHT = "15.4073,-61.3749";
    private static final String NINE = "15.4075,-61.3746";

    @TestAtlas(loadFromJosmOsmResource = "DiffAtlas1.josm.osm")
    private Atlas atlas1;

    @TestAtlas(loadFromJosmOsmResource = "DiffAtlas2.josm.osm")
    private Atlas atlas2;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE), tags = { "tag1=value1" }),
                    @Node(id = "2", coordinates = @Loc(value = TWO), tags = { "tag1=value1" }),
                    @Node(id = "3", coordinates = @Loc(value = THREE), tags = { "tag1=value1" }),
                    @Node(id = "4", coordinates = @Loc(value = FOUR), tags = { "tag1=value1" }),
                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1",
                            "tag2=value2" })

            })
    private Atlas atlas3;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE), tags = { "tag1=value1" }),
                    @Node(id = "2", coordinates = @Loc(value = TWO), tags = { "tag1=value1" }),
                    @Node(id = "3", coordinates = @Loc(value = THREE), tags = { "tag1=value1",
                            "tag2=value2" }),
                    @Node(id = "4", coordinates = @Loc(value = FOUR), tags = { "tag1=value1" }),
                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1",
                            "tag2=value2" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            })
    private Atlas atlas4;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE), tags = { "tag1=value1" }),
                    @Node(id = "2", coordinates = @Loc(value = TWO), tags = { "tag1=value1" }),
                    @Node(id = "3", coordinates = @Loc(value = THREE), tags = { "tag1=value1" }),
                    @Node(id = "4", coordinates = @Loc(value = FOUR), tags = { "tag1=value1" }),
                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT), tags = { "tag1=value1" })

            })
    private Atlas atlas5;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE), tags = { "tag1=value1" }),
                    @Node(id = "2", coordinates = @Loc(value = TWO), tags = { "tag1=value1" }),
                    @Node(id = "3", coordinates = @Loc(value = THREE), tags = { "tag1=value1" }),
                    @Node(id = "4", coordinates = @Loc(value = FOUR), tags = { "tag1=value1" }),
                    @Node(id = "5", coordinates = @Loc(value = FIVE), tags = { "tag1=value1" }),
                    @Node(id = "6", coordinates = @Loc(value = SIX), tags = { "tag1=value1" }),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN), tags = { "tag1=value1" }),
                    @Node(id = "8", coordinates = @Loc(value = NINE), tags = { "tag1=value1" })

            })
    private Atlas atlas6;

    public Atlas getAtlas1()
    {
        return this.atlas1;
    }

    public Atlas getAtlas2()
    {
        return this.atlas2;
    }

    public Atlas getAtlas3()
    {
        return this.atlas3;
    }

    public Atlas getAtlas4()
    {
        return this.atlas4;
    }

    public Atlas getAtlas5()
    {
        return this.atlas5;
    }

    public Atlas getAtlas6()
    {
        return this.atlas6;
    }
}
