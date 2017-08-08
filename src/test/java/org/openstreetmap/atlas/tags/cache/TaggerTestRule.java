package org.openstreetmap.atlas.tags.cache;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test atlas for Tagger
 * 
 * @author gpogulsky
 */
public class TaggerTestRule extends CoreTestRule
{
    private static final String ONE = "34.1444317, -118.7909766";
    private static final String TWO_CONNECTED = "34.144713, -118.7924548";
    private static final String THREE = "34.1448283, -118.7937022";
    private static final String FOUR = "34.1447281, -118.7936994";

    @TestAtlas(
            // nodes
            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO_CONNECTED)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR))

            },
            // edges
            edges = {
                    @Edge(id = "12000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO_CONNECTED) }, tags = { "highway=secondary",
                                    "name=Agoura Road" }),
                    @Edge(id = "23000000", coordinates = { @Loc(value = TWO_CONNECTED),
                            @Loc(value = THREE) }, tags = { "highway=secondary", "name=Agoura Road",
                                    "oneway=yes" }),
                    @Edge(id = "24000000", coordinates = { @Loc(value = FOUR),
                            @Loc(value = TWO_CONNECTED) }, tags = { "highway=secondary",
                                    "name=Agoura Road", "oneway=yes" })

            })

    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
