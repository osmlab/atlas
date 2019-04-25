package org.openstreetmap.atlas.geography.atlas.packed;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.tags.AerowayTag;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LanesTag;
import org.openstreetmap.atlas.tags.LeisureTag;
import org.openstreetmap.atlas.tags.MaxSpeedTag;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.PowerTag;
import org.openstreetmap.atlas.tags.RailwayTag;
import org.openstreetmap.atlas.tags.SportTag;
import org.openstreetmap.atlas.tags.SurfaceTag;
import org.openstreetmap.atlas.tags.TunnelTag;
import org.openstreetmap.atlas.tags.WaterTag;
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Speed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the {@link PackedAtlas}
 *
 * @author matthieun
 */
public class PackedAtlasTest
{
    private static final Logger logger = LoggerFactory.getLogger(PackedAtlasTest.class);

    private PackedAtlas atlas;

    @Rule
    public PackedAtlasTestRule setup = new PackedAtlasTestRule();

    @Before
    public void setup()
    {
        this.atlas = this.setup.getAtlas().cloneToPackedAtlas();
    }

    @Test
    public void testArea()
    {
        final Area area1 = this.atlas.area(45);
        final Area area2 = this.atlas.area(54);
        logger.trace(area1.toString());
        logger.trace(area2.toString());

        Assert.assertTrue(area1.asPolygon().intersects(area2.asPolygon()));
        Assert.assertEquals(Location.TEST_6,
                area1.asPolygon().intersections(area2.asPolygon()).iterator().next());

        Assert.assertEquals("golf_course", area1.getTags().get("leisure"));
        Assert.assertEquals("grassland", area1.getTags().get("natural"));
        Assert.assertFalse(AerowayTag.get(area1).isPresent());
        Assert.assertEquals(LeisureTag.GOLF_COURSE,
                LeisureTag.get(area1).orElseThrow(CoreException.supplier("No tag")));
        Assert.assertEquals(NaturalTag.GRASSLAND,
                NaturalTag.get(area1).orElseThrow(CoreException.supplier("No tag")));
        Assert.assertFalse(WaterTag.get(area1).isPresent());

        Assert.assertFalse(AerowayTag.get(area2).isPresent());
        Assert.assertEquals(LeisureTag.SWIMMING_POOL,
                LeisureTag.get(area2).orElseThrow(CoreException.supplier("No tag")));
        Assert.assertEquals(SportTag.SWIMMING,
                SportTag.get(area2).orElseThrow(CoreException.supplier("No tag")));
        Assert.assertFalse(NaturalTag.get(area2).isPresent());
        Assert.assertFalse(WaterTag.get(area2).isPresent());
    }

    @Test
    public void testConnectivity()
    {
        final Node node123 = this.atlas.node(123);
        final Node node1234 = this.atlas.node(1234);
        final Node node12345 = this.atlas.node(12345);
        final Edge edge9 = this.atlas.edge(9);
        final Edge edgeMinus9 = this.atlas.edge(-9);
        final Edge edge98 = this.atlas.edge(98);
        final Edge edge987 = this.atlas.edge(987);

        Assert.assertTrue(edge9.end().outEdges().contains(edge98));
        Assert.assertTrue(edge9.end().outEdges().contains(edgeMinus9));
        Assert.assertTrue(edge98.end().outEdges().contains(edge987));
        Assert.assertTrue(edge987.end().outEdges().contains(edge9));

        Assert.assertTrue(edge9.start().inEdges().contains(edge987));
        Assert.assertTrue(edge9.start().inEdges().contains(edgeMinus9));
        Assert.assertTrue(edge98.start().inEdges().contains(edge9));
        Assert.assertTrue(edge987.start().inEdges().contains(edge98));

        Assert.assertEquals(node123, edge9.start());
        Assert.assertEquals(node123, edgeMinus9.end());
        Assert.assertEquals(node123, edge987.end());

        Assert.assertEquals(node1234, edge9.end());
        Assert.assertEquals(node1234, edgeMinus9.start());
        Assert.assertEquals(node1234, edge98.start());

        Assert.assertEquals(node12345, edge98.end());
        Assert.assertEquals(node12345, edge987.start());
    }

