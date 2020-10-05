package org.openstreetmap.atlas.geography.atlas.command;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author jamesgage
 */
public class ComplexBuildingsTestRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "complex-SF.txt")
    private Atlas stadiumAtlas;

    public Atlas getStadiumAtlas()
    {
        return this.stadiumAtlas;
    }
}
