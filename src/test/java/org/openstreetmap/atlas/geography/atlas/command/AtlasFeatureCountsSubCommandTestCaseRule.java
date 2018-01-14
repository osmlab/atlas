package org.openstreetmap.atlas.geography.atlas.command;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test Fixture for the AtlasFeatureCountsSubCommandTestCase class
 *
 * @author cstaylor
 */
public class AtlasFeatureCountsSubCommandTestCaseRule extends CoreTestRule
{
    private static final String ONE = "37.33,-122.00";
    private static final String TWO = "37.33,-122.03";
    private static final String THREE = "37.32,-122.03";
    private static final String FOUR = "37.32,-122.00";

    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)) }, iso = "JPN")
    private Atlas firstAtlas;

    @TestAtlas(nodes = { @Node(id = "2", coordinates = @Loc(value = TWO)) }, iso = "JPN")
    private Atlas secondAtlas;

    @TestAtlas(nodes = { @Node(id = "3", coordinates = @Loc(value = THREE)) }, iso = "JPN")
    private Atlas thirdAtlas;

    @TestAtlas(nodes = { @Node(id = "4", coordinates = @Loc(value = FOUR)) }, iso = "JPN")
    private Atlas fourthAtlas;

    public Atlas getFirstAtlas()
    {
        return this.firstAtlas;
    }

    public Atlas getFourthAtlas()
    {
        return this.fourthAtlas;
    }

    public Atlas getSecondAtlas()
    {
        return this.secondAtlas;
    }

    public Atlas getThirdAtlas()
    {
        return this.thirdAtlas;
    }
}
