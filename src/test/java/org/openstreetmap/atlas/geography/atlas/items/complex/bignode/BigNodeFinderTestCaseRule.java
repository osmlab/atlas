package org.openstreetmap.atlas.geography.atlas.items.complex.bignode;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author Sid
 * @author mgostintsev
 **/
public class BigNodeFinderTestCaseRule extends CoreTestRule
{
    /**
     * 1. The atlas is a two dual carriage way intersection in DOM
     */
    private static final String ONE = "19.2202841, -70.5305208";
    private static final String TWO = "19.2203577, -70.5309603";
    private static final String THREE = "19.2201964, -70.5305353";
    private static final String FOUR = "19.2202682, -70.5309606";
    private static final String FIVE = "19.2203771, -70.5310792";
    private static final String SIX = "19.2202916, -70.5310805";
    private static final String SEVEN = "19.2215199, -70.5307007";
    private static final String EIGHT = "19.2215288, -70.5307861";
    private static final String NINE = "19.2205727, -70.5322487";
    private static final String TEN = "19.220495, -70.5322645";
    private static final String ELEVEN = "19.2189432, -70.5311005";
    private static final String TWELVE = "19.2189336, -70.5310225";

    /**
     * 2. The nodes below are an use case for junctionRoute. See
     * http://www.openstreetmap.org/search?query=18.9576533%2C%20-70.4047707#map=19/18.95815/-70.
     * 40446
     */
    private static final String THIRTEEN = "18.9584473, -70.4051898";
    private static final String FOURTEEN = "18.9585898, -70.4048838";
    private static final String FIFTEEN = "18.9586238, -70.4048091";
    private static final String SIXTEEN = "18.9591569, -70.4055156";
    private static final String SEVENTEEN = "18.9591438, -70.405073";
    private static final String EIGHTEEN = "18.9576533, -70.4047707";
    private static final String NINETEEN = "18.958118, -70.4044936";

    /**
     * 3. This tests the cases where junction edges are merged together through non-junction edges
     * http://www.openstreetmap.org/search?query=19.2263658%2C%20-70.5232922#map=18/19.22637/-70.
     * 52329
     */
    private static final String TWENTY = "19.2263658, -70.5232922";
    private static final String TWENTYONE = "19.2264076, -70.523348";
    private static final String TWENTYTWO = "19.226494, -70.5232983";
    private static final String TWENTYTHREE = "19.2264474, -70.5232479";
    private static final String TWENTYFOUR = "19.2255758, -70.5221077";
    private static final String TWENTYFIVE = "19.2261509, -70.5229941";
    private static final String TWENTYSIX = "19.2271679, -70.5243228";
    private static final String TWENTYSEVEN = "19.2272337, -70.5242798";
    private static final String TWENTYEIGHT = "19.2267647, -70.5231237";
    private static final String TWENTYNINE = "19.2267337, -70.5230635";

    /**
     * 4. Expand to Residential Roads
     * http://www.openstreetmap.org/way/139612559#map=18/-34.75409/-55.99516&layers=D
     */
    private static final String THIRTY = "-34.7530995, -55.9955144";
    private static final String THIRTYONE = "-34.7531244, -55.9954034";
    private static final String THIRTYTWO = "-34.7538068, -55.995732";
    private static final String THIRTYTHREE = "-34.7538356, -55.9956094";
    private static final String THIRTYFOUR = "-34.7544319, -55.995908";
    private static final String THIRTYFIVE = "-34.7544525, -55.995791";
    private static final String THIRTYSIX = "-34.7534419, -55.9974882";

    /**
     * 5. Tertiary dual carriage way roads are on one side of junction road
     * http://www.openstreetmap.org/way/425027651
     */
    private static final String THIRTYSEVEN = "-34.0999738, -56.2199071";
    private static final String THIRTYEIGHT = "-34.1000359, -56.2198257";
    private static final String THIRTYNINE = "-34.1002258, -56.2208914";
    private static final String FORTY = "-34.100143, -56.2208073";

    /**
     * 6. Angled (hence longer in length) junction roads
     * http://www.openstreetmap.org/way/179178503#map=19/-34.91306/-56.16228
     */
    private static final String FORTYONE = "-34.9128909, -56.1630794";
    private static final String FORTYTWO = "-34.9130465, -56.1628523";
    private static final String FORTYTHREE = "-34.9134932, -56.1630144";
    private static final String FORTYFOUR = "-34.9134742, -56.1628122";
    private static final String FORTYFIVE = "-34.9139186, -56.1629724";
    private static final String FORTYSIX = "-34.9138376, -56.1627738";
    private static final String FORTYSEVEN = "-34.9123896, -56.163135";
    private static final String FORTYEIGHT = "-34.9123744, -56.1629292";

