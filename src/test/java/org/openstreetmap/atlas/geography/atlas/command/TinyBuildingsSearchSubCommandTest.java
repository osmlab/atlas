package org.openstreetmap.atlas.geography.atlas.command;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.command.buildings.TinyBuildingsSearchSubCommand;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuilding;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuildingFinder;

/**
 * @author jamesgage
 */
public class TinyBuildingsSearchSubCommandTest
{
    @Rule
    public final ComplexBuildingsTestRule setup = new ComplexBuildingsTestRule();

    @Test
    public void testTooSmall()
    {
        final TinyBuildingsSearchSubCommand command = new TinyBuildingsSearchSubCommand();
        final Atlas stadiumAtlas = this.setup.getStadiumAtlas();
        final ComplexBuildingFinder finder = new ComplexBuildingFinder();
        final ComplexBuilding complexBuilding = finder.find(stadiumAtlas).iterator().next();
        Assert.assertFalse(command.tooSmall(complexBuilding));

    }
}
