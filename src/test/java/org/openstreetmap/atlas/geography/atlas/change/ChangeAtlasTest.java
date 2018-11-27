package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedArea;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedLine;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedPoint;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedRelation;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author matthieun
 */
public class ChangeAtlasTest
{
    @Rule
    public ChangeAtlasTestRule rule = new ChangeAtlasTestRule();

    @Test
    public void testBounds()
    {
        final Atlas atlas = this.rule.getAtlas();
        Assert.assertEquals("POLYGON ((-122.2450237 37.5920679, -122.2450237 37.5938783, "
                + "-122.2412753 37.5938783, -122.2412753 37.5920679, -122.2450237 37.5920679))",
                atlas.bounds().toWkt());

        final ChangeBuilder changeBuilder = new ChangeBuilder();
        // One-way motorway
        final Edge source = atlas.edge(39004000002L);
        final PolyLine newPolyLine = source.asPolyLine().shiftLastAlongGreatCircle(Heading.NORTH,
                Distance.ONE_METER);
        final Location end = newPolyLine.last();
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD,
                BloatedEdge.shallowFromEdge(source).withPolyLine(newPolyLine));
        changeBuilder.add(featureChange);
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                BloatedNode.shallowFromNode(atlas.node(38990000000L)).withLocation(end)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals("POLYGON ((-122.2450237 37.5920679, -122.2450237 37.5938873, "
                + "-122.2412753 37.5938873, -122.2412753 37.5920679, -122.2450237 37.5920679))",
                changeAtlas.bounds().toWkt());
    }

    @Test
    public void testChangeRelationTags()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        final Relation disconnectedFeatures = atlas.relation(41834000000L);
        final Map<String, String> tags = disconnectedFeatures.getTags();
        tags.put("newKey", "newValue");
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                BloatedRelation.shallowFromRelation(disconnectedFeatures).withTags(tags)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(tags, changeAtlas.relation(41834000000L).getTags());

        final Relation parentRelation = changeAtlas.relation(41860000000L);
        final Relation fromRelation = (Relation) Iterables.stream(parentRelation.members())
                .firstMatching(member -> "child1".equals(member.getRole())).get().getEntity();
        Assert.assertEquals(tags, fromRelation.getTags());
    }

    @Test
    public void testMoveArea()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        final Area source = atlas.area(41795000000L);
        final Polygon origin = source.asPolygon();
        final Polygon newPolygon = new Polygon(
                origin.shiftFirstAlongGreatCircle(Heading.NORTH, Distance.ONE_METER));
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                BloatedArea.shallowFromArea(source).withPolygon(newPolygon)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(newPolygon, changeAtlas.area(41795000000L).asPolygon());

        final Relation disconnectedFeatures = changeAtlas.relation(41834000000L);
        final Area fromRelation = (Area) Iterables.stream(disconnectedFeatures.members())
                .firstMatching(member -> "pond".equals(member.getRole())).get().getEntity();
        Assert.assertEquals(newPolygon, fromRelation.asPolygon());
    }

    @Test
    public void testMoveEdgeAndNode()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        final Edge source = atlas.edge(39004000002L);
        final PolyLine origin = source.asPolyLine();
        final PolyLine newPolyLine = origin.shiftLastAlongGreatCircle(Heading.NORTH,
                Distance.ONE_METER);
        final Location newLocation = newPolyLine.last();
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD,
                BloatedEdge.shallowFromEdge(source).withPolyLine(newPolyLine));
        changeBuilder.add(featureChange);
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                BloatedNode.shallowFromNode(atlas.node(38990000000L)).withLocation(newLocation)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        final Edge changeEdge = changeAtlas.edge(39004000002L);
        final Node changeNode = changeAtlas.node(38990000000L);
        Assert.assertEquals(newPolyLine, changeEdge.asPolyLine());
        Assert.assertEquals(newLocation, changeNode.getLocation());

        final Relation routeA = changeAtlas.relation(39010000000L);
        final Edge edgeFromRelation = (Edge) Iterables.stream(routeA.members())
                .firstMatching(member -> member.getEntity().getIdentifier() == 39004000002L).get()
                .getEntity();
        final Node nodeFromRelation = (Node) Iterables.stream(routeA.members())
                .firstMatching(member -> member.getEntity().getIdentifier() == 38990000000L).get()
                .getEntity();
        Assert.assertEquals(newPolyLine, edgeFromRelation.asPolyLine());
        Assert.assertEquals(newLocation, nodeFromRelation.getLocation());
    }

    @Test
    public void testMoveLine()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        final Line source = atlas.line(41771000000L);
        final PolyLine origin = source.asPolyLine();
        final PolyLine newPolyLine = origin.shiftFirstAlongGreatCircle(Heading.NORTH,
                Distance.ONE_METER);
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                BloatedLine.shallowFromLine(source).withPolyLine(newPolyLine)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(newPolyLine, changeAtlas.line(41771000000L).asPolyLine());

        final Relation disconnectedFeatures = changeAtlas.relation(41834000000L);
        final Line fromRelation = (Line) Iterables.stream(disconnectedFeatures.members())
                .firstMatching(member -> "river".equals(member.getRole())).get().getEntity();
        Assert.assertEquals(newPolyLine, fromRelation.asPolyLine());
    }

    @Test
    public void testMovePoint()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        final Point source = atlas.point(41822000000L);
        final Location newLocation = source.getLocation().shiftAlongGreatCircle(Heading.NORTH,
                Distance.ONE_METER);
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                BloatedPoint.shallowFromPoint(source).withLocation(newLocation)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(newLocation, changeAtlas.point(41822000000L).getLocation());

        final Relation disconnectedFeatures = changeAtlas.relation(41834000000L);
        final Point fromRelation = (Point) Iterables.stream(disconnectedFeatures.members())
                .firstMatching(member -> "tree".equals(member.getRole())).get().getEntity();
        Assert.assertEquals(newLocation, fromRelation.getLocation());
    }

    @Test
    public void testRemoveRelationMember()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        final Relation disconnectedFeatures = atlas.relation(41834000000L);
        // Remove the point from the list
        final RelationMemberList newMembers = new RelationMemberList(disconnectedFeatures.members()
                .stream().filter(member -> !(member.getEntity() instanceof Point))
                .collect(Collectors.toList()));
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                BloatedRelation.shallowFromRelation(disconnectedFeatures).withMembers(newMembers)));
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                BloatedPoint.shallowFromPoint(atlas.point(41822000000L))));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(newMembers.asBean(),
                changeAtlas.relation(41834000000L).members().asBean());

        final Relation parentRelation = changeAtlas.relation(41860000000L);
        final Relation fromRelation = (Relation) Iterables.stream(parentRelation.members())
                .firstMatching(member -> "child1".equals(member.getRole())).get().getEntity();
        Assert.assertEquals(newMembers.asBean(), fromRelation.members().asBean());
    }

    /**
     * Once relations can prune their member list when a member is not present, this test will pass.
     */
    @Ignore
    @Test
    public void testRemoveRelationMemberWithoutChangingMemberList()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        final Relation disconnectedFeatures = atlas.relation(41834000000L);
        // Remove the point from the list
        final RelationMemberList newMembers = new RelationMemberList(disconnectedFeatures.members()
                .stream().filter(member -> !(member.getEntity() instanceof Point))
                .collect(Collectors.toList()));
        // Here only remove the point. Do not remove the member in the relation. It should
        // automatically be removed.
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                BloatedPoint.shallowFromPoint(atlas.point(41822000000L))));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(newMembers.asBean(),
                changeAtlas.relation(41834000000L).members().asBean());

        final Relation parentRelation = changeAtlas.relation(41860000000L);
        final Relation fromRelation = (Relation) Iterables.stream(parentRelation.members())
                .firstMatching(member -> "child1".equals(member.getRole())).get().getEntity();
        Assert.assertEquals(newMembers.asBean(), fromRelation.members().asBean());
    }
}
