package org.openstreetmap.atlas.geography.atlas.items.complex.aoi;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Test data for {@link ComplexAreaOfInterestFinderTest}
 *
 * @author sayas01
 */
public class ComplexAreaOfInterestFinderTestRule extends CoreTestRule
{
    private static final String LOCATION_ONE = "1.43958970913, 103.91306306772";
    private static final String LOCATION_TWO = "1.42773565932, 103.90488839864";
    private static final String LOCATION_THREE = "1.43419031086, 103.89654055711";
    private static final String LOCATION_FOUR = "1.41407426416, 103.89600156794";
    private static final String LOCATION_FIVE = "1.40850639889, 103.91621366183";
    private static final String LOCATION_SIX = "1.39835848123, 103.92322052105";
    private static final String LOCATION_SEVEN = "1.41146994174, 103.94549874009";
    private static final String LOCATION_EIGHT = "1.41155974601, 103.96373454036";
    private static final String LOCATION_NINE = "1.43419031086, 103.97038207346";
    private static final String LOCATION_TEN = "1.45116308784, 103.96454302412";
    private static final String LOCATION_ELEVEN = "1.45233052286, 103.93786306018";
    private static final String LOCATION_TWELVE = "1.45367756252, 103.91845945004";
    private static final String LOCATION_THIRTEEN = "1.41193016458, 103.91459020371";
    private static final String LOCATION_FOURTEEN = "1.41624076477, 103.94180915682";
    private static final String LOCATION_FIFTEEN = "1.42638860433, 103.94621090171";
    private static final String LOCATION_SIXTEEN = "1.43141760561, 103.96399754433";
    private static final String LOCATION_SEVENTEEN = "1.44273281808, 103.95447540232";
    private static final String LOCATION_EIGHTEEN = "1.43842226756, 103.93857522179";
    private static final String LOCATION_NINETEEN = "1.44839040324, 103.9256394817";
    private static final String LOCATION_TWENTY = "1.4507140742, 103.9069610144";