    /**
     * 7. Merging Nearby Junction Edges
     * https://www.openstreetmap.org/#map=19/-34.86409655621848/-56.15216571962752
     */
    private static final String FORTYNINE = "-34.8633275, -56.1520582";
    private static final String FIFTY = "-34.8633582, -56.1518486";
    private static final String FIFTYONE = "-34.864245, -56.1529168";
    private static final String FIFTYTWO = "-34.8638276, -56.1521606";
    private static final String FIFTYTHREE = "-34.8636873, -56.1519129";
    private static final String FIFTYFOUR = "-34.8634207, -56.1514326";
    private static final String FIFTYFIVE = "-34.8644674, -56.1527475";
    private static final String FIFTYSIX = "-34.8641705, -56.1522056";
    private static final String FIFTYSEVEN = "-34.8640427, -56.151971";
    private static final String FIFTYEIGHT = "-34.8632392, -56.1505407";
    private static final String FIFTYNINE = "-34.8645135, -56.1522445";
    private static final String SIXTY = "-34.86453, -56.1520303";

    /**
     * 8. Case where a bifurcation point (between a two-way road and 2 one-ways roads ) creates a
     * invalid junction edge http://www.openstreetmap.org/relation/6517015#map=19/4.88642/-52.28552
     * Other examples : https://www.openstreetmap.org/#map=20/4.931389860539109/-52.30634515601126
     * https://www.openstreetmap.org/#map=19/4.887468820117024/-52.28483397495584
     */
    private static final String SIXTYONE = "4.8866669, -52.2865243";
    private static final String SIXTYTWO = "4.8868532, -52.2862674";
    private static final String SIXTYTHREE = "4.8870696, -52.2860041";
    private static final String SIXTYFOUR = "4.8871591, -52.2858701";
    private static final String SIXTYFIVE = "4.8870147, -52.2859692";

    /**
     * 9. Filter off piers
     */
    private static final String SIXTYSIX = "41.9043091, 12.4744368";
    private static final String SIXTYSEVEN = "41.9043335, 12.4743707";
    private static final String SIXTYEIGHT = "41.9044448, 12.4744446";
    private static final String SIXTYNINE = "41.9044215, 12.4745081";

    /**
     * 10. Inspired by Big Node at Location: 42.386538, -83.139549. Making sure we are returning all
     * possible paths, instead of just the shortest path
     */
    private static final String SEVENTY = "42.3867848, -83.1395522";
    private static final String SEVENTY_ONE = "42.3866924, -83.139548";
    private static final String SEVENTY_TWO = "42.3865343, -83.1395345";
    private static final String SEVENTY_THREE = "42.3864108, -83.1395301";
    private static final String SEVENTY_FOUR = "42.3866828, -83.139747";
    private static final String SEVENTY_FIVE = "42.3865265, -83.1397502";
    private static final String SEVENTY_SIX = "42.3865507, -83.139331";
    private static final String SEVENTY_SEVEN = "42.3867006, -83.1393079";

    /**
     * 11. Exclude cycle ways at inEdges/outEdges
     */
    private static final String SEVENTY_EIGHT = "55.7167715, 12.4571926";
    private static final String SEVENTY_NINE = "55.7168313, 12.4572756";
    private static final String EIGHTY = "55.7169283, 12.4573998";
    private static final String EIGHTY_ONE = "55.7169963, 12.4574844";

    private static final String EIGHTY_TWO = "55.71685, 12.45793";
    private static final String EIGHTY_THREE = "55.71675, 12.45792";
    private static final String EIGHTY_FOUR = "55.71668, 12.45771";

    private static final String EIGHTY_FIVE = "55.71702, 12.45676";
    private static final String EIGHTY_SIX = "55.71711, 12.45687";
    private static final String EIGHTY_SEVEN = "55.71718, 12.45692";

    /**
     * 12. Exclude service roads
     */
    private static final String EIGHTY_EIGHT = "56.1930077, 10.2433446";
    private static final String EIGHTY_NINE = "56.1928659, 10.2430657";
    private static final String NINETY = "56.1930248, 10.2426652";
    private static final String NINETY_ONE = "56.1930801, 10.243183";

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

