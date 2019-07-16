package org.openstreetmap.atlas.utilities.testing;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Building;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Example test case rule showing how annotation processing can simplify test code, and
 * {@link TestAtlasHandler} unit test rule.
 *
 * @author cstaylor
 */
public class AtlasTestCaseRule extends CoreTestRule
{
    private static final String TEST_1 = "50.2722020136163,7.64883186874144";
    private static final String TEST_2 = "50.2718530548889,7.64920738356862";
    private static final String TEST_3 = "50.2722620153118,7.64926173439887";

    @TestAtlas(areas = { @Area(id = "1234", tags = { "name=17", "building=apartments",
            "addr:street=Expreso V Centenario" }, coordinates = { @Loc("18.4762695,-69.9118829"),
                    @Loc(lon = -69.3320129, lat = 19.2025913) }) })
    private Atlas atlas;

    @TestAtlas(areas = { @Area(tags = { "name=17", "building=apartments",
            "addr:street=Expreso V Centenario" }, coordinates = {
                    @Loc(lon = -69.9118829, lat = 18.4762695),
                    @Loc(lon = -69.3320129, lat = 19.2025913) }) })
    private Atlas atlas2;

    /**
     * A quick way for testing non-geometry related information about an area
     */
    @TestAtlas(areas = { @Area(tags = { "name=42" }) })
    private Atlas atlas3;

    @TestAtlas(
            // Points
            points = { @Point(coordinates = @Loc(TEST_2), tags = { "name=Marksburg",
                    "historic=castle" }) },
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST_1)), @Node(coordinates = @Loc(TEST_3)) },
            // Edges
            edges = { @Edge(coordinates = { @Loc(TEST_1), @Loc(TEST_3) }, tags = {
                    "highway=footway" }) },
            // Lines
            lines = { @Line(coordinates = { @Loc(TEST_1), @Loc(TEST_3) }, tags = {
                    "barrier=retaining_wall" }) },
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(TEST_1), @Loc(TEST_2),
                    @Loc(TEST_3) }, tags = { "building=yes" }) },
            // Relations
            relations = { @Relation(members = {
                    @Member(id = "1000000", type = "area", role = "outer") }, tags = {
                            "type=multipolygon", "building=yes" }) },
            // Buildings
            buildings = { @Building(outer = @Area(coordinates = { @Loc(TEST_1), @Loc(TEST_2),
                    @Loc(TEST_3) }), tags = "building=yes") })
    private Atlas allAnnotationsAtlas;

    @TestAtlas(
            // Points
            points = { @Point(coordinates = @Loc(TEST_2), tags = { "name=Marksburg",
                    "historic=castle" }) },
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST_1)), @Node(coordinates = @Loc(TEST_3)) },
            // Edges
            edges = { @Edge(coordinates = { @Loc(TEST_1), @Loc(TEST_3) }, tags = {
                    "highway=footway" }) },
            // Lines
            lines = { @Line(coordinates = { @Loc(TEST_1), @Loc(TEST_3) }, tags = {
                    "barrier=retaining_wall" }) },
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(TEST_1), @Loc(TEST_2),
                    @Loc(TEST_3) }, tags = { "building=yes" }) },
            // Relations
            relations = { @Relation(members = {
                    @Member(id = "1000000", type = "area", role = "outer") }, tags = {
                            "type=multipolygon", "building=yes" }) },
            // Buildings
            buildings = { @Building(outer = @Area(coordinates = { @Loc(TEST_1), @Loc(TEST_2),
                    @Loc(TEST_3) }), tags = "building=yes") },
            // ISO code
            iso = "DEU")
    private Atlas allAnnotationsISOAtlas;

    @TestAtlas(
            // Points
            points = { @Point(coordinates = @Loc(TEST_2), tags = { "name=Marksburg",
                    "historic=castle", "iso_country_code=BBH" }) },
            // Nodes
            nodes = { @Node(coordinates = @Loc(TEST_1)), @Node(coordinates = @Loc(TEST_3)) },
            // Edges
            edges = { @Edge(coordinates = { @Loc(TEST_1), @Loc(TEST_3) }, tags = {
                    "highway=footway" }) },
            // Lines
            lines = { @Line(coordinates = { @Loc(TEST_1), @Loc(TEST_3) }, tags = {
                    "barrier=retaining_wall" }) },
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(TEST_1), @Loc(TEST_2),
                    @Loc(TEST_3) }, tags = { "building=yes" }) },
            // Relations
            relations = { @Relation(members = {
                    @Member(id = "1000000", type = "area", role = "outer") }, tags = {
                            "type=multipolygon", "building=yes" }) },
            // Buildings
            buildings = { @Building(outer = @Area(coordinates = { @Loc(TEST_1), @Loc(TEST_2),
                    @Loc(TEST_3) }), tags = "building=yes") },
            // ISO code
            iso = "DEU")
    private Atlas allAnnotationsISOOverrideAtlas;

    public Atlas allAnnotationsAtlas()
    {
        return this.allAnnotationsAtlas;
    }

    public Atlas allAnnotationsISOAtlas()
    {
        return this.allAnnotationsISOAtlas;
    }

    public Atlas allAnnotationsISOOverrideAtlas()
    {
        return this.allAnnotationsISOOverrideAtlas;
    }

    public Atlas atlas()
    {
        return this.atlas;
    }

    public Atlas atlas2()
    {
        return this.atlas2;
    }

    public Atlas atlas3()
    {
        return this.atlas3;
    }
}
