package org.openstreetmap.atlas.geography.atlas.lightweight;

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
 * A test atlas rule for use with the lightweight atlas object tests
 *
 * @author Taylor Smock
 */
public class LightweightTestAtlasRule extends CoreTestRule
{
    @TestAtlas(points = {
            @Point(coordinates = @Loc(Location.TEST_1_COORDINATES), tags = "amenity=fuel", id = "1000000"),
            @Point(coordinates = @Loc(Location.TEST_2_COORDINATES), tags = "amenity=shop", id = "2000000") }, nodes = {
                    @Node(coordinates = @Loc(Location.TEST_3_COORDINATES), tags = "highway=stop", id = "3000000"),
                    @Node(coordinates = @Loc(Location.TEST_4_COORDINATES), tags = "highway=yield", id = "4000000") }, lines = @Line(coordinates = {
                            @Loc(Location.TEST_1_COORDINATES),
                            @Loc(Location.TEST_2_COORDINATES) }, tags = "waterway=stream", id = "5000000"), edges = @Edge(coordinates = {
                                    @Loc(Location.TEST_3_COORDINATES),
                                    @Loc(Location.TEST_4_COORDINATES) }, tags = "highway=residential", id = "6000000"), areas = @Area(coordinates = {
                                            @Loc(Location.TEST_1_COORDINATES),
                                            @Loc(Location.TEST_2_COORDINATES),
                                            @Loc(Location.TEST_3_COORDINATES),
                                            @Loc(Location.TEST_4_COORDINATES) }, tags = "amenity=swimming_pool", id = "7000000"), relations = @Relation(id = "8000000", members = {
                                                    @Member(id = "1000000", role = "", type = "point"),
                                                    @Member(id = "3000000", type = "node", role = ""),
                                                    @Member(id = "5000000", type = "line", role = "east"),
                                                    @Member(id = "6000000", type = "edge", role = "west"),
                                                    @Member(id = "7000000", type = "area", role = "pool") }))
    private Atlas atlas;

    /**
     * Get the atlas for testing
     *
     * @return An atlas with all types of geography types
     */
    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