                    // Second Big Node example
                    @Node(id = "13", coordinates = @Loc(value = THIRTEEN)),
                    @Node(id = "14", coordinates = @Loc(value = FOURTEEN)),
                    @Node(id = "15", coordinates = @Loc(value = FIFTEEN)),
                    @Node(id = "16", coordinates = @Loc(value = SIXTEEN)),
                    @Node(id = "17", coordinates = @Loc(value = SEVENTEEN)),
                    @Node(id = "18", coordinates = @Loc(value = EIGHTEEN)),
                    @Node(id = "19", coordinates = @Loc(value = NINETEEN)),

                    // Third Big Node example
                    @Node(id = "20", coordinates = @Loc(value = TWENTY)),
                    @Node(id = "21", coordinates = @Loc(value = TWENTYONE)),
                    @Node(id = "22", coordinates = @Loc(value = TWENTYTWO)),
                    @Node(id = "23", coordinates = @Loc(value = TWENTYTHREE)),
                    @Node(id = "24", coordinates = @Loc(value = TWENTYFOUR)),
                    @Node(id = "25", coordinates = @Loc(value = TWENTYFIVE)),
                    @Node(id = "26", coordinates = @Loc(value = TWENTYSIX)),
                    @Node(id = "27", coordinates = @Loc(value = TWENTYSEVEN)),
                    @Node(id = "28", coordinates = @Loc(value = TWENTYEIGHT)),
                    @Node(id = "29", coordinates = @Loc(value = TWENTYNINE)),

                    // Fourth example
                    @Node(id = "30", coordinates = @Loc(value = THIRTY)),
                    @Node(id = "31", coordinates = @Loc(value = THIRTYONE)),
                    @Node(id = "32", coordinates = @Loc(value = THIRTYTWO)),
                    @Node(id = "33", coordinates = @Loc(value = THIRTYTHREE)),
                    @Node(id = "34", coordinates = @Loc(value = THIRTYFOUR)),
                    @Node(id = "35", coordinates = @Loc(value = THIRTYFIVE)),
                    @Node(id = "36", coordinates = @Loc(value = THIRTYSIX)),

                    // Fifth example
                    @Node(id = "37", coordinates = @Loc(value = THIRTYSEVEN)),
                    @Node(id = "38", coordinates = @Loc(value = THIRTYEIGHT)),
                    @Node(id = "39", coordinates = @Loc(value = THIRTYNINE)),
                    @Node(id = "40", coordinates = @Loc(value = FORTY)),

                    // Sixth example
                    @Node(id = "41", coordinates = @Loc(value = FORTYONE)),
                    @Node(id = "42", coordinates = @Loc(value = FORTYTWO)),
                    @Node(id = "43", coordinates = @Loc(value = FORTYTHREE)),
                    @Node(id = "44", coordinates = @Loc(value = FORTYFOUR)),
                    @Node(id = "45", coordinates = @Loc(value = FORTYFIVE)),
                    @Node(id = "46", coordinates = @Loc(value = FORTYSIX)),
                    @Node(id = "47", coordinates = @Loc(value = FORTYSEVEN)),
                    @Node(id = "48", coordinates = @Loc(value = FORTYEIGHT)),

                    // Seventh example
                    @Node(id = "49", coordinates = @Loc(value = FORTYNINE)),
                    @Node(id = "50", coordinates = @Loc(value = FIFTY)),
                    @Node(id = "51", coordinates = @Loc(value = FIFTYONE)),
                    @Node(id = "52", coordinates = @Loc(value = FIFTYTWO)),
                    @Node(id = "53", coordinates = @Loc(value = FIFTYTHREE)),
                    @Node(id = "54", coordinates = @Loc(value = FIFTYFOUR)),
                    @Node(id = "55", coordinates = @Loc(value = FIFTYFIVE)),
                    @Node(id = "56", coordinates = @Loc(value = FIFTYSIX)),
                    @Node(id = "57", coordinates = @Loc(value = FIFTYSEVEN)),
                    @Node(id = "58", coordinates = @Loc(value = FIFTYEIGHT)),
                    @Node(id = "59", coordinates = @Loc(value = FIFTYNINE)),
                    @Node(id = "60", coordinates = @Loc(value = SIXTY)),

