package org.openstreetmap.atlas.geography.atlas.raw;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * {@link OsmPbfToSlicedRawAtlasTest} test data.
 *
 * @author mgostintsev
 */
public class DynamicRawAtlasSectioningTestRule extends CoreTestRule
{
    // Fully inside 8-123-122
    private static final String ONE = "7.9747091, -6.6837721";
    private static final String TWO = "7.9737754, -6.6823776";

    // Fully inside 8-123-123
    private static final String THREE = "5.7826593, -6.5920337";
    private static final String FOUR = "5.7827163, -6.5914695";

    // Fully inside 7-62-61
    private static final String FIVE = "6.7240498, -3.4840235";
    private static final String SIX = "6.7260445, -3.4840096";

    // Crossing 8-123-122 into 8-123-123
    private static final String SEVEN = "7.0401813, -6.4733942";
    private static final String EIGHT = "6.8870387, -6.4641103";
    private static final String NINE = " 6.8832099, -6.4636300";
    private static final String TEN = "6.8866444, -6.4654545";

    // Crossing 8-123-122 into 7-62-61
    private static final String ELEVEN = "7.6829818, -5.6379654";
    private static final String TWELVE = "7.6735590, -5.5867130";
    private static final String THIRTEEN = "7.6717846, -5.5812728";
    private static final String FOURTEEN = "7.6681651, -5.5865950";

    // Crossing 8-123-123 into 7-62-61
    private static final String FIFTEEN = "5.8751274, -5.6744400";
    private static final String SIXTEEN = "5.8641274, -5.5664451";
    private static final String SEVENTEEN = "5.8545285, -5.5181042";
    private static final String EIGHTEEN = "5.8813557, -5.5808398";

    // Starting in 8-123-122, through 8-123-123, ending in 7-62-61
    private static final String NINETEEN = "7.3884501, -6.4772718";
    private static final String TWENTY = "7.3864604, -6.4728799";
    private static final String TWENTY_ONE = "6.8787867, -6.4534416";
    private static final String TWENTY_TWO = "6.8738255, -6.4553079";
    private static final String TWENTY_THREE = "6.8299549, -5.2608135";
    private static final String TWENTY_FOUR = "6.8298791, -5.2562899";
    private static final String TWENTY_FIVE = "7.3868182, -6.4796125";
    private static final String TWENTY_SIX = "6.8769819, -6.4596778";
    private static final String TWENTY_SEVEN = "6.8333518, -5.2585345";

    @TestAtlas(lines = {
            @Line(id = "541701001000", coordinates = { @Loc(value = THREE),
                    @Loc(value = FOUR) }, tags = { "highway=primary" }),
            @Line(id = "541702001000", coordinates = { @Loc(value = SEVEN), @Loc(value = EIGHT),
                    @Loc(value = NINE) }, tags = { "highway=primary" }),
            @Line(id = "541703001000", coordinates = { @Loc(value = EIGHT),
                    @Loc(value = TEN) }, tags = { "highway=primary" }),
            @Line(id = "541704001000", coordinates = { @Loc(value = TWENTY_SIX),
                    @Loc(value = TWENTY_ONE) }, tags = { "highway=primary" }),
            @Line(id = "541705001000", coordinates = { @Loc(value = FIFTEEN), @Loc(value = SIXTEEN),
                    @Loc(value = SEVENTEEN) }, tags = { "highway=primary" }),
            @Line(id = "541706001000", coordinates = { @Loc(value = NINETEEN), @Loc(value = TWENTY),
                    @Loc(value = TWENTY_ONE), @Loc(value = TWENTY_TWO), @Loc(value = TWENTY_THREE),
                    @Loc(value = TWENTY_FOUR) }, tags = { "highway=primary" })

    }, points = {

            @Point(id = "511111003", coordinates = @Loc(value = THREE)),
            @Point(id = "511111004", coordinates = @Loc(value = FOUR)),
            @Point(id = "511111009", coordinates = @Loc(value = NINE)),
            @Point(id = "511111007", coordinates = @Loc(value = SEVEN)),
            @Point(id = "511111008", coordinates = @Loc(value = EIGHT)),
            @Point(id = "511111010", coordinates = @Loc(value = TEN)),
            @Point(id = "511111015", coordinates = @Loc(value = FIFTEEN)),
            @Point(id = "511111017", coordinates = @Loc(value = SEVENTEEN)),
            @Point(id = "511111019", coordinates = @Loc(value = NINETEEN)),
            @Point(id = "511111021", coordinates = @Loc(value = TWENTY_ONE)),
            @Point(id = "511111026", coordinates = @Loc(value = TWENTY_SIX)),
            @Point(id = "511111022", coordinates = @Loc(value = TWENTY_TWO)),
            @Point(id = "511111024", coordinates = @Loc(value = TWENTY_FOUR)) })
    private Atlas atlasZ8x123y123;

