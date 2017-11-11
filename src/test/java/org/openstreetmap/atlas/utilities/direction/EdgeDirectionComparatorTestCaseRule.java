package org.openstreetmap.atlas.utilities.direction;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author Sid
 */
public class EdgeDirectionComparatorTestCaseRule extends CoreTestRule
{
    private static final String ONE = "-34.870399,-56.1752752";
    private static final String TWO = "-34.870359, -56.174562";
    private static final String THREE = "-34.8704619,-56.1747235";
    private static final String FOUR = "-34.869687, -56.174080";
    private static final String FIVE = "-31.7180368,-55.9959477";
    private static final String SIX = "-31.718552, -55.996589";
    private static final String SEVEN = "-31.718639, -55.996470";
    private static final String EIGHT = "-31.7181469,-55.9958428";
    private static final String NINE = "19.0546006,-70.446448";
    private static final String TEN = "19.0495406,-70.446176";
    private static final String ELEVEN = "19.0502,-70.4514893";
    private static final String TWELVE = "19.049056, -70.445427";
    private static final String THIRTEEN = "-34.7761251,-55.3534846";
    private static final String FOURTEEN = "-34.774595,-55.3565624";
    private static final String FIFTEEN = "-34.7756381,-55.3548773";

    /*
     * Same Direction
     */
    private static final String SIXTEEN = "-34.897118, -56.157317";
    private static final String SEVENTEEN = "-34.897149, -56.157169";
    private static final String EIGHTEEN = "-34.897165, -56.157087";

    /*
     * Same Direction - Segment Heading vs OverallHeading
     */
    private static final String NINETEEN = "18.4220149,-70.7864727";
    private static final String TWENTY = "18.4220965,-70.7869184";
    private static final String TWENTYONE = "18.4221578,-70.7872535";
    private static final String TWENTYTWO = "18.42245,-70.78779";

    @TestAtlas(

            nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)),
                    @Node(id = "6", coordinates = @Loc(value = SIX)),
                    @Node(id = "7", coordinates = @Loc(value = SEVEN)),
                    @Node(id = "8", coordinates = @Loc(value = EIGHT)),
                    @Node(id = "9", coordinates = @Loc(value = NINE)),
                    @Node(id = "10", coordinates = @Loc(value = TEN)),
                    @Node(id = "11", coordinates = @Loc(value = ELEVEN)),
                    @Node(id = "12", coordinates = @Loc(value = TWELVE)),
                    @Node(id = "13", coordinates = @Loc(value = THIRTEEN)),
                    @Node(id = "14", coordinates = @Loc(value = FOURTEEN)),
                    @Node(id = "15", coordinates = @Loc(value = FIFTEEN)),
                    @Node(id = "16", coordinates = @Loc(value = SIXTEEN)),
                    @Node(id = "17", coordinates = @Loc(value = SEVENTEEN)),
                    @Node(id = "18", coordinates = @Loc(value = EIGHTEEN)),
                    @Node(id = "19", coordinates = @Loc(value = NINETEEN)),
                    @Node(id = "20", coordinates = @Loc(value = TWENTY)),
                    @Node(id = "21", coordinates = @Loc(value = TWENTYONE)),
                    @Node(id = "22", coordinates = @Loc(value = TWENTYTWO)), },

            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=trunk" }),
                    @Edge(id = "23", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=trunk" }),
                    @Edge(id = "24", coordinates = { @Loc(value = TWO),
                            @Loc(value = FOUR) }, tags = { "highway=trunk" }),
                    @Edge(id = "56", coordinates = { @Loc(value = FIVE),
                            @Loc(value = SIX) }, tags = { "highway=trunk" }),
                    @Edge(id = "67", coordinates = { @Loc(value = SIX),
                            @Loc(value = SEVEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "78", coordinates = { @Loc(value = SEVEN),
                            @Loc(value = EIGHT) }, tags = { "highway=trunk" }),
                    @Edge(id = "65", coordinates = { @Loc(value = SIX),
                            @Loc(value = FIVE) }, tags = { "highway=trunk" }),
                    @Edge(id = "910", coordinates = { @Loc(value = NINE),
                            @Loc(value = TEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1011", coordinates = { @Loc(value = TEN),
                            @Loc(value = ELEVEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1012", coordinates = { @Loc(value = TEN),
                            @Loc(value = TWELVE) }, tags = { "highway=trunk" }),
                    @Edge(id = "1314", coordinates = { @Loc(value = THIRTEEN),
                            @Loc(value = FOURTEEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1415", coordinates = { @Loc(value = FOURTEEN),
                            @Loc(value = FIFTEEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1617", coordinates = { @Loc(value = SIXTEEN),
                            @Loc(value = SEVENTEEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1718", coordinates = { @Loc(value = SEVENTEEN),
                            @Loc(value = EIGHTEEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1920", coordinates = { @Loc(value = NINETEEN),
                            @Loc(value = TWENTY) }, tags = { "highway=trunk" }),
                    @Edge(id = "202122", coordinates = { @Loc(value = TWENTY),
                            @Loc(value = TWENTYONE),
                            @Loc(value = TWENTYTWO) }, tags = { "highway=trunk" }), })

    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