                    // Eighth example
                    @Node(id = "61", coordinates = @Loc(value = SIXTYONE)),
                    @Node(id = "62", coordinates = @Loc(value = SIXTYTWO)),
                    @Node(id = "63", coordinates = @Loc(value = SIXTYTHREE)),
                    @Node(id = "64", coordinates = @Loc(value = SIXTYFOUR)),
                    @Node(id = "65", coordinates = @Loc(value = SIXTYFIVE)),

                    // Ninth example
                    @Node(id = "66", coordinates = @Loc(value = SIXTYSIX)),
                    @Node(id = "67", coordinates = @Loc(value = SIXTYSEVEN)),
                    @Node(id = "68", coordinates = @Loc(value = SIXTYEIGHT)),
                    @Node(id = "69", coordinates = @Loc(value = SIXTYNINE)),

                    // Eleventh example
                    @Node(id = "78", coordinates = @Loc(value = SEVENTY_EIGHT)),
                    @Node(id = "79", coordinates = @Loc(value = SEVENTY_NINE)),
                    @Node(id = "80", coordinates = @Loc(value = EIGHTY)),
                    @Node(id = "81", coordinates = @Loc(value = EIGHTY_ONE)),
                    @Node(id = "82", coordinates = @Loc(value = EIGHTY_TWO)),
                    @Node(id = "83", coordinates = @Loc(value = EIGHTY_THREE)),
                    @Node(id = "84", coordinates = @Loc(value = EIGHTY_FOUR)),
                    @Node(id = "85", coordinates = @Loc(value = EIGHTY_FIVE)),
                    @Node(id = "86", coordinates = @Loc(value = EIGHTY_SIX)),
                    @Node(id = "87", coordinates = @Loc(value = EIGHTY_SEVEN)),

