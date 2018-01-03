package org.openstreetmap.atlas.geography.atlas.dynamic;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author matthieun
 */
public class DynamicAtlasPreemptiveLoadTestRule extends CoreTestRule
{
    private static final String ONE = "7.13463380459,-10.49970749297";
    private static final String TWO = "7.03806608353,-10.65738262866";
    private static final String THREE = "6.90376746323,-10.48617761334";
    private static final String FOUR = "6.93682915051,-10.38626465608";
    private static final String FIVE = "7.1124301863,-10.72451164682";
    private static final String SIX = "7.06078971839,-10.76145862581";
    private static final String SEVEN = "6.97092158452,-10.67871820807";
    private static final String EIGHT = "6.90686709472,-10.71514480708";
    private static final String NINE = "6.88671912738,-10.6230375496";
    private static final String TEN = "6.85107062885,-10.60534463008";
    private static final String ELEVEN = "6.88516924827,-10.5241653523";
    private static final String TWELVE = "6.8185196719,-10.47785153357";
    private static final String THIRTEEN = "6.84693729693,-10.41696707524";

    @TestAtlas(

            nodes = {

                    @Node(id = "5", coordinates = @Loc(value = FIVE)),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT)),
                    @Node(id = "9", coordinates = @Loc(value = NINE)),
                    @Node(id = "13", coordinates = @Loc(value = THIRTEEN))

            },

            edges = {

                    @Edge(id = "2000000", coordinates = { @Loc(value = FIVE), @Loc(value = SIX),
                            @Loc(value = SEVEN),
                            @Loc(value = EIGHT) }, tags = { "highway=trunk", "oneway=yes" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = NINE), @Loc(value = TEN),
                            @Loc(value = ELEVEN), @Loc(value = TWELVE),
                            @Loc(value = THIRTEEN) }, tags = { "highway=motorway", "oneway=yes" })

            }

    )
    private Atlas atlasZ9x240y246;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT))

            },

            edges = {

                    @Edge(id = "1000000", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "2000000", coordinates = { @Loc(value = FIVE), @Loc(value = SIX),
                            @Loc(value = SEVEN),
                            @Loc(value = EIGHT) }, tags = { "highway=trunk", "oneway=yes" })

            }

    )
    private Atlas atlasZ9x240y245;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR))

            },

            edges = {

                    @Edge(id = "1000000", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=primary", "oneway=yes" })

            }

    )
    private Atlas atlasZ9x241y245;

    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "9", coordinates = @Loc(value = NINE)),
                    @Node(id = "13", coordinates = @Loc(value = THIRTEEN))

            },

            edges = {

                    @Edge(id = "1000000", coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=primary", "oneway=yes" }),
                    @Edge(id = "3000000", coordinates = { @Loc(value = NINE), @Loc(value = TEN),
                            @Loc(value = ELEVEN), @Loc(value = TWELVE),
                            @Loc(value = THIRTEEN) }, tags = { "highway=motorway", "oneway=yes" })

            }

    )
    private Atlas atlasZ9x241y246;

    public Atlas getAtlasZ9x240y245()
    {
        return this.atlasZ9x240y245;
    }

    public Atlas getAtlasZ9x240y246()
    {
        return this.atlasZ9x240y246;
    }

    public Atlas getAtlasZ9x241y245()
    {
        return this.atlasZ9x241y245;
    }

    public Atlas getAtlasZ9x241y246()
    {
        return this.atlasZ9x241y246;
    }
}