    @TestAtlas(lines = {
            @Line(id = "541707001000", coordinates = { @Loc(value = ONE),
                    @Loc(value = TWO) }, tags = { "highway=primary" }),
            @Line(id = "541702001000", coordinates = { @Loc(value = SEVEN), @Loc(value = EIGHT),
                    @Loc(value = NINE) }, tags = { "highway=primary" }),
            @Line(id = "541708001000", coordinates = { @Loc(value = ELEVEN), @Loc(value = TWELVE),
                    @Loc(value = THIRTEEN) }, tags = { "highway=primary" }),
            @Line(id = "541709001000", coordinates = { @Loc(value = TWENTY_FIVE),
                    @Loc(value = TWENTY) }, tags = { "highway=primary" }),
            @Line(id = "541706001000", coordinates = { @Loc(value = NINETEEN), @Loc(value = TWENTY),
                    @Loc(value = TWENTY_ONE), @Loc(value = TWENTY_TWO), @Loc(value = TWENTY_THREE),
                    @Loc(value = TWENTY_FOUR) }, tags = { "highway=primary" })

    }, points = {

            @Point(id = "511111001", coordinates = @Loc(value = ONE)),
            @Point(id = "511111002", coordinates = @Loc(value = TWO)),
            @Point(id = "511111011", coordinates = @Loc(value = ELEVEN)),
            @Point(id = "511111013", coordinates = @Loc(value = THIRTEEN)),
            @Point(id = "511111007", coordinates = @Loc(value = SEVEN)),
            @Point(id = "511111009", coordinates = @Loc(value = NINE)),
            @Point(id = "511111019", coordinates = @Loc(value = NINETEEN)),
            @Point(id = "511111020", coordinates = @Loc(value = TWENTY)),
            @Point(id = "511111025", coordinates = @Loc(value = TWENTY_FIVE)),
            @Point(id = "511111022", coordinates = @Loc(value = TWENTY_TWO)),
            @Point(id = "511111024", coordinates = @Loc(value = TWENTY_FOUR))

    })
    private Atlas atlasZ8x123y122;

    @TestAtlas(lines = {

            @Line(id = "541710001000", coordinates = { @Loc(value = FIVE),
                    @Loc(value = SIX) }, tags = { "highway=primary" }),
            @Line(id = "541711001000", coordinates = { @Loc(value = TWELVE),
                    @Loc(value = FOURTEEN) }, tags = { "highway=primary" }),
            @Line(id = "541708001000", coordinates = { @Loc(value = ELEVEN), @Loc(value = TWELVE),
                    @Loc(value = THIRTEEN) }, tags = { "highway=primary" }),
            @Line(id = "541712001000", coordinates = { @Loc(value = TWENTY_SEVEN),
                    @Loc(value = TWENTY_THREE) }, tags = { "highway=primary" }),
            @Line(id = "541713001000", coordinates = { @Loc(value = SIXTEEN),
                    @Loc(value = EIGHTEEN) }, tags = { "highway=primary" }),
            @Line(id = "541705001000", coordinates = { @Loc(value = FIFTEEN), @Loc(value = SIXTEEN),
                    @Loc(value = SEVENTEEN) }, tags = { "highway=primary" }),
            @Line(id = "541706001000", coordinates = { @Loc(value = NINETEEN), @Loc(value = TWENTY),
                    @Loc(value = TWENTY_ONE), @Loc(value = TWENTY_TWO), @Loc(value = TWENTY_THREE),
                    @Loc(value = TWENTY_FOUR) }, tags = { "highway=primary" }) }, points = {

                            @Point(id = "511111005", coordinates = @Loc(value = FIVE)),
                            @Point(id = "511111006", coordinates = @Loc(value = SIX)),
                            @Point(id = "511111011", coordinates = @Loc(value = ELEVEN)),
                            @Point(id = "511111012", coordinates = @Loc(value = TWELVE)),
                            @Point(id = "511111013", coordinates = @Loc(value = THIRTEEN)),
                            @Point(id = "511111014", coordinates = @Loc(value = FOURTEEN)),
                            @Point(id = "511111015", coordinates = @Loc(value = FIFTEEN)),
                            @Point(id = "511111016", coordinates = @Loc(value = SIXTEEN)),
                            @Point(id = "511111017", coordinates = @Loc(value = SEVENTEEN)),
                            @Point(id = "511111018", coordinates = @Loc(value = EIGHTEEN)),
                            @Point(id = "511111019", coordinates = @Loc(value = NINETEEN)),
                            @Point(id = "511111022", coordinates = @Loc(value = TWENTY_TWO)),
                            @Point(id = "511111024", coordinates = @Loc(value = TWENTY_FOUR)),
                            @Point(id = "511111023", coordinates = @Loc(value = TWENTY_THREE)),
                            @Point(id = "511111027", coordinates = @Loc(value = TWENTY_SEVEN))

    })
    private Atlas atlasZ7x62y61;

    public Atlas getAtlasz7x62y61()
    {
        return this.atlasZ7x62y61;
    }

    public Atlas getAtlasz8x123y122()
    {
        return this.atlasZ8x123y122;
    }

    public Atlas getAtlasz8x123y123()
    {
        return this.atlasZ8x123y123;
    }
}
