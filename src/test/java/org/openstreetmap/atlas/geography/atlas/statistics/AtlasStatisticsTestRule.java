package org.openstreetmap.atlas.geography.atlas.statistics;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Test rule for {@link AtlasStatisticsTest}
 *
 * @author matthieun
 */
public class AtlasStatisticsTestRule extends CoreTestRule
{
    @TestAtlas(points = {
            @Point(id = "1", coordinates = @Loc(value = Location.TEST_3_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "2", coordinates = @Loc(value = Location.TEST_2_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "3", coordinates = @Loc(value = Location.TEST_6_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "4", coordinates = @Loc(value = Location.TEST_7_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "5", coordinates = @Loc(value = Location.TEST_4_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "6", coordinates = @Loc(value = Location.TEST_1_COORDINATES), tags = {
                    "addr:city=Cupertino" }),
            @Point(id = "7", coordinates = @Loc(value = Location.TEST_5_COORDINATES), tags = {
                    "addr:city=Cupertino" }), },

            nodes = {
                    @Node(id = "123", coordinates = @Loc(value = Location.TEST_6_COORDINATES), tags = {
                            "highway=turning_circle" }),
                    @Node(id = "1234", coordinates = @Loc(value = Location.TEST_5_COORDINATES), tags = {
                            "highway=turning_circle" }),
                    @Node(id = "12345", coordinates = @Loc(value = Location.TEST_2_COORDINATES), tags = {
                            "highway=turning_circle" }), },

            lines = {
                    @Line(id = "32", coordinates = { @Loc(value = Location.TEST_5_COORDINATES),
                            @Loc(value = Location.TEST_1_COORDINATES) }, tags = { "power=line" }),
                    @Line(id = "23", coordinates = { @Loc(value = Location.TEST_3_COORDINATES),
                            @Loc(value = Location.TEST_2_COORDINATES),
                            @Loc(value = Location.TEST_4_COORDINATES) }, tags = {
                                    "aeroway=runway" }),
                    @Line(id = "24", coordinates = { @Loc(value = Location.TEST_7_COORDINATES),
                            @Loc(value = Location.TEST_4_COORDINATES),
                            @Loc(value = Location.TEST_6_COORDINATES),
                            @Loc(value = Location.TEST_2_COORDINATES) }, tags = {
                                    "natural=coastline", "waterway=canal" }) },

            edges = {
                    @Edge(id = "9", coordinates = { @Loc(value = Location.TEST_6_COORDINATES),
                            @Loc(value = Location.TEST_5_COORDINATES) }, tags = { "highway=primary",
                                    "name=edge9", "surface=concrete", "lanes=3" }),
                    @Edge(id = "-9", coordinates = { @Loc(value = Location.TEST_5_COORDINATES),
                            @Loc(value = Location.TEST_6_COORDINATES) }, tags = { "highway=primary",
                                    "name=edge_9", "surface=gravel" }),
                    @Edge(id = "98", coordinates = { @Loc(value = Location.TEST_5_COORDINATES),
                            @Loc(value = Location.TEST_2_COORDINATES) }, tags = {
                                    "highway=secondary", "name=edge98", "bridge=movable",
                                    "maxspeed=100" }),
                    @Edge(id = "987", coordinates = { @Loc(value = Location.TEST_2_COORDINATES),
                            @Loc(value = Location.TEST_6_COORDINATES) }, tags = {
                                    "highway=residential", "name=edge987", "tunnel=culvert",
                                    "maxspeed=50 knots" }) },

            areas = {
                    @Area(id = "45", coordinates = { @Loc(value = Location.TEST_6_COORDINATES),
                            @Loc(value = Location.TEST_3_COORDINATES),
                            @Loc(value = Location.TEST_2_COORDINATES) }, tags = {
                                    "leisure=golf_course", "natural=grassland" }),
                    @Area(id = "54", coordinates = { @Loc(value = Location.TEST_5_COORDINATES),
                            @Loc(value = Location.TEST_1_COORDINATES),
                            @Loc(value = Location.TEST_4_COORDINATES),
                            @Loc(value = Location.TEST_6_COORDINATES) }, tags = {
                                    "leisure=swimming_pool", "sport=swimming" }),
                    @Area(id = "4554", coordinates = { @Loc(value = Location.TEST_1_COORDINATES),
                            @Loc(value = Location.TEST_2_COORDINATES),
                            @Loc(value = Location.TEST_3_COORDINATES) }, tags = {
                                    "hello=world" }) },

            relations = {
                    @Relation(id = "1", members = { @Member(id = "9", role = "in", type = "edge"),
                            @Member(id = "1234", role = "node", type = "node"),
                            @Member(id = "-9", role = "out", type = "edge") }, tags = {}),
                    @Relation(id = "2", members = {
                            @Member(id = "45", role = "area", type = "area"),
                            @Member(id = "32", role = "line", type = "line"),
                            @Member(id = "5", role = "pt", type = "point"),
                            @Member(id = "1", role = "rel", type = "relation"),
                            @Member(id = "1234", role = "node", type = "node") }, tags = {}) })
    private Atlas packedAtlas;

    @TestAtlas(loadFromJosmOsmResource = "addressAtlas.josm.osm")
    private Atlas addressAtlas;

    @TestAtlas(loadFromJosmOsmResource = "waterAtlas.josm.osm")
    private Atlas waterAtlas;

    @TestAtlas(loadFromJosmOsmResource = "ferryAtlas.josm.osm")
    private Atlas ferryAtlas;

    @TestAtlas(loadFromJosmOsmResource = "refsAtlas.josm.osm")
    private Atlas refsAtlas;

    public Atlas getAddressAtlas()
    {
        return this.addressAtlas;
    }

    public Atlas getFerryAtlas()
    {
        return this.ferryAtlas;
    }

    public Atlas getPackedAtlas()
    {
        return this.packedAtlas;
    }

    public Atlas getRefsAtlas()
    {
        return this.refsAtlas;
    }

    public Atlas getWaterAtlas()
    {
        return this.waterAtlas;
    }
}
