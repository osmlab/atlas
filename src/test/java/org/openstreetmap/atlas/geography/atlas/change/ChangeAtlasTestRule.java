package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

public class ChangeAtlasTestRule extends CoreTestRule
{
    // Inside 12-1350-1870
    private static final String ONE = "15.420563,-61.336198";
    private static final String TWO = "15.429499,-61.332850";

    // Inside 12-1350-1869
    private static final String THREE = "15.4855,-61.3041";
    private static final String FOUR = "15.4809,-61.3366";

    // Inside 12-1349-1869
    private static final String FIVE = "15.4852,-61.3816";
    private static final String SIX = "15.4781,-61.3949";

    // Inside 12-1349-1870
    private static final String SEVEN = "15.4145,-61.3826";
    private static final String EIGHT = "15.4073,-61.3749";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),
                    @Node(id = "6", coordinates = @Loc(value = SIX)),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN)),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT))

            }

    )
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