    @TestAtlas(points = { @Point(id = "39008", coordinates = @Loc(value = LOCATION_TWENTY)),
            @Point(id = "39009", coordinates = @Loc(value = LOCATION_THREE)),
            @Point(id = "39011", coordinates = @Loc(value = LOCATION_FOUR)),
            @Point(id = "39013", coordinates = @Loc(value = LOCATION_FIVE)),
            @Point(id = "39015", coordinates = @Loc(value = LOCATION_SIX)),
            @Point(id = "38985", coordinates = @Loc(value = LOCATION_ONE)),
            @Point(id = "39019", coordinates = @Loc(value = LOCATION_EIGHT)),
            @Point(id = "38988", coordinates = @Loc(value = LOCATION_TWO)),
            @Point(id = "39021", coordinates = @Loc(value = LOCATION_NINE)),
            @Point(id = "39023", coordinates = @Loc(value = LOCATION_TEN)),
            @Point(id = "38992", coordinates = @Loc(value = LOCATION_THIRTEEN)),
            @Point(id = "39025", coordinates = @Loc(value = LOCATION_ELEVEN)),
            @Point(id = "38994", coordinates = @Loc(value = LOCATION_FOURTEEN)),
            @Point(id = "39027", coordinates = @Loc(value = LOCATION_TWELVE)),
            @Point(id = "38996", coordinates = @Loc(value = LOCATION_FIFTEEN)),
            @Point(id = "38998", coordinates = @Loc(value = LOCATION_SIXTEEN)),
            @Point(id = "39017", coordinates = @Loc(value = LOCATION_SEVEN)),
            @Point(id = "39000", coordinates = @Loc(value = LOCATION_SEVENTEEN)),
            @Point(id = "39002", coordinates = @Loc(value = LOCATION_EIGHTEEN)),
            @Point(id = "39004", coordinates = @Loc(value = LOCATION_NINETEEN)) }, areas = {
                    @Area(id = "39010", coordinates = { @Loc(value = LOCATION_TWENTY),
                            @Loc(value = LOCATION_THREE), @Loc(value = LOCATION_FOUR),
                            @Loc(value = LOCATION_FIVE), @Loc(value = LOCATION_SIX),
                            @Loc(value = LOCATION_SEVEN), @Loc(value = LOCATION_EIGHT),
                            @Loc(value = LOCATION_NINE), @Loc(value = LOCATION_TEN),
                            @Loc(value = LOCATION_ELEVEN), @Loc(value = LOCATION_TWELVE),
                            @Loc(value = LOCATION_TWENTY) }),
                    @Area(id = "38989", coordinates = { @Loc(value = LOCATION_ONE),
                            @Loc(value = LOCATION_TWO), @Loc(value = LOCATION_THIRTEEN),
                            @Loc(value = LOCATION_FOURTEEN), @Loc(value = LOCATION_FIFTEEN),
                            @Loc(value = LOCATION_SIXTEEN), @Loc(value = LOCATION_SEVENTEEN),
                            @Loc(value = LOCATION_EIGHTEEN), @Loc(value = LOCATION_NINETEEN),
                            @Loc(value = LOCATION_ONE) }),
                    @Area(id = "38987", coordinates = { @Loc(value = LOCATION_ELEVEN),
                            @Loc(value = LOCATION_TEN), @Loc(value = LOCATION_EIGHTEEN),
                            @Loc(value = LOCATION_SEVENTEEN) }) }, relations = {
                                    @Relation(id = "39190", members = {
                                            @Member(id = "39010", type = "area", role = "outer"),
                                            @Member(id = "38989", type = "area", role = "inner") }, tags = {
                                                    "type=multipolygon",
                                                    "amenity=SCHOOL" }, wkt = "MULTIPOLYGON (((103.906961 1.4507141, 103.8965406 1.4341903, 103.8960016 1.4140743, 103.9162137 1.4085064, 103.9232205 1.3983585, 103.9454987 1.4114699, 103.9637345 1.4115597, 103.9703821 1.4341903, 103.964543 1.4511631, 103.9378631 1.4523305, 103.9184595 1.4536776, 103.906961 1.4507141, 103.906961 1.4507141), (103.9130631 1.4395897, 103.9048884 1.4277357, 103.9145902 1.4119302, 103.9418092 1.4162408, 103.9462109 1.4263886, 103.9639975 1.4314176, 103.9544754 1.4427328, 103.9385752 1.4384223, 103.9256395 1.4483904, 103.9130631 1.4395897, 103.9130631 1.4395897)))"),
                                    @Relation(id = "39990", members = {
                                            @Member(id = "38987", type = "area", role = "outer") }, tags = {
                                                    "type=boundary",
                                                    "landuse=CEMETERY" }, wkt = "MULTIPOLYGON (((103.9378631 1.4523305, 103.964543 1.4511631, 103.9385752 1.4384223, 103.9544754 1.4427328, 103.9378631 1.4523305)))") })
    private Atlas multipolygonAOIRelationAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "10020", coordinates = @Loc(value = LOCATION_SEVEN)),
                    @Node(id = "21001", coordinates = @Loc(value = LOCATION_EIGHT)),
                    @Node(id = "31233", coordinates = @Loc(value = LOCATION_NINE)) },
            // edges
            edges = {
                    @Edge(id = "12333", coordinates = { @Loc(value = LOCATION_SEVEN),
                            @Loc(value = LOCATION_EIGHT) }, tags = { "highway=road" }),
                    @Edge(id = "23332", coordinates = { @Loc(value = LOCATION_EIGHT),
                            @Loc(value = LOCATION_NINE) }, tags = { "highway=road" }),
                    @Edge(id = "31223", coordinates = { @Loc(value = LOCATION_NINE),
                            @Loc(value = LOCATION_SEVEN) }, tags = { "highway=road" }) },
            // relations
            relations = { @Relation(id = "89765", members = {
                    @Member(id = "12333", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_FROM),
                    @Member(id = "21001", type = "node", role = RelationTypeTag.RESTRICTION_ROLE_VIA),
                    @Member(id = "31223", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_TO) }, tags = {
                            "restriction=no_u_turn", "landuse=VILLAGE" }) })
    private Atlas nonMultipolygonAOIRelationAtlas;

    @TestAtlas(points = { @Point(id = "39008", coordinates = @Loc(value = LOCATION_TWENTY)),
            @Point(id = "39009", coordinates = @Loc(value = LOCATION_THREE)),
            @Point(id = "39011", coordinates = @Loc(value = LOCATION_FOUR)),
            @Point(id = "39013", coordinates = @Loc(value = LOCATION_FIVE)),
            @Point(id = "39015", coordinates = @Loc(value = LOCATION_SIX)),
            @Point(id = "38985", coordinates = @Loc(value = LOCATION_ONE)),
            @Point(id = "39019", coordinates = @Loc(value = LOCATION_EIGHT)),
            @Point(id = "38988", coordinates = @Loc(value = LOCATION_TWO)),
            @Point(id = "39021", coordinates = @Loc(value = LOCATION_NINE)),
            @Point(id = "39023", coordinates = @Loc(value = LOCATION_TEN)),
            @Point(id = "38992", coordinates = @Loc(value = LOCATION_THIRTEEN)),
            @Point(id = "39025", coordinates = @Loc(value = LOCATION_ELEVEN)),
            @Point(id = "38994", coordinates = @Loc(value = LOCATION_FOURTEEN)),
            @Point(id = "39027", coordinates = @Loc(value = LOCATION_TWELVE)),
            @Point(id = "38996", coordinates = @Loc(value = LOCATION_FIFTEEN)),
            @Point(id = "38998", coordinates = @Loc(value = LOCATION_SIXTEEN)),
            @Point(id = "39017", coordinates = @Loc(value = LOCATION_SEVEN)),
            @Point(id = "39000", coordinates = @Loc(value = LOCATION_SEVENTEEN)),
            @Point(id = "39002", coordinates = @Loc(value = LOCATION_EIGHTEEN)),
            @Point(id = "39004", coordinates = @Loc(value = LOCATION_NINETEEN)) }, areas = {
                    @Area(id = "39010", coordinates = { @Loc(value = LOCATION_TWENTY),
                            @Loc(value = LOCATION_THREE), @Loc(value = LOCATION_FOUR),
                            @Loc(value = LOCATION_FIVE), @Loc(value = LOCATION_SIX),
                            @Loc(value = LOCATION_SEVEN), @Loc(value = LOCATION_EIGHT),
                            @Loc(value = LOCATION_NINE), @Loc(value = LOCATION_TEN),
                            @Loc(value = LOCATION_ELEVEN), @Loc(value = LOCATION_TWELVE),
                            @Loc(value = LOCATION_TWENTY) }),
                    @Area(id = "38989", coordinates = { @Loc(value = LOCATION_ONE),
                            @Loc(value = LOCATION_TWO), @Loc(value = LOCATION_THIRTEEN),
                            @Loc(value = LOCATION_FOURTEEN), @Loc(value = LOCATION_FIFTEEN),
                            @Loc(value = LOCATION_SIXTEEN), @Loc(value = LOCATION_SEVENTEEN),
                            @Loc(value = LOCATION_EIGHTEEN), @Loc(value = LOCATION_NINETEEN),
                            @Loc(value = LOCATION_ONE) }, tags = "leisure=PARK"),
                    @Area(id = "38987", coordinates = { @Loc(value = LOCATION_ELEVEN),
                            @Loc(value = LOCATION_TEN), @Loc(value = LOCATION_EIGHTEEN),
                            @Loc(value = LOCATION_SEVENTEEN) }, tags = "tourism=ZOO") })
    private Atlas aoiAreaAtlas;

    @TestAtlas(points = { @Point(id = "39008", coordinates = @Loc(value = LOCATION_TWENTY)),
            @Point(id = "39009", coordinates = @Loc(value = LOCATION_THREE)),
            @Point(id = "39011", coordinates = @Loc(value = LOCATION_FOUR)),
            @Point(id = "39013", coordinates = @Loc(value = LOCATION_FIVE)),
            @Point(id = "39015", coordinates = @Loc(value = LOCATION_SIX)),
            @Point(id = "38985", coordinates = @Loc(value = LOCATION_ONE)),
            @Point(id = "39019", coordinates = @Loc(value = LOCATION_EIGHT)),
            @Point(id = "38988", coordinates = @Loc(value = LOCATION_TWO)),
            @Point(id = "39021", coordinates = @Loc(value = LOCATION_NINE)),
            @Point(id = "39023", coordinates = @Loc(value = LOCATION_TEN)),
            @Point(id = "38992", coordinates = @Loc(value = LOCATION_THIRTEEN)),
            @Point(id = "39025", coordinates = @Loc(value = LOCATION_ELEVEN)),
            @Point(id = "38994", coordinates = @Loc(value = LOCATION_FOURTEEN)),
            @Point(id = "39027", coordinates = @Loc(value = LOCATION_TWELVE)),
            @Point(id = "38996", coordinates = @Loc(value = LOCATION_FIFTEEN)),
            @Point(id = "38998", coordinates = @Loc(value = LOCATION_SIXTEEN)),
            @Point(id = "39017", coordinates = @Loc(value = LOCATION_SEVEN)),
            @Point(id = "39000", coordinates = @Loc(value = LOCATION_SEVENTEEN)),
            @Point(id = "39002", coordinates = @Loc(value = LOCATION_EIGHTEEN)),
            @Point(id = "39004", coordinates = @Loc(value = LOCATION_NINETEEN)) }, areas = {
                    @Area(id = "39010", coordinates = { @Loc(value = LOCATION_TWENTY),
                            @Loc(value = LOCATION_THREE), @Loc(value = LOCATION_FOUR),
                            @Loc(value = LOCATION_FIVE), @Loc(value = LOCATION_SIX),
                            @Loc(value = LOCATION_SEVEN), @Loc(value = LOCATION_EIGHT),
                            @Loc(value = LOCATION_NINE), @Loc(value = LOCATION_TEN),
                            @Loc(value = LOCATION_ELEVEN), @Loc(value = LOCATION_TWELVE),
                            @Loc(value = LOCATION_TWENTY) }),
                    @Area(id = "38989", coordinates = { @Loc(value = LOCATION_ONE),
                            @Loc(value = LOCATION_TWO), @Loc(value = LOCATION_THIRTEEN),
                            @Loc(value = LOCATION_FOURTEEN), @Loc(value = LOCATION_FIFTEEN),
                            @Loc(value = LOCATION_SIXTEEN), @Loc(value = LOCATION_SEVENTEEN),
                            @Loc(value = LOCATION_EIGHTEEN), @Loc(value = LOCATION_NINETEEN),
                            @Loc(value = LOCATION_ONE) }),
                    @Area(id = "38987", coordinates = { @Loc(value = LOCATION_ELEVEN),
                            @Loc(value = LOCATION_TEN), @Loc(value = LOCATION_EIGHTEEN),
                            @Loc(value = LOCATION_SEVENTEEN) }),
                    @Area(id = "45677", coordinates = { @Loc(value = LOCATION_EIGHT),
                            @Loc(value = LOCATION_TEN), @Loc(value = LOCATION_FIVE),
                            @Loc(value = LOCATION_FOUR) }, tags = {
                                    "amenity=SCHOOL" }) }, relations = {
                                            @Relation(id = "39190", members = {
                                                    @Member(id = "39010", type = "area", role = "outer"),
                                                    @Member(id = "38989", type = "area", role = "inner") }, tags = {
                                                            "type=multipolygon",
                                                            "amenity=PARKING" }, wkt = "MULTIPOLYGON (((103.906961 1.4507141, 103.8965406 1.4341903, 103.8960016 1.4140743, 103.9162137 1.4085064, 103.9232205 1.3983585, 103.9454987 1.4114699, 103.9637345 1.4115597, 103.9703821 1.4341903, 103.964543 1.4511631, 103.9378631 1.4523305, 103.9184595 1.4536776, 103.906961 1.4507141, 103.906961 1.4507141), (103.9130631 1.4395897, 103.9048884 1.4277357, 103.9145902 1.4119302, 103.9418092 1.4162408, 103.9462109 1.4263886, 103.9639975 1.4314176, 103.9544754 1.4427328, 103.9385752 1.4384223, 103.9256395 1.4483904, 103.9130631 1.4395897, 103.9130631 1.4395897)))"),
                                            @Relation(id = "39990", members = {
                                                    @Member(id = "38987", type = "area", role = "outer") }, tags = {
                                                            "type=boundary",
                                                            "landuse=VINEYARD" }, wkt = "MULTIPOLYGON (((103.9378631 1.4523305, 103.964543 1.4511631, 103.9385752 1.4384223, 103.9544754 1.4427328, 103.9378631 1.4523305)))") })
    private Atlas complexAOIWithRelationsAndAreas;

    @TestAtlas(points = { @Point(id = "39008", coordinates = @Loc(value = LOCATION_TWENTY)),
            @Point(id = "39009", coordinates = @Loc(value = LOCATION_THREE)),
            @Point(id = "39011", coordinates = @Loc(value = LOCATION_FOUR)),
            @Point(id = "39013", coordinates = @Loc(value = LOCATION_FIVE)),
            @Point(id = "39015", coordinates = @Loc(value = LOCATION_SIX)),
            @Point(id = "38985", coordinates = @Loc(value = LOCATION_ONE)),
            @Point(id = "39019", coordinates = @Loc(value = LOCATION_EIGHT)),
            @Point(id = "38988", coordinates = @Loc(value = LOCATION_TWO)),
            @Point(id = "39021", coordinates = @Loc(value = LOCATION_NINE)),
            @Point(id = "39023", coordinates = @Loc(value = LOCATION_TEN)),
            @Point(id = "38992", coordinates = @Loc(value = LOCATION_THIRTEEN)),
            @Point(id = "39025", coordinates = @Loc(value = LOCATION_ELEVEN)),
            @Point(id = "38994", coordinates = @Loc(value = LOCATION_FOURTEEN)),
            @Point(id = "39027", coordinates = @Loc(value = LOCATION_TWELVE)),
            @Point(id = "38996", coordinates = @Loc(value = LOCATION_FIFTEEN)),
            @Point(id = "38998", coordinates = @Loc(value = LOCATION_SIXTEEN)),
            @Point(id = "39017", coordinates = @Loc(value = LOCATION_SEVEN)),
            @Point(id = "39000", coordinates = @Loc(value = LOCATION_SEVENTEEN)),
            @Point(id = "39002", coordinates = @Loc(value = LOCATION_EIGHTEEN)),
            @Point(id = "39004", coordinates = @Loc(value = LOCATION_NINETEEN)) }, areas = {
                    @Area(id = "39010", coordinates = { @Loc(value = LOCATION_TWENTY),
                            @Loc(value = LOCATION_THREE), @Loc(value = LOCATION_FOUR),
                            @Loc(value = LOCATION_FIVE), @Loc(value = LOCATION_SIX),
                            @Loc(value = LOCATION_SEVEN), @Loc(value = LOCATION_EIGHT),
                            @Loc(value = LOCATION_NINE), @Loc(value = LOCATION_TEN),
                            @Loc(value = LOCATION_ELEVEN), @Loc(value = LOCATION_TWELVE),
                            @Loc(value = LOCATION_TWENTY) }),
                    @Area(id = "38989", coordinates = { @Loc(value = LOCATION_ONE),
                            @Loc(value = LOCATION_TWO), @Loc(value = LOCATION_THIRTEEN),
                            @Loc(value = LOCATION_FOURTEEN), @Loc(value = LOCATION_FIFTEEN),
                            @Loc(value = LOCATION_SIXTEEN), @Loc(value = LOCATION_SEVENTEEN),
                            @Loc(value = LOCATION_EIGHTEEN), @Loc(value = LOCATION_NINETEEN),
                            @Loc(value = LOCATION_ONE) }),
                    @Area(id = "38987", coordinates = { @Loc(value = LOCATION_ELEVEN),
                            @Loc(value = LOCATION_TEN), @Loc(value = LOCATION_EIGHTEEN),
                            @Loc(value = LOCATION_SEVENTEEN) }) }, relations = {
                                    @Relation(id = "39190", members = {
                                            @Member(id = "39010", type = "area", role = "outer"),
                                            @Member(id = "38989", type = "area", role = "inner") }, tags = {
                                                    "type=building",
                                                    "amenity=SCHOOL" }, wkt = "MULTIPOLYGON (((103.906961 1.4507141, 103.8965406 1.4341903, 103.8960016 1.4140743, 103.9162137 1.4085064, 103.9232205 1.3983585, 103.9454987 1.4114699, 103.9637345 1.4115597, 103.9703821 1.4341903, 103.964543 1.4511631, 103.9378631 1.4523305, 103.9184595 1.4536776, 103.906961 1.4507141, 103.906961 1.4507141), (103.9130631 1.4395897, 103.9048884 1.4277357, 103.9145902 1.4119302, 103.9418092 1.4162408, 103.9462109 1.4263886, 103.9639975 1.4314176, 103.9544754 1.4427328, 103.9385752 1.4384223, 103.9256395 1.4483904, 103.9130631 1.4395897, 103.9130631 1.4395897)))") })
    private Atlas buildingAOIRelationAtlas;

    public Atlas getAoiAreaAtlas()
    {
        return this.aoiAreaAtlas;
    }

    public Atlas getBuildingAOIRelationAtlas()
    {
        return this.buildingAOIRelationAtlas;
    }

    public Atlas getComplexAOIWithRelationsAndAreas()
    {
        return this.complexAOIWithRelationsAndAreas;
    }

    public Atlas getMultipolygonAOIRelationAtlas()
    {
        return this.multipolygonAOIRelationAtlas;
    }

    public Atlas getNonMultipolygonAOIRelationAtlas()
    {
        return this.nonMultipolygonAOIRelationAtlas;
    }
}
