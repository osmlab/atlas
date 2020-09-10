package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class AtlasDiffCommandTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "AtlasDiffCommandAtlas1.josm.osm")
    private Atlas atlas1;

    @TestAtlas(loadFromJosmOsmResource = "AtlasDiffCommandAtlas2.josm.osm")
    private Atlas atlas2;

    @TestAtlas(loadFromJosmOsmResource = "AtlasDiffCommandAtlas3.josm.osm")
    private Atlas atlas3;

    @TestAtlas(loadFromJosmOsmResource = "AtlasDiffCommandAtlas4.josm.osm")
    private Atlas atlas4;

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
}
