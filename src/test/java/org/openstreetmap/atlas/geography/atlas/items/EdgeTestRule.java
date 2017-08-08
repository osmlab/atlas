package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author matthieun
 */
public class EdgeTestRule extends CoreTestRule
{
    private static final String ONE = "37.780574, -122.472852";
    private static final String TWO = "37.780592, -122.472242";

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO))

            }, edges = {

                    @Edge(id = "6", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=trunk", "maxspeed:forward=60" }),
                    @Edge(id = "-6", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=trunk", "maxspeed:forward=60" }),
                    @Edge(id = "7", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=trunk", "maxspeed:backward=70" }),
                    @Edge(id = "-7", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=trunk",
                                    "maxspeed:backward=70" }),
                    @Edge(id = "8", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=trunk", "maxspeed=80" }),
                    @Edge(id = "-8", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=trunk", "maxspeed=80" }),
                    @Edge(id = "9", coordinates = { @Loc(value = ONE), @Loc(value = TWO) }, tags = {
                            "highway=trunk", "maxspeed:backward=10", "maxspeed:forward=90" }),
                    @Edge(id = "-9", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=trunk", "maxspeed:backward=10",
                                    "maxspeed:forward=90" }),
                    @Edge(id = "293669785000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=trunk" }),
                    @Edge(id = "-293669785000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=trunk" }),
                    @Edge(id = "293669785000001", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=trunk" }),
                    @Edge(id = "-293669785000001", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=trunk" }),
                    @Edge(id = "293669786000001", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }, tags = { "highway=trunk", "oneway=yes" })

            })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