                    // Twelfth example
                    @Node(id = "88", coordinates = @Loc(value = EIGHTY_EIGHT)),
                    @Node(id = "89", coordinates = @Loc(value = EIGHTY_NINE)),
                    @Node(id = "90", coordinates = @Loc(value = NINETY)),
                    @Node(id = "91", coordinates = @Loc(value = NINETY_ONE)) },

            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }, tags = { "highway=trunk" }),
                    @Edge(id = "43", coordinates = { @Loc(value = FOUR),
                            @Loc(value = THREE) }, tags = { "highway=trunk" }),
                    @Edge(id = "42", coordinates = { @Loc(value = FOUR),
                            @Loc(value = TWO) }, tags = { "highway=trunk" }),
                    @Edge(id = "25", coordinates = { @Loc(value = TWO),
                            @Loc(value = FIVE) }, tags = { "highway=trunk" }),
                    @Edge(id = "56", coordinates = { @Loc(value = FIVE),
                            @Loc(value = SIX) }, tags = { "highway=trunk" }),
                    @Edge(id = "64", coordinates = { @Loc(value = SIX),
                            @Loc(value = FOUR) }, tags = { "highway=trunk" }),
                    @Edge(id = "27", coordinates = { @Loc(value = TWO),
                            @Loc(value = SEVEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "85", coordinates = { @Loc(value = EIGHT),
                            @Loc(value = FIVE) }, tags = { "highway=trunk" }),
                    @Edge(id = "59", coordinates = { @Loc(value = FIVE),
                            @Loc(value = NINE) }, tags = { "highway=trunk" }),
                    @Edge(id = "106", coordinates = { @Loc(value = TEN),
                            @Loc(value = SIX) }, tags = { "highway=trunk" }),
                    @Edge(id = "611", coordinates = { @Loc(value = SIX),
                            @Loc(value = ELEVEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "124", coordinates = { @Loc(value = TWELVE),
                            @Loc(value = FOUR) }, tags = { "highway=trunk" }),

                    // Second Big Node example
                    @Edge(id = "1314", coordinates = { @Loc(value = THIRTEEN),
                            @Loc(value = FOURTEEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1415", coordinates = { @Loc(value = FOURTEEN),
                            @Loc(value = FIFTEEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1613", coordinates = { @Loc(value = SIXTEEN),
                            @Loc(value = THIRTEEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1517", coordinates = { @Loc(value = FIFTEEN),
                            @Loc(value = SEVENTEEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1318", coordinates = { @Loc(value = THIRTEEN),
                            @Loc(value = EIGHTEEN) }, tags = { "highway=trunk" }),
                    @Edge(id = "1915", coordinates = { @Loc(value = NINETEEN),
                            @Loc(value = FIFTEEN) }, tags = { "highway=trunk" }),

                    // Third example
                    @Edge(id = "2423", coordinates = { @Loc(value = TWENTYFOUR),
                            @Loc(value = TWENTYTHREE) }, tags = { "highway=primary" }),
                    @Edge(id = "2322", coordinates = { @Loc(value = TWENTYTHREE),
                            @Loc(value = TWENTYTWO) }, tags = { "highway=primary" }),
                    @Edge(id = "2227", coordinates = { @Loc(value = TWENTYTWO),
                            @Loc(value = TWENTYSEVEN) }, tags = { "highway=primary" }),
                    @Edge(id = "2025", coordinates = { @Loc(value = TWENTY),
                            @Loc(value = TWENTYFIVE) }, tags = { "highway=primary" }),
                    @Edge(id = "2120", coordinates = { @Loc(value = TWENTYONE),
                            @Loc(value = TWENTY) }, tags = { "highway=primary" }),
                    @Edge(id = "2621", coordinates = { @Loc(value = TWENTYSIX),
                            @Loc(value = TWENTYONE) }, tags = { "highway=primary" }),
                    @Edge(id = "2822", coordinates = { @Loc(value = TWENTYEIGHT),
                            @Loc(value = TWENTYTWO) }, tags = { "highway=primary" }),
                    @Edge(id = "2221", coordinates = { @Loc(value = TWENTYTWO),
                            @Loc(value = TWENTYONE) }, tags = { "highway=primary" }),
                    @Edge(id = "2023", coordinates = { @Loc(value = TWENTY),
                            @Loc(value = TWENTYTHREE) }, tags = { "highway=primary" }),
                    @Edge(id = "2329", coordinates = { @Loc(value = TWENTYTHREE),
                            @Loc(value = TWENTYNINE) }, tags = { "highway=primary" }),

                    // Fourth example
                    @Edge(id = "3230", coordinates = { @Loc(value = THIRTYTWO),
                            @Loc(value = THIRTY) }, tags = { "highway=residential" }),
                    @Edge(id = "3133", coordinates = { @Loc(value = THIRTYONE),
                            @Loc(value = THIRTYTHREE) }, tags = { "highway=residential" }),
                    @Edge(id = "3432", coordinates = { @Loc(value = THIRTYFOUR),
                            @Loc(value = THIRTYTWO) }, tags = { "highway=residential" }),
                    @Edge(id = "3335", coordinates = { @Loc(value = THIRTYTHREE),
                            @Loc(value = THIRTYFIVE) }, tags = { "highway=residential" }),
                    @Edge(id = "3632", coordinates = { @Loc(value = THIRTYSIX),
                            @Loc(value = THIRTYTWO) }, tags = { "highway=residential" }),
                    @Edge(id = "3233", coordinates = { @Loc(value = THIRTYTWO),
                            @Loc(value = THIRTYTHREE) }, tags = { "highway=residential",
                                    "oneway=no" }),
                    @Edge(id = "-3233", coordinates = { @Loc(value = THIRTYTHREE),
                            @Loc(value = THIRTYTWO) }, tags = { "highway=residential" }),

                    // Fifth example
                    @Edge(id = "3740", coordinates = { @Loc(value = THIRTYSEVEN),
                            @Loc(value = FORTY) }, tags = { "highway=tertiary" }),
                    @Edge(id = "4039", coordinates = { @Loc(value = FORTY),
                            @Loc(value = THIRTYNINE) }, tags = { "highway=tertiary" }),
                    @Edge(id = "3938", coordinates = { @Loc(value = THIRTYNINE),
                            @Loc(value = THIRTYEIGHT) }, tags = { "highway=tertiary" }),

                    // Sixth example
                    @Edge(id = "4741", coordinates = { @Loc(value = FORTYSEVEN),
                            @Loc(value = FORTYONE) }, tags = { "highway=secondary" }),
                    @Edge(id = "4143", coordinates = { @Loc(value = FORTYONE),
                            @Loc(value = FORTYTHREE) }, tags = { "highway=secondary" }),
                    @Edge(id = "4345", coordinates = { @Loc(value = FORTYTHREE),
                            @Loc(value = FORTYFIVE) }, tags = { "highway=secondary" }),
                    @Edge(id = "4644", coordinates = { @Loc(value = FORTYSIX),
                            @Loc(value = FORTYFOUR) }, tags = { "highway=secondary" }),
                    @Edge(id = "4442", coordinates = { @Loc(value = FORTYFOUR),
                            @Loc(value = FORTYTWO) }, tags = { "highway=secondary" }),
                    @Edge(id = "4248", coordinates = { @Loc(value = FORTYTWO),
                            @Loc(value = FORTYEIGHT) }, tags = { "highway=secondary" }),
                    @Edge(id = "4142", coordinates = { @Loc(value = FORTYONE),
                            @Loc(value = FORTYTWO) }, tags = { "highway=tertiary" }),
                    @Edge(id = "4443", coordinates = { @Loc(value = FORTYFOUR),
                            @Loc(value = FORTYTHREE) }, tags = { "highway=tertiary" }),
                    @Edge(id = "4546", coordinates = { @Loc(value = FORTYFIVE),
                            @Loc(value = FORTYSIX) }, tags = { "highway=tertiary" }),

                    // Seventh example
                    @Edge(id = "5450", coordinates = { @Loc(value = FIFTYFOUR),
                            @Loc(value = FIFTY) }, tags = { "highway=primary_link",
                                    "name=Avenida José Pedro Varela" }),
                    @Edge(id = "5049", coordinates = { @Loc(value = FIFTY),
                            @Loc(value = FORTYNINE) }, tags = { "highway=primary_link",
                                    "name=Avenida José Pedro Varela" }),
                    @Edge(id = "5251", coordinates = { @Loc(value = FIFTYTWO),
                            @Loc(value = FIFTYONE) }, tags = { "highway=primary",
                                    "name=Avenida José Pedro Varela" }),
                    @Edge(id = "5352", coordinates = { @Loc(value = FIFTYTHREE),
                            @Loc(value = FIFTYTWO) }, tags = { "highway=primary",
                                    "name=Avenida José Pedro Varela" }),
                    @Edge(id = "5453", coordinates = { @Loc(value = FIFTYFOUR),
                            @Loc(value = FIFTYTHREE) }, tags = { "highway=primary",
                                    "name=Avenida José Pedro Varela" }),
                    @Edge(id = "5556", coordinates = { @Loc(value = FIFTYFIVE),
                            @Loc(value = FIFTYSIX) }, tags = { "highway=primary",
                                    "name=Avenida José Pedro Varela" }),
                    @Edge(id = "5657", coordinates = { @Loc(value = FIFTYSIX),
                            @Loc(value = FIFTYSEVEN) }, tags = { "highway=primary",
                                    "name=Avenida José Pedro Varela" }),
                    @Edge(id = "5758", coordinates = { @Loc(value = FIFTYSEVEN),
                            @Loc(value = FIFTYEIGHT) }, tags = { "highway=primary",
                                    "name=Avenida José Pedro Varela" }),
                    @Edge(id = "5559", coordinates = { @Loc(value = FIFTYFIVE),
                            @Loc(value = FIFTYNINE) }, tags = { "highway=primary_link",
                                    "name=Avenida José Pedro Varela" }),
                    @Edge(id = "5960", coordinates = { @Loc(value = FIFTYNINE),
                            @Loc(value = SIXTY) }, tags = { "highway=primary_link",
                                    "name=Avenida José Pedro Varela" }),
                    @Edge(id = "4952", coordinates = { @Loc(value = FORTYNINE),
                            @Loc(value = FIFTYTWO) }, tags = { "highway=primary",
                                    "name=Avenida Dámaso Antonio Larrañaga" }),
                    @Edge(id = "5256", coordinates = { @Loc(value = FIFTYTWO),
                            @Loc(value = FIFTYSIX) }, tags = { "highway=primary",
                                    "name=Avenida Dámaso Antonio Larrañaga" }),
                    @Edge(id = "5659", coordinates = { @Loc(value = FIFTYSIX),
                            @Loc(value = FIFTYNINE) }, tags = { "highway=primary",
                                    "name=Avenida Dámaso Antonio Larrañaga" }),
                    @Edge(id = "6057", coordinates = { @Loc(value = SIXTY),
                            @Loc(value = FIFTYSEVEN) }, tags = { "highway=primary",
                                    "name=Avenida Dámaso Antonio Larrañaga" }),
                    @Edge(id = "5753", coordinates = { @Loc(value = FIFTYSEVEN),
                            @Loc(value = FIFTYTHREE) }, tags = { "highway=primary",
                                    "name=Avenida Dámaso Antonio Larrañaga" }),
                    @Edge(id = "5350", coordinates = { @Loc(value = FIFTYTHREE),
                            @Loc(value = FIFTY) }, tags = { "highway=primary",
                                    "name=Avenida Dámaso Antonio Larrañaga" }),

                    // Eighth example
                    @Edge(id = "6162", coordinates = { @Loc(value = SIXTYONE),
                            @Loc(value = SIXTYTWO) }, tags = { "highway=primary",
                                    "name=La Matouriennea" }),
                    @Edge(id = "-6162", coordinates = { @Loc(value = SIXTYTWO),
                            @Loc(value = SIXTYONE) }, tags = { "highway=primary",
                                    "name=La Matouriennea" }),
                    @Edge(id = "6362", coordinates = { @Loc(value = SIXTYTHREE),
                            @Loc(value = SIXTYTWO) }, tags = { "highway=primary",
                                    "name=La Matouriennea" }),
                    @Edge(id = "6463", coordinates = { @Loc(value = SIXTYFOUR),
                            @Loc(value = SIXTYTHREE) }, tags = { "highway=primary",
                                    "name=La Matouriennea" }),
                    @Edge(id = "6265", coordinates = { @Loc(value = SIXTYTWO),
                            @Loc(value = SIXTYFIVE) }, tags = { "highway=primary",
                                    "name=La Matouriennea" }),

                    // Ninth example
                    @Edge(id = "6667", coordinates = { @Loc(value = SIXTYSIX),
                            @Loc(value = SIXTYSEVEN) }, tags = { "man_made=pier", "mooring=yes" }),
                    @Edge(id = "6768", coordinates = { @Loc(value = SIXTYSEVEN),
                            @Loc(value = SIXTYEIGHT) }, tags = { "man_made=pier", "mooring=yes" }),
                    @Edge(id = "6869", coordinates = { @Loc(value = SIXTYEIGHT),
                            @Loc(value = SIXTYNINE) }, tags = { "man_made=pier", "mooring=yes" }),

                    // Eleventh example
                    @Edge(id = "7879", coordinates = { @Loc(value = SEVENTY_EIGHT),
                            @Loc(value = SEVENTY_NINE) }, tags = { "highway=residential",
                                    "name=Herlev Hovedgade" }),
                    @Edge(id = "7980", coordinates = { @Loc(value = SEVENTY_NINE),
                            @Loc(value = EIGHTY) }, tags = { "highway=residential",
                                    "name=Rytmevej" }),
                    @Edge(id = "8081", coordinates = { @Loc(value = EIGHTY),
                            @Loc(value = EIGHTY_ONE) }, tags = { "highway=residential",
                                    "name=Klokkedybet" }),
                    @Edge(id = "8281", coordinates = { @Loc(value = EIGHTY_TWO),
                            @Loc(value = EIGHTY_ONE) }, tags = { "highway=cycleway", "oneway=yes",
                                    "name=Herlev Hovedgade" }),
                    @Edge(id = "8380", coordinates = { @Loc(value = EIGHTY_THREE),
                            @Loc(value = EIGHTY) }, tags = { "highway=secondary", "oneway=yes" }),
                    @Edge(id = "8479", coordinates = { @Loc(value = EIGHTY_FOUR),
                            @Loc(value = SEVENTY_NINE) }, tags = { "highway=secondary",
                                    "oneway=yes" }),
                    @Edge(id = "8579", coordinates = { @Loc(value = EIGHTY_FIVE),
                            @Loc(value = SEVENTY_NINE) }, tags = { "highway=secondary",
                                    "oneway=yes" }),
                    @Edge(id = "8086", coordinates = { @Loc(value = EIGHTY),
                            @Loc(value = EIGHTY_SIX) }, tags = { "highway=secondary",
                                    "oneway=yes" }),
                    @Edge(id = "8187", coordinates = { @Loc(value = EIGHTY_ONE),
                            @Loc(value = EIGHTY_SEVEN) }, tags = { "highway=cycleway", "oneway=yes",
                                    "name=Herlev Hovedgade" }),
                    // Twelfth example
                    @Edge(id = "8889", coordinates = { @Loc(value = EIGHTY_EIGHT),
                            @Loc(value = EIGHTY_NINE) }, tags = { "highway=tertiary" }),
                    @Edge(id = "8988", coordinates = { @Loc(value = EIGHTY_NINE),
                            @Loc(value = EIGHTY_EIGHT) }, tags = { "highway=tertiary" }),
                    @Edge(id = "8891", coordinates = { @Loc(value = EIGHTY_EIGHT),
                            @Loc(value = NINETY_ONE) }, tags = { "highway=service", "oneway=yes" }),
                    @Edge(id = "9089", coordinates = { @Loc(value = NINETY),
                            @Loc(value = EIGHTY_NINE) }, tags = { "highway=service",
                                    "oneway=yes" }) })
    private Atlas atlas;

    @TestAtlas(

            nodes = { @Node(id = "70", coordinates = @Loc(value = SEVENTY)),
                    @Node(id = "71", coordinates = @Loc(value = SEVENTY_ONE)),
                    @Node(id = "72", coordinates = @Loc(value = SEVENTY_TWO)),
                    @Node(id = "73", coordinates = @Loc(value = SEVENTY_THREE)),
                    @Node(id = "74", coordinates = @Loc(value = SEVENTY_FOUR)),
                    @Node(id = "75", coordinates = @Loc(value = SEVENTY_FIVE)),
                    @Node(id = "76", coordinates = @Loc(value = SEVENTY_SIX)),
                    @Node(id = "77", coordinates = @Loc(value = SEVENTY_SEVEN)) },

            edges = {
                    @Edge(id = "1", coordinates = { @Loc(value = SEVENTY_THREE),
                            @Loc(value = SEVENTY_TWO) }, tags = { "highway=secondary",
                                    "name=Livernois Avenue" }),
                    @Edge(id = "-1", coordinates = { @Loc(value = SEVENTY_TWO),
                            @Loc(value = SEVENTY_THREE) }, tags = { "highway=secondary",
                                    "name=Livernois Avenue" }),
                    @Edge(id = "2", coordinates = { @Loc(value = SEVENTY_TWO),
                            @Loc(value = SEVENTY_ONE) }, tags = { "highway=secondary",
                                    "name=Livernois Avenue" }),
                    @Edge(id = "-2", coordinates = { @Loc(value = SEVENTY_ONE),
                            @Loc(value = SEVENTY_TWO) }, tags = { "highway=secondary",
                                    "name=Livernois Avenue" }),
                    @Edge(id = "3", coordinates = { @Loc(value = SEVENTY_ONE),
                            @Loc(value = SEVENTY) }, tags = { "highway=secondary",
                                    "name=Livernois Avenue" }),
                    @Edge(id = "-3", coordinates = { @Loc(value = SEVENTY),
                            @Loc(value = SEVENTY_ONE) }, tags = { "highway=secondary",
                                    "name=Livernois Avenue" }),
                    @Edge(id = "4", coordinates = { @Loc(value = SEVENTY_SEVEN),
                            @Loc(value = SEVENTY_ONE) }, tags = { "highway=primary",
                                    "name=West Davison Avenue", "oneway=yes" }),
                    @Edge(id = "5", coordinates = { @Loc(value = SEVENTY_ONE),
                            @Loc(value = SEVENTY_FOUR) }, tags = { "highway=primary",
                                    "name=West Davison Avenue", "oneway=yes" }),
                    @Edge(id = "6", coordinates = { @Loc(value = SEVENTY_FIVE),
                            @Loc(value = SEVENTY_TWO) }, tags = { "highway=primary",
                                    "name=West Davison Avenue", "oneway=yes" }),
                    @Edge(id = "7", coordinates = { @Loc(value = SEVENTY_TWO),
                            @Loc(value = SEVENTY_SIX) }, tags = { "highway=primary",
                                    "name=West Davison Avenue", "oneway=yes" }) })
    private Atlas complexJunctionAtlas;

    /*
     * Intersections where nearby big nodes overlap
     */
    @TestAtlas(loadFromTextResource = "overlap.atlas.txt.gz")
    private Atlas overlapAtlas;

    /*
     * Intersections that test expansion of big nodes
     */
    @TestAtlas(loadFromTextResource = "expand.atlas.txt.gz")
    private Atlas expandAtlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getComplexJunctionAtlas()
    {
        return this.complexJunctionAtlas;
    }

    public Atlas getExpandBigNodeAtlas()
    {
        return this.expandAtlas;
    }

    public Atlas getOverlapAtlas()
    {
        return this.overlapAtlas;
    }
}
