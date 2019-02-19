package org.openstreetmap.atlas.geography.atlas.statistics;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasTest;
import org.openstreetmap.atlas.geography.atlas.statistics.AtlasStatistics.StatisticKey;
import org.openstreetmap.atlas.geography.atlas.statistics.AtlasStatistics.StatisticValue;

/**
 * Test the {@link AtlasStatistics} range of classes.
 *
 * @author matthieun
 * @author patrick-mi
 */
public class AtlasStatisticsTest
{
    @Rule
    public final AtlasStatisticsTestRule rule = new AtlasStatisticsTestRule();

    @Test
    public void testCounting()
    {
        final Atlas atlas = new PackedAtlasTest().getAtlas();
        final Counter counter = new Counter();
        final AtlasStatistics statistics = counter.processAtlas(atlas);
        Assert.assertEquals(7.245,
                statistics.get(new StatisticKey("PRIMARY", "length_named", "true")).getCount(),
                0.01);
    }

    @Test
    public void testCountingAddresses()
    {
        final Atlas atlas = this.rule.getAddressAtlas();
        final AtlasStatistics statistics = new Counter().processAtlas(atlas);

        // Associated Street
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "associated_street", "true")).getCount(), 0.01);

        // Address Ranges
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "address_ranges", "true")).getCount(), 0.01);
        Assert.assertEquals(4.21,
                statistics.get(new StatisticKey("", "address_ranges_distance", "true")).getCount(),
                0.01);

        // Address Housenumber
        Assert.assertEquals(6.0,
                statistics.get(new StatisticKey("", "address_housenumber", "true")).getCount(),
                0.01);

        // Address Housename
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "address_housename", "true")).getCount(), 0.01);

        // Address Street
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "address_street", "true")).getCount(), 0.01);

        // Address Housenumber and Street
        Assert.assertEquals(1.0, statistics
                .get(new StatisticKey("", "address_housenumber_and_street", "true")).getCount(),
                0.01);

        // Address Housename and Street
        Assert.assertEquals(1.0, statistics
                .get(new StatisticKey("", "address_housename_and_street", "true")).getCount(),
                0.01);

        // Address Blocknumber
        Assert.assertEquals(2.0,
                statistics.get(new StatisticKey("", "address_blocknumber", "true")).getCount(),
                0.01);
    }

    @Test
    public void testCountingFerries()
    {
        final Atlas atlas = this.rule.getFerryAtlas();
        final AtlasStatistics statistics = new Counter().processAtlas(atlas);

        // Ferries
        Assert.assertEquals(2.0,
                statistics.get(new StatisticKey("", "ferry_route", "true")).getCount(), 0.01);
        Assert.assertEquals(14.05,
                statistics.get(new StatisticKey("", "ferry_route_distance", "true")).getCount(),
                0.01);
    }

    @Test
    public void testCountingRefs()
    {
        final Atlas atlas = this.rule.getRefsAtlas();
        final AtlasStatistics statistics = new Counter().processAtlas(atlas);

        // Refs
        Assert.assertEquals(1.0, statistics.get(new StatisticKey("", "ref", "true")).getCount(),
                0.01);
        Assert.assertEquals(0.53,
                statistics.get(new StatisticKey("", "ref_distance", "true")).getCount(), 0.01);

        Assert.assertEquals(3.0,
                statistics.get(new StatisticKey("", "ref_no_relation", "true")).getCount(), 0.01);
        Assert.assertEquals(0.16,
                statistics.get(new StatisticKey("", "ref_no_relation_distance", "true")).getCount(),
                0.01);

        Assert.assertEquals(1.0, statistics.get(new StatisticKey("", "int_ref", "true")).getCount(),
                0.01);
        Assert.assertEquals(0.53,
                statistics.get(new StatisticKey("", "int_ref_distance", "true")).getCount(), 0.01);

        Assert.assertEquals(2.0,
                statistics.get(new StatisticKey("", "int_ref_no_relation", "true")).getCount(),
                0.01);
        Assert.assertEquals(0.21, statistics
                .get(new StatisticKey("", "int_ref_no_relation_distance", "true")).getCount(),
                0.01);

        // Lane direction
        Assert.assertEquals(3.0,
                statistics.get(new StatisticKey("", "lane_direction", "true")).getCount(), 0.01);
        Assert.assertEquals(0.31,
                statistics.get(new StatisticKey("", "lane_direction_distance", "true")).getCount(),
                0.01);

        // Toll booths
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "toll_booths", "true")).getCount(), 0.01);
    }

    @Test
    public void testCountingWater()
    {
        final Atlas atlas = this.rule.getWaterAtlas();
        final AtlasStatistics statistics = new Counter().processAtlas(atlas);

        // Rivers
        Assert.assertEquals(6.0, statistics.get(new StatisticKey("", "rivers", "true")).getCount(),
                0.01);
        Assert.assertEquals(24.92,
                statistics.get(new StatisticKey("", "rivers_distance", "true")).getCount(), 0.01);

        // Wetland
        Assert.assertEquals(1.0, statistics.get(new StatisticKey("", "wetland", "true")).getCount(),
                0.01);
        Assert.assertEquals(0.84,
                statistics.get(new StatisticKey("", "wetland_surface", "true")).getCount(), 0.01);

        // Lakes2
        Assert.assertEquals(3.0, statistics.get(new StatisticKey("", "lakes2", "true")).getCount(),
                0.01);
        Assert.assertEquals(2.08,
                statistics.get(new StatisticKey("", "lakes2_surface", "true")).getCount(), 0.01);

        // Reservoir
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "reservoir", "true")).getCount(), 0.01);
        Assert.assertEquals(0.77,
                statistics.get(new StatisticKey("", "reservoir_surface", "true")).getCount(), 0.01);

        // Lagoon
        Assert.assertEquals(1.0, statistics.get(new StatisticKey("", "lagoon", "true")).getCount(),
                0.01);
        Assert.assertEquals(0.35,
                statistics.get(new StatisticKey("", "lagoon_surface", "true")).getCount(), 0.01);

        // Pool
        Assert.assertEquals(1.0, statistics.get(new StatisticKey("", "pool", "true")).getCount(),
                0.01);
        Assert.assertEquals(0.084,
                statistics.get(new StatisticKey("", "pool_surface", "true")).getCount(), 0.01);

        // Coastlines
        Assert.assertEquals(4.803585,
                statistics.get(new StatisticKey("", "coastline_distance", "true")).getCount(),
                0.01);

        // Harbour
        Assert.assertEquals(3.0, statistics.get(new StatisticKey("", "harbour", "true")).getCount(),
                0.01);
        Assert.assertEquals(0.99,
                statistics.get(new StatisticKey("", "harbour_distance", "true")).getCount(), 0.01);
        Assert.assertEquals(2.39,
                statistics.get(new StatisticKey("", "harbour_surface", "true")).getCount(), 0.01);

        // Bay
        Assert.assertEquals(2.0, statistics.get(new StatisticKey("", "bay", "true")).getCount(),
                0.01);
        Assert.assertEquals(2.39,
                statistics.get(new StatisticKey("", "harbour_surface", "true")).getCount(), 0.01);

        // Beach
        Assert.assertEquals(2.0, statistics.get(new StatisticKey("", "beach", "true")).getCount(),
                0.01);
        Assert.assertEquals(2.44,
                statistics.get(new StatisticKey("", "beach_surface", "true")).getCount(), 0.01);

        // Island
        Assert.assertEquals(2.0, statistics.get(new StatisticKey("", "island", "true")).getCount(),
                0.01);
        Assert.assertEquals(0.25,
                statistics.get(new StatisticKey("", "island_surface", "true")).getCount(), 0.01);

        // Piers
        Assert.assertEquals(3.0, statistics.get(new StatisticKey("", "pier", "true")).getCount(),
                0.01);
        Assert.assertEquals(2.67,
                statistics.get(new StatisticKey("", "pier_distance", "true")).getCount(), 0.01);
        Assert.assertEquals(0.11,
                statistics.get(new StatisticKey("", "pier_surface", "true")).getCount(), 0.01);
    }

    @Test
    public void testCountingAois()
    {
        final Atlas atlas = this.rule.getAoiAtlas();
        final AtlasStatistics statistics = new Counter().processAtlas(atlas);

        // natural_areas
        Assert.assertEquals(3.0,
                statistics.get(new StatisticKey("", "natural_areas", "true")).getCount(), 0.01);

        // airfield_areas
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "airfield_areas", "true")).getCount(), 0.01);
        Assert.assertEquals(0.4084847279972836,
                statistics.get(new StatisticKey("", "airfield_areas_surface", "true")).getCount(), 0.01);

        // airfield_relations
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "airfield_relations", "true")).getCount(), 0.01);
        Assert.assertEquals(1.5860808721221027,
                statistics.get(new StatisticKey("", "airfield_relations_surface", "true")).getCount(), 0.01);

        // airport_areas
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "airport_areas", "true")).getCount(), 0.01);
        Assert.assertEquals(0.40845810837601526,
                statistics.get(new StatisticKey("", "airport_areas_surface", "true")).getCount(), 0.01);

        // runway_lines
        Assert.assertEquals(4.2820480000000005,
                statistics.get(new StatisticKey("", "runway_lines_distance", "true")).getCount(), 0.01);
        Assert.assertEquals(2.0,
                statistics.get(new StatisticKey("", "runway_lines", "true")).getCount(), 0.01);

        // runway_relations
        Assert.assertEquals(2.245368,
                statistics.get(new StatisticKey("", "runway_relations_distance", "true")).getCount(), 0.01);
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "runway_relations", "true")).getCount(), 0.01);
        Assert.assertEquals(0.10788901440886413,
                statistics.get(new StatisticKey("", "runway_relations_surface", "true")).getCount(), 0.01);

        // national park areas
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "national_park_areas", "true")).getCount(), 0.01);
        Assert.assertEquals(0.5355297484173871,
                statistics.get(new StatisticKey("", "national_park_areas_surface", "true")).getCount(), 0.01);

        // national park relations
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "national_park_relations", "true")).getCount(), 0.01);
        Assert.assertEquals(1.524581804213072,
                statistics.get(new StatisticKey("", "national_park_relations_surface", "true")).getCount(), 0.01);

        // glacier_areas
        Assert.assertEquals(1.0,
                statistics.get(new StatisticKey("", "glacier_areas", "true")).getCount(), 0.01);
        Assert.assertEquals(0.6508549851979776,
                statistics.get(new StatisticKey("", "glacier_areas_surface", "true")).getCount(), 0.01);
    }

    @Test
    public void testCSVCompatability()
    {
        final StatisticKey key = new StatisticKey("", "last_edit_user_name", "kepta\"sds'sds");
        // if there is a " or , or \n character within a field in a CSV, the CSV field is wrapped in
        // double quotes and the interior double quote is escaped
        final String correctlyFormattedCSVKey = ",last_edit_user_name,\"kepta\"\"sds'sds\"";
        Assert.assertEquals(key.toString(), correctlyFormattedCSVKey);
    }

    @Test
    public void testFormatting()
    {
        final Atlas atlas = this.rule.getWaterAtlas();
        final AtlasStatistics statistics = new Counter().processAtlas(atlas);

        Assert.assertEquals("6.00,34.00",
                statistics.get(new StatisticKey("", "rivers", "true")).toString());
        Assert.assertEquals("24.92,37.61",
                statistics.get(new StatisticKey("", "rivers_distance", "true")).toString());

    }

    @Test(expected = CoreException.class)
    public void testFormattingKeyTooHigh()
    {
        final double key = 1.0 / 0.0;
        final double value = 100.0;
        new StatisticValue(key, value);
    }

    @Test(expected = CoreException.class)
    public void testFormattingKeyTooLow()
    {
        final double key = -1.0 / 0.0;
        final double value = 100.0;
        new StatisticValue(key, value);
    }

    @Test(expected = CoreException.class)
    public void testFormattingValueTooHigh()
    {
        final double key = 100.0;
        final double value = Double.MAX_VALUE / 1.5;
        System.out.println(new StatisticValue(key, value));
    }

    @Test
    public void testMerge()
    {
        final StatisticKey key = new StatisticKey("RESIDENTIAL", "length_named", "TRUE");
        final AtlasStatistics stat1 = new AtlasStatistics();
        stat1.add(key, new StatisticValue(100.0, 500.0));
        final AtlasStatistics stat2 = new AtlasStatistics();
        stat2.add(key, new StatisticValue(200.0, 300.0));
        final AtlasStatistics merged = AtlasStatistics.merge(stat1, stat2);
        Assert.assertEquals(300.0, merged.get(key).getCount(), 0.001);
        Assert.assertEquals(800.0, merged.get(key).getTotalCount(), 0.001);
    }
}
