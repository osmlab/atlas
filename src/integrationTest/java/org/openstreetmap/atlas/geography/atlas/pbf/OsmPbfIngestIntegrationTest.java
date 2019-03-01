package org.openstreetmap.atlas.geography.atlas.pbf;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
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
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator;
import org.openstreetmap.atlas.geography.atlas.raw.sectioning.WaySectionProcessor;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasCountrySlicer;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.clipping.Clip.ClipType;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LastEditTimeTag;
import org.openstreetmap.atlas.tags.LastEditUserIdentifierTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author tony
 */
public class OsmPbfIngestIntegrationTest extends AtlasIntegrationTest
{
    @Test
    public void testAreaAndLine()
    {
        final Rectangle bound = Location.forString("25.0771736, -77.3597574")
                .boxAround(Distance.meters(300));
        final Atlas atlas = loadBahamas(bound);

        Assert.assertEquals(24, atlas.numberOfAreas());
        Assert.assertEquals(11, atlas.numberOfLines());

        final Area smallArea = atlas.area(522592143000000L);
        Assert.assertEquals(true, BuildingTag.isBuilding(smallArea));
        Assert.assertEquals(4, Iterables.size(smallArea));
        Assert.assertNotNull(smallArea.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(smallArea.tag(LastEditUserIdentifierTag.KEY));

        final Area bigArea = atlas.area(191814889000000L);
        Assert.assertEquals(false, BuildingTag.isBuilding(bigArea));
        Assert.assertEquals("attraction", bigArea.getTags().get("tourism"));
        Assert.assertEquals("Fort Charlotte", bigArea.getTags().get("name"));
        Assert.assertEquals(20, Iterables.size(bigArea));
        Assert.assertNotNull(bigArea.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(bigArea.tag(LastEditUserIdentifierTag.KEY));

        final Line longLine = atlas.line(374341334000000L);
        Assert.assertEquals("drain", longLine.getTags().get("waterway"));
        Assert.assertEquals("Storm Water Drain", longLine.getTags().get("name"));
        Assert.assertEquals(24, Iterables.size(longLine));
        Assert.assertNotNull(longLine.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(longLine.tag(LastEditUserIdentifierTag.KEY));
    }

    @Test
    public void testEdgeAndNode()
    {
        final Rectangle bound = Location.forString("25.0693383, -77.3160218")
                .boxAround(Distance.feet(1));
        final Atlas atlas = loadBahamas(bound);
        Assert.assertEquals(6, atlas.numberOfEdges());

        final Edge edgeForward = atlas.edge(63423376000000L);
        Assert.assertEquals(63423376000000L, edgeForward.getIdentifier());
        Assert.assertEquals(HighwayTag.RESIDENTIAL, edgeForward.highwayTag());
        Assert.assertEquals(6, Iterables.size(edgeForward.getRawGeometry()));
        Assert.assertNotNull(edgeForward.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(edgeForward.tag(LastEditUserIdentifierTag.KEY));

        final Edge edgeBackward = atlas.edge(-63423376000000L);
        Assert.assertEquals(-63423376000000L, edgeBackward.getIdentifier());
        Assert.assertEquals(HighwayTag.RESIDENTIAL, edgeBackward.highwayTag());
        Assert.assertEquals(6, Iterables.size(edgeBackward.getRawGeometry()));
        Assert.assertNotNull(edgeBackward.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(edgeBackward.tag(LastEditUserIdentifierTag.KEY));

        Assert.assertEquals(Iterables.first(edgeForward.getRawGeometry()).get(),
                Iterables.last(edgeBackward.getRawGeometry()).get());

        final Node startNodeOfForwardEdge = atlas.node(786050062000000L);
        Assert.assertEquals("POINT (-77.3160218 25.0693383)",
                startNodeOfForwardEdge.getLocation().toString());
        Assert.assertEquals(3, startNodeOfForwardEdge.inEdges().size());
        Assert.assertTrue(
                startNodeOfForwardEdge.inEdges().stream().map(edge -> edge.getIdentifier())
                        .collect(Collectors.toList()).contains(edgeBackward.getIdentifier()));
        Assert.assertEquals(3, startNodeOfForwardEdge.outEdges().size());
        Assert.assertTrue(
                startNodeOfForwardEdge.outEdges().stream().map(edge -> edge.getIdentifier())
                        .collect(Collectors.toList()).contains(edgeForward.getIdentifier()));
        Assert.assertEquals(startNodeOfForwardEdge, edgeForward.start());
        Assert.assertNotNull(startNodeOfForwardEdge.tag(LastEditTimeTag.KEY));
        Assert.assertNotNull(startNodeOfForwardEdge.tag(LastEditUserIdentifierTag.KEY));

        final Node endNodeOfForwardEdge = atlas.node(4354620579000000L);
        Assert.assertEquals("POINT (-77.3149029 25.0691753)",
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
        final Rectangle bound = Location.forString("25.0735519,-77.3073068")
                .boxAround(Distance.inches(1));
        final Atlas atlas = loadBahamas(bound);
        Assert.assertEquals(1, atlas.numberOfPoints());
        final Point point = atlas.points().iterator().next();

        Assert.assertEquals(5665510971000000L, point.getIdentifier());
        Assert.assertEquals("POINT (-77.3073068 25.0735519)", point.getLocation().toString());
        Assert.assertEquals(6, point.getTags().size());
        Assert.assertEquals("viewpoint", point.getTags().get("tourism"));
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
        Assert.assertEquals(40, smallAtlas.numberOfEdges());
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
        Assert.assertEquals(9, relations.size());

        final Relation relation1 = relations.get(1251624000000L);
        Assert.assertEquals("route", relation1.getTags().get("type"));
        Assert.assertEquals("bus", relation1.getTags().get("route"));
        Assert.assertEquals("Adult Tram", relation1.getTags().get("name"));
        Assert.assertEquals(23, relation1.members().size());

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
                () -> OsmPbfIngestIntegrationTest.class.getResourceAsStream("CUB_72-111.pbf"));
        final CountryBoundaryMap map = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> OsmPbfIngestIntegrationTest.class
                        .getResourceAsStream("CUB_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        final SlippyTile tile = SlippyTile.forName("8-72-111");
        final MultiPolygon boundary = map.countryBoundary("CUB").get(0).getBoundary();
        final MultiPolygon loadingArea = tile.bounds().clip(boundary, ClipType.AND)
                .getClipMultiPolygon();

        final AtlasLoadingOption loadingOption = AtlasLoadingOption.createOptionWithAllEnabled(map);
        Atlas atlas = new RawAtlasGenerator(pbf, loadingOption, loadingArea).build();
        atlas = new RawAtlasCountrySlicer(map.getLoadedCountries(), map).slice(atlas);
        atlas = new WaySectionProcessor(atlas, loadingOption).run();
        // Make sure that the big bridge over water made it to the Atlas
        Assert.assertNotNull(atlas.edge(308541861000000L));
    }
}
