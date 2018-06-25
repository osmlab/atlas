package org.openstreetmap.atlas.geography.atlas.pbf;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasIntegrationTest;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.clipping.Clip.ClipType;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.tags.AerowayTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LastEditTimeTag;
import org.openstreetmap.atlas.tags.LastEditUserIdentifierTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author tony
 */
public class OsmPbfLoaderIntegrationTest extends AtlasIntegrationTest
{
    @Test
    public void testAreaAndLine()
    {
        // -74.8421145,23.6792718,-74.8284978,23.6868704
        final Rectangle bound = Rectangle.forLocated(Location.forString("23.6792718,-74.8421145"),
                Location.forString("23.6868704,-74.8284978"));
        final Atlas atlas = loadBahamas(bound);

        Assert.assertEquals(2, atlas.numberOfAreas());
        Assert.assertEquals(2, atlas.numberOfLines());

        final Area smallArea = atlas.area(197913769000000L);
        Assert.assertEquals(false, BuildingTag.isBuilding(smallArea));
        Assert.assertEquals("apron", smallArea.getTags().get("aeroway"));
        Assert.assertEquals(4, Iterables.size(smallArea));
        Assert.assertNotNull(smallArea.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(smallArea.tag(LastEditUserIdentifierTag.KEY));

        final Area bigArea = atlas.area(197913691000000L);
        Assert.assertEquals(false, BuildingTag.isBuilding(bigArea));
        Assert.assertEquals("aerodrome", bigArea.getTags().get("aeroway"));
        Assert.assertEquals("Rum Cay Airport", bigArea.getTags().get("name"));
        Assert.assertEquals(11, Iterables.size(bigArea));
        Assert.assertNotNull(bigArea.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(bigArea.tag(LastEditUserIdentifierTag.KEY));

        final Line longLine = atlas.line(197913772000000L);
        Assert.assertEquals(AerowayTag.RUNWAY,
                AerowayTag.get(longLine).orElseThrow(() -> new CoreException("No aeroway Tag")));
        Assert.assertEquals(4, Iterables.size(longLine));
        Assert.assertNotNull(longLine.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(longLine.tag(LastEditUserIdentifierTag.KEY));

        final Line shortLine = atlas.line(197913770000000L);
        Assert.assertEquals(AerowayTag.TAXIWAY,
                AerowayTag.get(shortLine).orElseThrow(() -> new CoreException("No aeroway Tag")));
        Assert.assertEquals(4, Iterables.size(shortLine));
        Assert.assertNotNull(shortLine.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(shortLine.tag(LastEditUserIdentifierTag.KEY));
    }

    @Test
    public void testEdgeAndNode()
    {
        // -74.8335514,23.6744331,-74.8268456,23.6781754
        final Rectangle bound = Rectangle.forLocated(Location.forString("23.6744331,-74.8335514"),
                Location.forString("23.6781754,-74.8268456"));
        final Atlas atlas = loadBahamas(bound);
        Assert.assertEquals(2, atlas.numberOfEdges());

        final Edge edgeForward = atlas.edge(197913739000001L);
        Assert.assertEquals(197913739000001L, edgeForward.getIdentifier());
        Assert.assertEquals(HighwayTag.TERTIARY, edgeForward.highwayTag());
        Assert.assertEquals(32, Iterables.size(edgeForward.getRawGeometry()));
        Assert.assertNotNull(edgeForward.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(edgeForward.tag(LastEditUserIdentifierTag.KEY));

        final Edge edgeBackward = atlas.edge(-197913739000001L);
        Assert.assertEquals(-197913739000001L, edgeBackward.getIdentifier());
        Assert.assertEquals(HighwayTag.TERTIARY, edgeBackward.highwayTag());
        Assert.assertEquals(32, Iterables.size(edgeBackward.getRawGeometry()));
        Assert.assertNotNull(edgeBackward.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(edgeBackward.tag(LastEditUserIdentifierTag.KEY));

        Assert.assertEquals(Iterables.first(edgeForward.getRawGeometry()).get(),
                Iterables.last(edgeBackward.getRawGeometry()).get());

        final Node startNodeOfForwardEdge = atlas.node(2081113899000000L);
        Assert.assertEquals("POINT (-74.8278433 23.6811808)",
                startNodeOfForwardEdge.getLocation().toString());
        Assert.assertEquals(1, startNodeOfForwardEdge.inEdges().size());
        Assert.assertTrue(
                startNodeOfForwardEdge.inEdges().stream().map(edge -> edge.getIdentifier())
                        .collect(Collectors.toList()).contains(edgeBackward.getIdentifier()));
        Assert.assertEquals(1, startNodeOfForwardEdge.outEdges().size());
        Assert.assertTrue(
                startNodeOfForwardEdge.outEdges().stream().map(edge -> edge.getIdentifier())
                        .collect(Collectors.toList()).contains(edgeForward.getIdentifier()));
        Assert.assertEquals(startNodeOfForwardEdge, edgeForward.start());
        Assert.assertNotNull(startNodeOfForwardEdge.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(startNodeOfForwardEdge.tag(LastEditUserIdentifierTag.KEY));

        final Node endNodeOfForwardEdge = atlas.node(2081113527000000L);
        Assert.assertEquals("POINT (-74.8332022 23.6576434)",
                endNodeOfForwardEdge.getLocation().toString());
        Assert.assertEquals(1, endNodeOfForwardEdge.inEdges().size());
        Assert.assertTrue(endNodeOfForwardEdge.inEdges().stream().map(edge -> edge.getIdentifier())
                .collect(Collectors.toList()).contains(edgeForward.getIdentifier()));
        Assert.assertEquals(1, endNodeOfForwardEdge.outEdges().size());
        Assert.assertTrue(endNodeOfForwardEdge.outEdges().stream().map(edge -> edge.getIdentifier())
                .collect(Collectors.toList()).contains(edgeBackward.getIdentifier()));
        Assert.assertEquals(endNodeOfForwardEdge, edgeForward.end());
        Assert.assertNotNull(endNodeOfForwardEdge.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(endNodeOfForwardEdge.tag(LastEditUserIdentifierTag.KEY));
    }

    @Test
    public void testPoint()
    {
        // -74.8419221,23.6499071,-74.841058,23.6503894
        final Rectangle bound = Rectangle.forLocated(Location.forString("23.6499071,-74.8419221"),
                Location.forString("23.6503894,-74.841058"));
        final Atlas atlas = loadBahamas(bound);
        Assert.assertEquals(1, atlas.numberOfPoints());
        final Point point = atlas.points().iterator().next();

        Assert.assertEquals(821907853000000L, point.getIdentifier());
        Assert.assertEquals("POINT (-74.8413646 23.6501327)", point.getLocation().toString());
        Assert.assertEquals(9, point.getTags().size());
        Assert.assertEquals("Port Nelson", point.getTags().get("name"));
        Assert.assertNotNull(point.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(point.tag(LastEditUserIdentifierTag.KEY));
    }

    @Test
    public void testPolygonLoading()
    {
        final Rectangle largerBound = Rectangle.forLocated(
                Location.forString("25.0288172,-77.5420233"),
                Location.forString("25.0213741,-77.5237397"));
        final Atlas bigAtlas = loadBahamas(largerBound);

        final Polygon polygon = bigAtlas.area(24601488000000L).asPolygon();
        final Atlas smallAtlas = loadBahamas(polygon);

        Assert.assertTrue(bigAtlas.numberOfEdges() > smallAtlas.numberOfEdges());
        Assert.assertEquals(6, smallAtlas.numberOfEdges());
        Assert.assertEquals(7, smallAtlas.numberOfAreas());
    }

    @Test
    public void testRestrictionAndMultiPolygon()
    {
        final Rectangle bound = Location.forString("25.0812893, -77.3216682")
                .boxAround(Distance.kilometers(0.5));
        final Atlas atlas = loadBahamas(bound);

        final Map<Long, Relation> relations = new HashMap<>();
        atlas.relations().forEach(relation -> relations.put(relation.getIdentifier(), relation));
        Assert.assertEquals(3, relations.size());

        final Relation relation1 = relations.get(4309052000000L);
        Assert.assertEquals("restriction", relation1.getTags().get("type"));
        final RelationMemberList members1 = relation1.members();
        Assert.assertEquals(3, members1.size());
        Assert.assertEquals(1635708993000000L, members1.get(0).getEntity().getIdentifier());
        Assert.assertEquals("via", members1.get(0).getRole());
        Assert.assertEquals(150683353000000L, members1.get(1).getEntity().getIdentifier());
        Assert.assertEquals("from", members1.get(1).getRole());
        Assert.assertEquals(150683354000000L, members1.get(2).getEntity().getIdentifier());
        Assert.assertEquals("to", members1.get(2).getRole());

        final Relation relation2 = relations.get(1621692000000L);
        Assert.assertEquals("multipolygon", relation2.getTags().get("type"));
        final RelationMemberList members2 = relation2.members();
        Assert.assertEquals(2, members2.size());
        Assert.assertEquals(117365339000000L, members2.get(1).getEntity().getIdentifier());
        Assert.assertEquals("outer", members2.get(1).getRole());
        Assert.assertEquals(117365338000000L, members2.get(0).getEntity().getIdentifier());
        Assert.assertEquals("inner", members2.get(0).getRole());

        final Relation relation3 = relations.get(4309051000000L);
        Assert.assertEquals("restriction", relation3.getTags().get("type"));
        final RelationMemberList members3 = relation3.members();
        Assert.assertEquals(3, members3.size());
        Assert.assertEquals(719062796000000L, members3.get(0).getEntity().getIdentifier());
        Assert.assertEquals("via", members3.get(0).getRole());
        Assert.assertEquals(57942389000000L, members3.get(1).getEntity().getIdentifier());
        Assert.assertEquals("from", members3.get(1).getRole());
        Assert.assertEquals(318515851000000L, members3.get(2).getEntity().getIdentifier());
        Assert.assertEquals("to", members3.get(2).getRole());
    }

    @Test
    public void testRoute()
    {
        // This 250 meters range just covers part of the relation1
        final Rectangle bound = Location.forString("26.0845577, -77.5369822")
                .boxAround(Distance.meters(250));
        final Atlas atlas = loadBahamas(bound);

        final Map<Long, Relation> relations = new HashMap<>();
        atlas.relations().forEach(relation -> relations.put(relation.getIdentifier(), relation));
        Assert.assertEquals(2, relations.size());

        final Relation relation1 = relations.get(1251624000000L);
        Assert.assertEquals("route", relation1.getTags().get("type"));
        Assert.assertEquals("bus", relation1.getTags().get("route"));
        Assert.assertEquals("Adult Tram", relation1.getTags().get("name"));
        Assert.assertEquals(27, relation1.members().size());

        final Relation relation2 = relations.get(1245746000000L);
        Assert.assertEquals("route", relation2.getTags().get("type"));
        Assert.assertEquals("bus", relation2.getTags().get("route"));
        Assert.assertEquals("Tram", relation2.getTags().get("name"));
        Assert.assertEquals(18, relation2.members().size());
    }

    @Test
    public void testWaysSpanningOutsideOfCountry()
    {
        final Resource pbf = new InputStreamResource(
                () -> OsmPbfLoaderIntegrationTest.class.getResourceAsStream("CUB_72-111.pbf"));
        final CountryBoundaryMap map = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> OsmPbfLoaderIntegrationTest.class
                        .getResourceAsStream("CUB_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        final SlippyTile tile = SlippyTile.forName("8-72-111");
        final MultiPolygon boundary = map.countryBoundary("CUB").get(0).getBoundary();
        final MultiPolygon loadingArea = tile.bounds().clip(boundary, ClipType.AND)
                .getClipMultiPolygon();
        final OsmPbfLoader loader = new OsmPbfLoader(pbf, loadingArea,
                AtlasLoadingOption.createOptionWithAllEnabled(map));
        final Atlas atlas = loader.read();
        // Make sure that the big bridge over water made it to the Atlas
        Assert.assertNotNull(atlas.edge(308541861000000L));
    }
}