    @Test
    public void testEdge()
    {
        final Node node123 = this.atlas.node(123);
        final Node node1234 = this.atlas.node(1234);
        final Node node12345 = this.atlas.node(12345);
        final Edge edge9 = this.atlas.edge(9);
        final Edge edgeMinus9 = this.atlas.edge(-9);
        final Edge edge98 = this.atlas.edge(98);
        final Edge edge987 = this.atlas.edge(987);
        logger.trace(edge9.toString());
        logger.trace(edgeMinus9.toString());
        logger.trace(edge98.toString());
        logger.trace(edge987.toString());

        Assert.assertEquals(2, node123.inEdges().size());
        Assert.assertEquals(1, node123.outEdges().size());

        Assert.assertEquals(1, node1234.inEdges().size());
        Assert.assertEquals(2, node1234.outEdges().size());

        Assert.assertEquals(1, node12345.inEdges().size());
        Assert.assertEquals(1, node12345.outEdges().size());

        Assert.assertEquals("edge98", edge98.getTags().get("name"));

        Assert.assertEquals(HighwayTag.PRIMARY, edge9.highwayTag());
        Assert.assertEquals(SurfaceTag.CONCRETE,
                SurfaceTag.get(edge9).orElseThrow(CoreException.supplier("No tag")));
        Assert.assertFalse(BridgeTag.isBridge(edge9));
        Assert.assertFalse(TunnelTag.isTunnel(edge9));
        Assert.assertEquals(3, (int) LanesTag.numberOfLanes(edge9)
                .orElseThrow(CoreException.supplier("Should have 3 lanes!")));

        Assert.assertEquals(HighwayTag.PRIMARY, edgeMinus9.highwayTag());
        Assert.assertEquals(SurfaceTag.FINE_GRAVEL,
                SurfaceTag.get(edgeMinus9).orElseThrow(CoreException.supplier("No tag")));
        Assert.assertFalse(BridgeTag.isBridge(edgeMinus9));
        Assert.assertFalse(TunnelTag.isTunnel(edgeMinus9));
        Assert.assertFalse(MaxSpeedTag.get(edgeMinus9).isPresent());
        Assert.assertFalse(LanesTag.numberOfLanes(edgeMinus9).isPresent());

        Assert.assertEquals(HighwayTag.SECONDARY, edge98.highwayTag());
        Assert.assertFalse(SurfaceTag.get(edge98).isPresent());
        Assert.assertTrue(BridgeTag.isBridge(edge98));
        Assert.assertFalse(TunnelTag.isTunnel(edge98));
        Assert.assertEquals(Speed.kilometersPerHour(100), MaxSpeedTag.get(edge98)
                .orElseThrow(CoreException.supplier("Should have maxspeed")));

        Assert.assertEquals(HighwayTag.RESIDENTIAL, edge987.highwayTag());
        Assert.assertFalse(SurfaceTag.get(edge987).isPresent());
        Assert.assertFalse(BridgeTag.isBridge(edge987));
        Assert.assertTrue(TunnelTag.isTunnel(edge987));
        Assert.assertEquals(Speed.knots(50).asKilometersPerHour(), MaxSpeedTag.get(edge987)
                .orElseThrow(CoreException.supplier("Should have maxspeed")).asKilometersPerHour(),
                1);
    }

    @Test
    public void testGeoJson()
    {
        System.out.println(this.atlas.asGeoJson().toString());
    }

    @Test
    public void testIndex()
    {
        final Rectangle testBox = Location.TEST_6.boxAround(Distance.ONE_METER);
        Assert.assertEquals(1, Iterables.size(this.atlas.nodesWithin(testBox)));
        Assert.assertEquals(Location.TEST_6,
                this.atlas.nodesWithin(testBox).iterator().next().getLocation());

        Assert.assertEquals(3, Iterables.size(this.atlas.edgesIntersecting(testBox)));
        Assert.assertTrue(
                Iterables.contains(this.atlas.edgesIntersecting(testBox), this.atlas.edge(9)));
        Assert.assertTrue(
                Iterables.contains(this.atlas.edgesIntersecting(testBox), this.atlas.edge(-9)));
        Assert.assertTrue(
                Iterables.contains(this.atlas.edgesIntersecting(testBox), this.atlas.edge(987)));
    }

