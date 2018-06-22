package org.openstreetmap.atlas.geography.atlas.pbf.slicing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.store.PbfMemoryStore;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticNearestNeighborCountryCodeTag;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

/**
 * @author Yiqing Jin
 */
public class CountrySlicingProcessorTest
{
    private static CountryBoundaryMap boundaryMap;
    private PbfMemoryStore store;

    @BeforeClass
    public static void setup()
    {
        boundaryMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountrySlicingProcessorTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
    }

    @Test
    public void testAreaInside()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        final Way way = addWay(
                "6.940132,-8.267757 6.92829,-8.25744 6.93949,-8.24101 6.954847,-8.255834", true);
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);
        processor.run();
        Assert.assertEquals(1, this.store.wayCount());
        Assert.assertTrue(way.getTags().stream().anyMatch(tag -> "CIV".equals(tag.getValue())));
    }

    @Test
    public void testAreaOverlap()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        final Way way = addWay(
                "6.91758,-8.32742 6.91758,-8.30635 6.92911,-8.30680 6.92843,-8.32731", true);
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);
        processor.run();
        // debug code to investigate the random failing issue.
        if (this.store.wayCount() != 2)
        {
            try
            {
                this.store.writeXml(new File("debug.xml"));
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
        Assert.assertEquals(2, this.store.wayCount());
        Assert.assertTrue(this.store.getWays().values().stream().allMatch(Way::isClosed));
        Assert.assertNull(this.store.getWay(way.getId()));
    }

    @Test
    public void testCountryAssignment()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        final Way way = addWay("7.2,-8.4 7.18,-8.36 7.2,-8.2");
        way.getTags().add(new Tag("highway", "primary"));
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);
        processor.run();
        Assert.assertEquals("CIV,LBR", this.store.getNode(1001000L).getTags().stream()
                .filter(tag -> tag.getKey().equals(ISOCountryTag.KEY)).findAny().get().getValue());
    }

    @Test
    public void testFilterBasedOnBound()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());

        // Way starts in Liberia and ends in Sierra Leone (outside of given boundary)
        addWay("7.5,-10.8 7.7,-11.8");

        CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store, boundaryMap,
                null);
        processor.run();
        Assert.assertEquals("should be 1 piece after filtering", 1, this.store.wayCount());
        final HashSet<String> countryCodeSet = new HashSet<>();
        countryCodeSet.add("CIV");
        processor = new CountrySlicingProcessor(this.store, boundaryMap, countryCodeSet);
        processor.run();
        Assert.assertEquals("should be 1 piece after filtering", 1, this.store.wayCount());
    }

    @Test
    public void testForceSlicing()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        final CountryBoundaryMap map = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> CountrySlicingProcessorTest.class
                        .getResourceAsStream("testForceSlicingBoundaryMap.txt")));
        Assert.assertFalse(map.hasGridIndex());
        final Set<String> countries = new HashSet<>();
        countries.add("AAA");
        countries.add("BBB");
        map.initializeGridIndex(countries);
        // AAA is made of 3 polygons, and BBB of one. Below is a way that is really close to all
        // AAA polygons, but intersects BBB at the end. Even though it is next to 4 polygons, it
        // should still be sliced, as this is less than 3 countries.
        addWayWkt("LINESTRING(-73.48974622786 17.494627833366, -72.21533216536 17.428709864616, "
                + "-71.07275404036 16.813475489616, -70.87500013411 16.286131739616, "
                + "-70.34765638411 16.022459864616, -69.93017591536 16.088377833366, "
                + "-69.24902357161 16.527830958366)");
        map.setShouldAlwaysSlicePredicate(taggable -> true);
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store, map);
        processor.run();

        final Way outsideBbb = this.store.getWay(1002000L);
        final Way insideBbb = this.store.getWay(1001000L);

        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.toString(), outsideBbb
                .getTags().stream()
                .filter(tag -> tag.getKey().equals(SyntheticNearestNeighborCountryCodeTag.KEY))
                .findAny().get().getValue());
        Assert.assertEquals("BBB", outsideBbb.getTags().stream()
                .filter(tag -> tag.getKey().equals(ISOCountryTag.KEY)).findAny().get().getValue());
        Assert.assertEquals("BBB", insideBbb.getTags().stream()
                .filter(tag -> tag.getKey().equals(ISOCountryTag.KEY)).findAny().get().getValue());
    }

    @Test
    public void testNearestNeighborTag()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());

        // The way is way outside of the CIV,LBR,GIN boundaries provided
        final Way way = addWay("14.2,-8.4 14.18,-8.36 14.2,-8.2");
        way.getTags().add(new Tag("highway", "primary"));
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);
        processor.run();

        // Verify we're using nearest neighbor country code assignment and the tag exists
        Assert.assertEquals(SyntheticNearestNeighborCountryCodeTag.YES.toString(), this.store
                .getWay(1000000L).getTags().stream()
                .filter(tag -> tag.getKey().equals(SyntheticNearestNeighborCountryCodeTag.KEY))
                .findAny().get().getValue());
    }

    @Test
    public void testRelationComplex()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        readXml("relation_multipolygon_complex.xml");
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);
        processor.run();
        Assert.assertEquals(2, this.store.relationCount());
    }

    @Test
    public void testRelationComplexHole()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        readXml("relation_multipolygon_complex_hole.xml");
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);
        processor.run();
        Assert.assertEquals(2, this.store.relationCount());
    }

    @Test
    public void testRelationSimple()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        readXml("relation_multipolygon.xml");
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);
        processor.run();
        Assert.assertEquals(2, this.store.relationCount());
        Assert.assertEquals(5, this.store.wayCount());
    }

    @Test
    public void testRelationSimpleHole()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        readXml("relation_multipolygon_hole.xml");
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);
        processor.run();
        Assert.assertEquals(2, this.store.relationCount());
        final Relation relation1 = this.store.getRelation(108772001000L);
        Assert.assertNotNull(relation1);
        Assert.assertEquals("CIV",
                relation1.getTags().stream().filter(tag -> ISOCountryTag.KEY.equals(tag.getKey()))
                        .findFirst().get().getValue());
        Assert.assertTrue(relation1.getMembers().stream()
                .map(member -> this.store.getWay(member.getMemberId())).allMatch(Way::isClosed));
    }

    @Test
    public void testWayInside()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        readXml("way_inside.osm");
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);
        processor.run();
        Assert.assertEquals(1, this.store.wayCount());
        final Way way = this.store.getWay(1000000);
        Assert.assertTrue(way.getTags().stream().anyMatch(tag -> "CIV".equals(tag.getValue())));
    }

    @Test
    public void testWayMultipleCross()
    {
        // a case that a way cross border two times.
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        final Way way = addWay(
                "6.98365,-8.30103 6.98237,-8.29694 6.98019,-8.29400 6.98573,-8.29511 6.983833,-8.28705");
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);

        processor.run();
        Assert.assertEquals(4, this.store.wayCount());
        Assert.assertNull("Original way should be removed", this.store.getWay(way.getId()));
        Assert.assertTrue(this.store.getWays().values().stream()
                .allMatch(way1 -> way1.getTags().size() == 3));
        Assert.assertEquals(8, this.store.nodeCount());
    }

    @Test
    public void testWaySimpleCross()
    {
        this.store = new PbfMemoryStore(AtlasLoadingOption.createOptionWithNoSlicing());
        final Way way = addWay("7.2,-8.4 7.18,-8.36 7.2,-8.2");
        final CountrySlicingProcessor processor = new CountrySlicingProcessor(this.store,
                boundaryMap);
        processor.run();
        Assert.assertEquals("the way should be cut into 2 pieces", 2, this.store.wayCount());
        Assert.assertEquals(4, this.store.getNodes().size());
        Assert.assertTrue(this.store.getWays().values().stream()
                .allMatch(way1 -> way1.getTags().size() == 3));
        Assert.assertNull("Original way should be removed", this.store.getWay(way.getId()));
        Assert.assertNotNull("New way should be added", this.store.getWay(way.getId() + 1 * 1000));
        Assert.assertNotNull("New way should be added", this.store.getWay(way.getId() + 2 * 1000));
    }

    private Way addWay(final PolyLine polyLine)
    {
        final int padding = 1000000;
        // e.g. 7.186552,-8.307586
        final long newWayIdBase = this.store.wayCount() + 1;
        long newNodeIdBase = this.store.nodeCount() + 1;
        final Way way = new Way(createEntityData(newWayIdBase * padding));
        this.store.addWay(way);
        for (final Location location : polyLine)
        {
            final double latitude = location.getLatitude().asDegrees();
            final double longitude = location.getLongitude().asDegrees();
            final Node node = new Node(createEntityData(newNodeIdBase * padding), latitude,
                    longitude);
            newNodeIdBase++;
            this.store.addNode(node);
            way.getWayNodes().add(new WayNode(node.getId()));
        }
        if (polyLine instanceof Polygon)
        {
            way.getWayNodes().add(new WayNode(way.getWayNodes().get(0).getNodeId()));
        }
        return way;
    }

    private Way addWay(final String value)
    {
        return addWay(value, false);
    }

    private Way addWay(final String value, final boolean isArea)
    {
        final String[] split = value.split(" |,");
        final List<Location> locations = new ArrayList<>();
        for (int i = 0; i < split.length; i += 2)
        {
            final double latitude = Double.parseDouble(split[i]);
            final double longitude = Double.parseDouble(split[i + 1]);
            locations.add(new Location(Latitude.degrees(latitude), Longitude.degrees(longitude)));
        }
        final PolyLine result;
        if (isArea)
        {
            result = new Polygon(locations);
        }
        else
        {
            result = new PolyLine(locations);
        }
        return addWay(result);
    }

    private Way addWayWkt(final String wkt)
    {
        return addWay(PolyLine.wkt(wkt));
    }

    private CommonEntityData createEntityData(final long identifier)
    {
        final CommonEntityData data = new CommonEntityData(identifier, 1, new Date(),
                new OsmUser(1000, "test_user"), 0);
        data.getTags().add(new Tag("test_key1", "test_value1"));
        data.getTags().add(new Tag("test_key2", "test_value2"));
        return data;
    }

    private void readXml(final String path)
    {
        final File file = new File(CountrySlicingProcessorTest.class.getResource(path).getPath());
        final XmlReader reader = new XmlReader(file, true, CompressionMethod.None);
        reader.setSink(this.store);
        reader.run();
    }
}
