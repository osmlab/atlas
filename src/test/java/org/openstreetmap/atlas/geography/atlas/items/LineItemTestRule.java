package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class LineItemTestRule extends CoreTestRule
{
    private static final String ONE = "37.780574, -122.472852";
    private static final String TWO = "37.780592, -122.472242";
    private static final String THREE = "37.780724, -122.472249";
    private static final String FOUR = "37.780825, -122.471896";

    @TestAtlas(

            lines = {

                    @TestAtlas.Line(id = "1", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, tags = { "name=Linear" }),
                    @TestAtlas.Line(id = "2", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO), @TestAtlas.Loc(value = THREE),
                            @TestAtlas.Loc(value = FOUR),
                            @TestAtlas.Loc(value = ONE) }, tags = { "name=Loop" })

            }

    )
    private Atlas overallHeadingAtlas;

    public Atlas getoverallHeadingAtlas()
    {
        return this.overallHeadingAtlas;
    }
}