    @Test
    public void testLine()
    {
        final Line line1 = this.atlas.line(32);
        final Line line2 = this.atlas.line(23);
        final Line line3 = this.atlas.line(24);
        logger.trace(line1.toString());
        logger.trace(line2.toString());
        logger.trace(line3.toString());

        Assert.assertTrue(line1.asPolyLine().length().isGreaterThan(line2.asPolyLine().length()));
        Assert.assertTrue(line2.asPolyLine().intersects(line3.asPolyLine()));
        Assert.assertFalse(line1.asPolyLine().intersects(line3.asPolyLine()));

        Assert.assertFalse(AerowayTag.get(line1).isPresent());
        Assert.assertFalse(NaturalTag.get(line1).isPresent());
        Assert.assertEquals(PowerTag.LINE,
                PowerTag.get(line1).orElseThrow(CoreException.supplier("No tag")));
        Assert.assertFalse(RailwayTag.get(line1).isPresent());
        Assert.assertFalse(WaterwayTag.get(line1).isPresent());

        Assert.assertEquals("runway", line2.getTags().get("aeroway"));
        Assert.assertNull(line2.getTags().get("waterway"));
        Assert.assertEquals(AerowayTag.RUNWAY,
                AerowayTag.get(line2).orElseThrow(CoreException.supplier("No tag")));
        Assert.assertFalse(NaturalTag.get(line2).isPresent());
        Assert.assertFalse(PowerTag.get(line2).isPresent());
        Assert.assertFalse(RailwayTag.get(line2).isPresent());
        Assert.assertFalse(WaterwayTag.get(line2).isPresent());

        Assert.assertFalse(AerowayTag.get(line3).isPresent());
        Assert.assertEquals(NaturalTag.COASTLINE,
                NaturalTag.get(line3).orElseThrow(CoreException.supplier("No tag")));
        Assert.assertFalse(PowerTag.get(line3).isPresent());
        Assert.assertFalse(RailwayTag.get(line3).isPresent());
        Assert.assertEquals(WaterwayTag.CANAL,
                WaterwayTag.get(line3).orElseThrow(CoreException.supplier("No tag")));
    }

    @Test
    public void testLocationContaining()
    {
        Assert.assertTrue(Iterables.size(this.atlas.edgesContaining(Location.TEST_6)) == 3);
        Assert.assertTrue(Iterables.size(this.atlas.linesContaining(Location.TEST_6)) == 1);

        // Total number of LineItem = sum of the lines and edges
        Assert.assertTrue(
                Iterables.size(this.atlas.lineItemsContaining(Location.TEST_6)) == Iterables
                        .size(this.atlas.edgesContaining(Location.TEST_6))
                        + Iterables.size(this.atlas.linesContaining(Location.TEST_6)));
        Assert.assertTrue(Iterables.size(this.atlas.nodesAt(Location.TEST_6)) == 1);
        Assert.assertTrue(Iterables.size(this.atlas.pointsAt(Location.TEST_6)) == 1);

        // check areasCovering, which uses fullyGeometricallyEncloses to check the covering property
        Assert.assertTrue(Iterables.size(this.atlas.areasCovering(Location.TEST_8)) == 2);

        // Total number of AtlasItems = sum of the edges, areas, lines, nodes and points
        Assert.assertTrue(Iterables.size(this.atlas.itemsContaining(Location.TEST_6)) == Iterables
                .size(this.atlas.edgesContaining(Location.TEST_6))
                + Iterables.size(this.atlas.linesContaining(Location.TEST_6))
                + Iterables.size(this.atlas.areasCovering(Location.TEST_6))
                + Iterables.size(this.atlas.nodesAt(Location.TEST_6))
                + Iterables.size(this.atlas.pointsAt(Location.TEST_6)));
    }

    @Test
    public void testNode()
    {
        final Node node1 = this.atlas.node(123);
        final Node node2 = this.atlas.node(1234);
        final Node node3 = this.atlas.node(12345);
        logger.trace(node1.toString());
        logger.trace(node2.toString());
        logger.trace(node3.toString());
        Assert.assertEquals(node1.getLocation(), Location.TEST_6);
        Assert.assertEquals(node2.getLocation(), Location.TEST_5);
        Assert.assertEquals(node3.getLocation(), Location.TEST_2);

        Assert.assertEquals("turning_circle", node1.getTags().get("highway"));
        Assert.assertEquals("turning_circle", node2.getTags().get("highway"));
        Assert.assertEquals("turning_circle", node3.getTags().get("highway"));
    }

