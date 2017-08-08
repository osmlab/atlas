package org.openstreetmap.atlas.geography.atlas.statistics;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.Coverage;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * Test a subset of the counter metrics, to make sure that the logic is sane.
 *
 * @author matthieun
 */
public class CounterTest
{
    // private static final Logger logger = LoggerFactory.getLogger(CounterTest.class);

    private static final Location LOCATION = Location.TEST_6;
    private static final Polygon POLYGON = new Polygon(Location.TEST_1, Location.TEST_2,
            Location.TEST_7);
    private static final PolyLine POLY_LINE = new Segment(LOCATION, Location.TEST_1);

    private Atlas atlas;
    private Resource countsDefinition;

    @Test
    public void countsTest()
    {
        final Counter counter = new Counter();
        counter.setCountsDefinition(this.countsDefinition);
        final AtlasStatistics atlasStatistics = counter.processAtlas(this.atlas);
        // logger.info(this.atlas.toString());
        // logger.info(atlasStatistics.toString());
        // Nodes
        Assert.assertEquals(1,
                atlasStatistics.get(Coverage.NULL_KEY, "stop_signs", "true").getCount(), 0.01);
        Assert.assertEquals(1,
                atlasStatistics.get(Coverage.NULL_KEY, "stop_lights", "true").getCount(), 0.01);

        // Edges
        Assert.assertEquals(POLY_LINE.length().asKilometers(),
                atlasStatistics.get("secondary", "length_roads_named", "true").getCount(), 0.01);
        Assert.assertEquals(POLY_LINE.length().asKilometers(),
                atlasStatistics.get(Coverage.AGGREGATE_KEY, "length_total", "true").getCount(),
                0.01);

        // Areas
        Assert.assertEquals(POLYGON.surface().asKilometerSquared(),
                atlasStatistics.get(Coverage.NULL_KEY, "lakes_area", "true").getCount(), 0.01);
        Assert.assertEquals(POLYGON.surface().asKilometerSquared(),
                atlasStatistics.get(Coverage.NULL_KEY, "rivers_area", "true").getCount(), 0.01);

        // Lines
        Assert.assertEquals(2 * POLY_LINE.length().asKilometers(),
                atlasStatistics.get(Coverage.NULL_KEY, "river_length", "true").getCount(), 0.01);
        Assert.assertEquals(POLY_LINE.length().asKilometers(),
                atlasStatistics.get(Coverage.NULL_KEY, "rail_length", "true").getCount(), 0.01);

        // Points
        Assert.assertEquals(1, atlasStatistics.get(Coverage.NULL_KEY, "shop", "true").getCount(),
                0.01);

        // Relations
        Assert.assertEquals(2 * POLY_LINE.length().asKilometers(),
                atlasStatistics.get(Coverage.NULL_KEY, "transit_bus_length", "true").getCount(),
                0.01);
    }

    @Test
    public void countsWithShardBoundariesTest()
    {
        final Counter counter = new Counter();
        counter.setCountsDefinition(this.countsDefinition);
        counter.withSharding(new SlippyTileSharding(14));
        final AtlasStatistics atlasStatistics = counter.processAtlas(this.atlas);
        // logger.info(this.atlas.toString());
        // logger.info(atlasStatistics.toString());
        // Nodes
        Assert.assertEquals(1,
                atlasStatistics.get(Coverage.NULL_KEY, "stop_signs", "true").getCount(), 0.01);
        Assert.assertEquals(1,
                atlasStatistics.get(Coverage.NULL_KEY, "stop_lights", "true").getCount(), 0.01);

        // Edges
        Assert.assertEquals(POLY_LINE.length().asKilometers() / 3,
                atlasStatistics.get("secondary", "length_roads_named", "true").getCount(), 0.01);
        Assert.assertEquals(POLY_LINE.length().asKilometers() / 3,
                atlasStatistics.get(Coverage.AGGREGATE_KEY, "length_total", "true").getCount(),
                0.01);

        // Areas
        Assert.assertEquals(POLYGON.surface().asKilometerSquared() / 3,
                atlasStatistics.get(Coverage.NULL_KEY, "lakes_area", "true").getCount(), 0.01);
        Assert.assertEquals(POLYGON.surface().asKilometerSquared() / 3,
                atlasStatistics.get(Coverage.NULL_KEY, "rivers_area", "true").getCount(), 0.01);

        // Lines
        Assert.assertEquals(2 * POLY_LINE.length().asKilometers() / 3,
                atlasStatistics.get(Coverage.NULL_KEY, "river_length", "true").getCount(), 0.01);
        Assert.assertEquals(POLY_LINE.length().asKilometers() / 3,
                atlasStatistics.get(Coverage.NULL_KEY, "rail_length", "true").getCount(), 0.01);

        // Points
        Assert.assertEquals(1, atlasStatistics.get(Coverage.NULL_KEY, "shop", "true").getCount(),
                0.01);

        // Relations
        Assert.assertEquals(2 * POLY_LINE.length().asKilometers() / 3,
                atlasStatistics.get(Coverage.NULL_KEY, "transit_bus_length", "true").getCount(),
                0.01);
    }

    @Before
    public void getAtlas()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        // Nodes
        builder.addNode(0, LOCATION, tags("highway=traffic_signals"));
        // Mimic the osm pbf reader, and add a point as well.
        builder.addPoint(1, LOCATION, tags("highway=traffic_signals"));
        builder.addNode(1, Location.TEST_1, tags("highway=stop"));
        // Mimic the osm pbf reader, and add a point as well.
        builder.addPoint(2, Location.TEST_1, tags("highway=stop"));

        // Edges
        builder.addEdge(0, POLY_LINE, tags("highway=primary", "name=coco"));
        builder.addEdge(1, POLY_LINE, tags("highway=secondary"));

        // Areas
        builder.addArea(0, POLYGON, tags("natural=water", "water=lake"));
        builder.addArea(1, POLYGON, tags("natural=water", "water=river"));

        // Lines
        builder.addLine(0, POLY_LINE, tags("waterway=river"));
        builder.addLine(1, POLY_LINE, tags("natural=water", "water=canal"));
        builder.addLine(2, POLY_LINE, tags("natural=water"));
        builder.addLine(3, POLY_LINE, tags("railway=narrow_gauge"));

        // Points
        builder.addPoint(0, LOCATION, tags("shop=some_shop"));

        // Relations

        // 0
        final RelationBean structure0 = new RelationBean();
        structure0.addItem(1L, "roadSegment", ItemType.EDGE);
        builder.addRelation(0, 0, structure0, tags("type=route", "route=road", "ref=HighwayOne"));

        // 1
        final RelationBean structure1 = new RelationBean();
        structure1.addItem(0L, "roadSegment", ItemType.EDGE);
        structure1.addItem(1L, "roadSegment", ItemType.EDGE);
        builder.addRelation(1, 1, structure1, tags("type=route", "route=bus"));

        this.atlas = builder.get();
        this.countsDefinition = Counter.POI_COUNTS_DEFINITION.getDefault();
    }

    private Map<String, String> tags(final String... tags)
    {
        final Map<String, String> result = new HashMap<>();
        for (final String tag : tags)
        {
            final StringList split = StringList.split(tag, "=");
            result.put(split.get(0), split.get(1));
        }
        return result;
    }
}
