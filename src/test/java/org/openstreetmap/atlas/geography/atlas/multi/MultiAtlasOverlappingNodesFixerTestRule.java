package org.openstreetmap.atlas.geography.atlas.multi;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Used for atlas generation for test cases in {@link MultiAtlasOverlappingNodesFixerTest}
 *
 * @author mkalender
 */
public class MultiAtlasOverlappingNodesFixerTestRule extends CoreTestRule
{
    public static final String POINT_1_ID = "1234567891000000";
    public static final String POINT_2_ID = "2234567891000000";
    public static final String POINT_3_ID = "3234567891000000";
    public static final String POINT_4_ID = "4234567891000000";
    public static final String POINT_5_ID = "5234567891000000";
    public static final String POINT_6_ID = "6234567891000000";
    public static final String POINT_7_ID = "7234567891000000";
    public static final String POINT_8_ID = "8234567891000000";
    public static final String POINT_9_ID = "9234567891000000";

    public static final long POINT_1_ID_LONG = Long.parseLong(POINT_1_ID);
    public static final long POINT_2_ID_LONG = Long.parseLong(POINT_2_ID);
    public static final long POINT_3_ID_LONG = Long.parseLong(POINT_3_ID);
    public static final long POINT_4_ID_LONG = Long.parseLong(POINT_4_ID);
    public static final long POINT_5_ID_LONG = Long.parseLong(POINT_5_ID);
    public static final long POINT_6_ID_LONG = Long.parseLong(POINT_6_ID);
    public static final long POINT_7_ID_LONG = Long.parseLong(POINT_7_ID);
    public static final long POINT_8_ID_LONG = Long.parseLong(POINT_8_ID);
    public static final long POINT_9_ID_LONG = Long.parseLong(POINT_9_ID);

    public static final String POINT_1_LOCATION = "37.0,179.0";
    public static final String POINT_2_LOCATION = "37.0,180.0";
    public static final String POINT_3_LOCATION = "37.0,-180.0";
    public static final String POINT_4_LOCATION = "37.0,-179.0";
    public static final String POINT_5_LOCATION = "37.339310,-121.0895660";
    public static final String POINT_6_LOCATION = "37.341310,-121.0705660";
    public static final String POINT_7_LOCATION = "37.341310,-121.0705660";
    public static final String POINT_8_LOCATION = "37.345310,-121.0595660";
    public static final String POINT_9_LOCATION = "37.347310,-121.0395660";

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_1_ID, coordinates = @Loc(value = POINT_1_LOCATION)),
                    @Node(id = POINT_2_ID, coordinates = @Loc(value = POINT_2_LOCATION)) },
            // edges
            edges = { @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_1_LOCATION),
                    @Loc(value = POINT_2_LOCATION) }) })
    private Atlas subAtlasOnAntimeridianEast;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_3_ID, coordinates = @Loc(value = POINT_3_LOCATION)),
                    @Node(id = POINT_4_ID, coordinates = @Loc(value = POINT_4_LOCATION)) },
            // edges
            edges = { @Edge(id = "223456789120001", coordinates = { @Loc(value = POINT_3_LOCATION),
                    @Loc(value = POINT_4_LOCATION) }) })
    private Atlas subAtlasOnAntimeridianWest;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)),
                    @Node(id = POINT_6_ID, coordinates = @Loc(value = POINT_6_LOCATION)) },
            // edges
            edges = { @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_5_LOCATION),
                    @Loc(value = POINT_6_LOCATION) }) })
    private Atlas overlappingSubAtlas1;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_7_ID, coordinates = @Loc(value = POINT_7_LOCATION)),
                    @Node(id = POINT_8_ID, coordinates = @Loc(value = POINT_8_LOCATION)) },
            // edges
            edges = { @Edge(id = "223456789120001", coordinates = { @Loc(value = POINT_7_LOCATION),
                    @Loc(value = POINT_8_LOCATION) }) })
    private Atlas overlappingSubAtlas2;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)),
                    @Node(id = POINT_6_ID, coordinates = @Loc(value = POINT_6_LOCATION)),
                    @Node(id = POINT_8_ID, coordinates = @Loc(value = POINT_8_LOCATION)),
                    @Node(id = POINT_9_ID, coordinates = @Loc(value = POINT_9_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_5_LOCATION),
                            @Loc(value = POINT_6_LOCATION), @Loc(value = POINT_8_LOCATION) }),
                    @Edge(id = "223456789120000", coordinates = { @Loc(value = POINT_6_LOCATION),
                            @Loc(value = POINT_9_LOCATION) }) })
    private Atlas overlappingAndCrossingSubAtlas1;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = POINT_5_ID, coordinates = @Loc(value = POINT_5_LOCATION)),
                    @Node(id = POINT_7_ID, coordinates = @Loc(value = POINT_7_LOCATION)),
                    @Node(id = POINT_8_ID, coordinates = @Loc(value = POINT_8_LOCATION)),
                    @Node(id = POINT_9_ID, coordinates = @Loc(value = POINT_9_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "123456789120001", coordinates = { @Loc(value = POINT_5_LOCATION),
                            @Loc(value = POINT_7_LOCATION) }),
                    @Edge(id = "123456789120002", coordinates = { @Loc(value = POINT_7_LOCATION),
                            @Loc(value = POINT_8_LOCATION) }) })
    private Atlas overlappingAndCrossingSubAtlas2;

    public Atlas overlappingAndCrossingSubAtlas1()
    {
        return this.overlappingAndCrossingSubAtlas1;
    }

    public Atlas overlappingAndCrossingSubAtlas2()
    {
        return this.overlappingAndCrossingSubAtlas2;
    }

    public Atlas overlappingSubAtlas1()
    {
        return this.overlappingSubAtlas1;
    }

    public Atlas overlappingSubAtlas2()
    {
        return this.overlappingSubAtlas2;
    }

    public Atlas subAtlasOnAntimeridianEast()
    {
        return this.subAtlasOnAntimeridianEast;
    }

    public Atlas subAtlasOnAntimeridianWest()
    {
        return this.subAtlasOnAntimeridianWest;
    }
}