    @Test
    public void testPoint()
    {
        final Point point1 = this.atlas.point(1);
        final Point point2 = this.atlas.point(2);
        final Point point3 = this.atlas.point(3);
        final Point point4 = this.atlas.point(4);
        final Point point5 = this.atlas.point(5);
        final Point point6 = this.atlas.point(6);
        final Point point7 = this.atlas.point(7);
        logger.trace(point1.toString());
        logger.trace(point2.toString());
        logger.trace(point3.toString());
        logger.trace(point4.toString());
        logger.trace(point5.toString());
        logger.trace(point6.toString());
        logger.trace(point7.toString());

        Assert.assertTrue(Location.TEST_3.boxAround(Distance.ONE_METER)
                .fullyGeometricallyEncloses(point1.getLocation()));
        Assert.assertTrue(Location.TEST_3.boxAround(Distance.kilometers(2))
                .fullyGeometricallyEncloses(point3.getLocation()));
        Assert.assertEquals(1, Iterables
                .size(this.atlas.pointsWithin(Location.TEST_3.boxAround(Distance.ONE_METER))));
        Assert.assertEquals(3, Iterables
                .size(this.atlas.pointsWithin(Location.TEST_3.boxAround(Distance.kilometers(2)))));

        final Rectangle box = Location.TEST_3.boxAround(Distance.ONE_METER);
        Assert.assertEquals(1, Iterables.size(
                this.atlas.points(point -> box.fullyGeometricallyEncloses(point.getLocation()))));
    }

    @Test
    public void testRelation()
    {
        final Relation relation1 = this.atlas.relation(1);
        final Relation relation2 = this.atlas.relation(2);

        // The null members are now forbidden!
        Assert.assertEquals(/* 4 */3, relation1.members().size());
        Assert.assertEquals(5, relation2.members().size());

        final RelationMemberList members1 = relation1.members();
        final RelationMemberList members2 = relation2.members();

        Assert.assertEquals(-9, members1.get(1).getEntity().getIdentifier());
        Assert.assertEquals(5, members2.get(3).getEntity().getIdentifier());
        Assert.assertTrue(members2.get(3).getEntity() instanceof Point);
        Assert.assertEquals("area", members2.get(1).getRole());

        Assert.assertEquals(2, this.atlas.node(1234).relations().size());
        boolean foundOne = false;
        boolean foundTwo = false;
        for (final Relation relation : this.atlas.node(1234).relations())
        {
            if (relation.getIdentifier() == 1)
            {
                foundOne = true;
            }
            if (relation.getIdentifier() == 2)
            {
                foundTwo = true;
            }
        }
        if (!foundOne || !foundTwo)
        {
            Assert.fail("Did not find the proper relations");
        }

        Assert.assertEquals(5, relation2.allKnownOsmMembers().size());
    }

    @Test
    public void testRelationMembersWithSameIdentifiersButDifferentTypes()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addArea(0, Polygon.SILICON_VALLEY, Maps.hashMap());
        builder.addLine(0, PolyLine.TEST_POLYLINE, Maps.hashMap());
        final RelationBean bean = new RelationBean();
        bean.addItem(0L, "Role of the Area", ItemType.AREA);
        bean.addItem(0L, "Role of the Line", ItemType.LINE);
        builder.addRelation(0, 0, bean, Maps.hashMap());
        final Atlas result = builder.get();
        Assert.assertEquals(2, result.relation(0).members().size());
    }

    @Test
    public void testValence()
    {
        // Case where valences are equals
        Assert.assertEquals(this.atlas.node(12345L).valence(),
                this.atlas.node(12345L).absoluteValence());

        // Case where absolute valence becomes a factor
        Assert.assertTrue(
                this.atlas.node(1234L).valence() != this.atlas.node(1234L).absoluteValence());
        Assert.assertEquals(2, this.atlas.node(1234L).valence());
        Assert.assertEquals(3, this.atlas.node(1234L).absoluteValence());
    }
}
