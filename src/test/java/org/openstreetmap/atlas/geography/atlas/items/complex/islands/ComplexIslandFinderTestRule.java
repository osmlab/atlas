package org.openstreetmap.atlas.geography.atlas.items.complex.islands;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author sbhalekar
 */
public class ComplexIslandFinderTestRule extends CoreTestRule
{
    public static final String ISLAND_ID_STRING = "100000000";
    public static final String RELATION_ID_STRING_ONE = "200000000";

    private static final String ONE = "40.0000001, -80.0000003";
    private static final String TWO = "40.0000001, -80.0000001";
    private static final String THREE = "40.0000003, -80.0000001";
    private static final String FOUR = "40.0000003, -80.0000003";

    @TestAtlas(areas = { @TestAtlas.Area(id = ISLAND_ID_STRING, coordinates = {
            @TestAtlas.Loc(value = ONE), @TestAtlas.Loc(value = TWO), @TestAtlas.Loc(value = THREE),
            @TestAtlas.Loc(value = FOUR) }) }, relations = {
                    @TestAtlas.Relation(id = RELATION_ID_STRING_ONE, tags = { "type=multipolygon",
                            "place=islet" }, members = {
                                    @TestAtlas.Relation.Member(id = ISLAND_ID_STRING, type = "area", role = "outer") }, wkt = "MULTIPOLYGON (((-80.0000003 40.0000001, -80.0000001 40.0000001, -80.0000001 40.0000003, -80.0000003 40.0000003, -80.0000003 40.0000001)))") })
    private Atlas atlasWithValidIsletRelation;

    @TestAtlas(areas = { @TestAtlas.Area(id = ISLAND_ID_STRING, coordinates = {
            @TestAtlas.Loc(value = ONE), @TestAtlas.Loc(value = TWO), @TestAtlas.Loc(value = THREE),
            @TestAtlas.Loc(value = FOUR) }, tags = { "natural=island" }) })
    private Atlas atlasWithValidIslandArea;

    @TestAtlas(areas = { @TestAtlas.Area(id = ISLAND_ID_STRING, coordinates = {
            @TestAtlas.Loc(value = ONE), @TestAtlas.Loc(value = TWO), @TestAtlas.Loc(value = THREE),
            @TestAtlas.Loc(value = FOUR) }, tags = { "natural=islet" }) })
    private Atlas atlasWithInvalidIslandArea;

    public Atlas getAtlasWithInvalidAreaIsland()
    {
        return this.atlasWithInvalidIslandArea;
    }

    public Atlas getAtlasWithValidAreaIsland()
    {
        return this.atlasWithValidIslandArea;
    }

    public Atlas getValidIsletRelationAtlas()
    {
        return this.atlasWithValidIsletRelation;
    }
}
