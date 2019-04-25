package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * @author matthieun
 */
public class ChangeAtlasTest
{
    private static final Location NEW_LOCATION = Location.forString("37.592796,-122.2457961");

    @Rule
    public ChangeAtlasTestRule rule = new ChangeAtlasTestRule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

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
                CompleteEdge.shallowFrom(source).withPolyLine(newPolyLine));
        changeBuilder.add(featureChange);
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                CompleteNode.shallowFrom(atlas.node(38990000000L)).withLocation(end)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals("POLYGON ((-122.2450237 37.5920679, -122.2450237 37.5938873, "
                + "-122.2412753 37.5938873, -122.2412753 37.5920679, -122.2450237 37.5920679))",
                changeAtlas.bounds().toWkt());
    }

    @Test
    public void testBuildFromScratch()
    {
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        final FeatureChange featureChange1 = FeatureChange.add(new CompleteArea(123L,
                Polygon.SILICON_VALLEY, Maps.hashMap("k", "v"), Sets.hashSet(123L)));
        changeBuilder.add(featureChange1);
        final Change change = changeBuilder.get();
        final Atlas result = new ChangeAtlas(change);
        Assert.assertNotNull(result.area(123L));
        Assert.assertEquals(0, Iterables.size(result.points()));
    }

    @Test
    public void testBuildFromScratchError()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("ChangeAtlas needs all ADD featureChanges to be full");
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        changeBuilder.add(getFeatureChangeUpdatedEdgePolyLine().getFirst());
        final Change change = changeBuilder.get();
        new ChangeAtlas(change);
    }

    @Test
    public void testChangeRelationTags()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        final Relation disconnectedFeatures = atlas.relation(41834000000L);
        final Map<String, String> tags = disconnectedFeatures.getTags();
        tags.put("newKey", "newValue");
        changeBuilder.add(FeatureChange
                .add(CompleteRelation.shallowFrom(disconnectedFeatures).withTags(tags)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        final Relation changeRelation = changeAtlas.relation(41834000000L);
        Assert.assertEquals(tags, changeRelation.getTags());
        Assert.assertEquals(disconnectedFeatures.members().asBean(),
                changeRelation.members().asBean());

        final Relation parentRelation = changeAtlas.relation(41860000000L);
        final Relation changeRelationFromParentRelation = (Relation) Iterables
                .stream(parentRelation.members())
                .firstMatching(member -> "child1".equals(member.getRole())).get().getEntity();
        Assert.assertEquals(tags, changeRelationFromParentRelation.getTags());
        Assert.assertEquals(disconnectedFeatures.members().asBean(),
                changeRelationFromParentRelation.members().asBean());
    }

    @Test
    public void testModifyEdgeAndNode()
    {
        final Atlas atlas = this.rule.getAtlasEdge();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        final Tuple<FeatureChange, FeatureChange> featureChange1 = getFeatureChangeUpdatedEdgePolyLine();
        changeBuilder.add(featureChange1.getFirst());
        changeBuilder.add(featureChange1.getSecond());

        changeBuilder.add(getFeatureChangeMovedNode());

        final Change change = changeBuilder.get();
        Assert.assertEquals("[Edge: id=39001000001, startNode=38999000000, endNode=39002000000, "
                + "polyLine=LINESTRING (-122.2457961 37.592796, -122.2450237 37.5926929, "
                + "-122.2441049 37.5930666, -122.2429584 37.5926993), "
                + "[Tags: [last_edit_user_name => myself], [last_edit_changeset => 1], "
                + "[last_edit_time => 1513719782000], [last_edit_user_id => 1], [name => primary], "
                + "[highway => primary], [last_edit_version => 1]]]",
                new ChangeAtlas(atlas, change).edge(39001000001L).toString());
    }

    @Test
    public void testModifyEdgeWithoutStartNode()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not match with its start Node");

        final Atlas atlas = this.rule.getAtlasEdge();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        final Tuple<FeatureChange, FeatureChange> featureChange1 = getFeatureChangeUpdatedEdgePolyLine();
        changeBuilder.add(featureChange1.getFirst());
        changeBuilder.add(featureChange1.getSecond());

        final Change change = changeBuilder.get();
        new ChangeAtlas(atlas, change);
    }

    @Test
    public void testModifyForwardEdgeWithoutReverseEdge()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("have mismatching PolyLines");

        final Atlas atlas = this.rule.getAtlasEdge();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        final Edge edge = atlas.edge(39001000001L);
        final PolyLine oldPolyLine = edge.asPolyLine();
        final PolyLine newPolyLine = new PolyLine(oldPolyLine.first(), NEW_LOCATION,
                oldPolyLine.last());
        final CompleteEdge bloatedEdge = CompleteEdge.shallowFrom(edge).withPolyLine(newPolyLine);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, bloatedEdge);
        changeBuilder.add(featureChange);

        final Change change = changeBuilder.get();
        new ChangeAtlas(atlas, change);
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
                CompleteArea.shallowFrom(source).withPolygon(newPolygon)));
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
                CompleteEdge.shallowFrom(source).withPolyLine(newPolyLine));
        changeBuilder.add(featureChange);
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                CompleteNode.shallowFrom(atlas.node(38990000000L)).withLocation(newLocation)));
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
                CompleteLine.shallowFrom(source).withPolyLine(newPolyLine)));
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
                CompletePoint.shallowFrom(source).withLocation(newLocation)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(newLocation, changeAtlas.point(41822000000L).getLocation());

        final Relation disconnectedFeatures = changeAtlas.relation(41834000000L);
        final Point fromRelation = (Point) Iterables.stream(disconnectedFeatures.members())
                .firstMatching(member -> "tree".equals(member.getRole())).get().getEntity();
        Assert.assertEquals(newLocation, fromRelation.getLocation());
    }

    @Test
    public void testNodePropertyMergeFromInconsistentBeforeAtlases()
    {
        final Atlas fullSizedAtlas = this.rule.differentNodeAndEdgeProperties1();
        final Atlas subbedAtlas = this.rule.differentNodeAndEdgeProperties2();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        // remove Edge -1
        changeBuilder.add(FeatureChange.remove(CompleteEdge.shallowFrom(fullSizedAtlas.edge(-1L)),
                fullSizedAtlas));

        // remove Edge -1 from the referenced out edges of node2
        final CompleteNode node2FromFullAtlas = CompleteNode.shallowFrom(fullSizedAtlas.node(2L));
        node2FromFullAtlas
                .withOutEdgeIdentifiers(fullSizedAtlas.node(2L).outEdges().stream()
                        .map(Edge::getIdentifier).collect(Collectors.toCollection(TreeSet::new)))
                .withOutEdgeIdentifierLess(-1L);
        changeBuilder.add(FeatureChange.add(node2FromFullAtlas, fullSizedAtlas));

        // change a tag in node 2, but use a different atlas context that cannot see edge -1
        final CompleteNode node2FromSubbedAtlas = CompleteNode.shallowFrom(subbedAtlas.node(2L));
        node2FromSubbedAtlas.withTags(subbedAtlas.node(2L).getTags()).withAddedTag("new", "tag");
        changeBuilder.add(FeatureChange.add(node2FromSubbedAtlas, subbedAtlas));

        final Atlas changeAtlas = new ChangeAtlas(fullSizedAtlas, changeBuilder.get());

        final Set<Long> goldenOutEdgeIdentifiers = Sets.hashSet(2L);
        Assert.assertEquals(goldenOutEdgeIdentifiers, changeAtlas.node(2L).outEdges().stream()
                .map(Edge::getIdentifier).collect(Collectors.toSet()));
    }

    @Test
    public void testRemovedRelationMemberFromIndirectRemoval()
    {
        final Atlas atlas = this.rule.getAtlas();

        // Remove a node. This removal will trigger an indirect removal of the connected edges. This
        // should also trigger an indirect removal of these members from any relation member lists.
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompleteNode.shallowFrom(atlas.node(38984000000L))));
        final Atlas changeAtlas = new ChangeAtlas(atlas, changeBuilder.get());
        final RelationMemberList memberList = changeAtlas.relation(39008000000L).members();
        final RelationMemberList newMembers = new RelationMemberList(
                changeAtlas.relation(39008000000L).members().stream()
                        .filter(member -> member.getEntity().getIdentifier() == -39002000001L
                                || member.getEntity().getIdentifier() == 39002000001L)
                        .collect(Collectors.toList()));
        Assert.assertEquals(newMembers, memberList);

        // Now, let's do the same thing but remove an additional node. This will trigger the
        // indirect removal of all the relation's members - so we should see the relation disappear.
        final ChangeBuilder changeBuilder2 = new ChangeBuilder();
        changeBuilder2.add(new FeatureChange(ChangeType.REMOVE,
                CompleteNode.shallowFrom(atlas.node(38984000000L))));
        changeBuilder2.add(new FeatureChange(ChangeType.REMOVE,
                CompleteNode.shallowFrom(atlas.node(38982000000L))));
        final Atlas changeAtlas2 = new ChangeAtlas(atlas, changeBuilder2.get());
        Assert.assertNull(changeAtlas2.relation(39008000000L));
    }

    @Test
    public void testRemoveEdgeWhenAConnectedNodeIsMissing()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompleteNode.shallowFrom(atlas.node(38982000000L))));
        final Atlas changeAtlas = new ChangeAtlas(atlas, changeBuilder.get());

        // Check that appropriate edges were deleted, triggered by removal of the node
        Assert.assertNull(changeAtlas.node(38982000000L));
        Assert.assertNull(changeAtlas.edge(39004000002L));
        Assert.assertNull(changeAtlas.edge(39004000001L));
        Assert.assertNull(changeAtlas.edge(39002000001L));
        Assert.assertNull(changeAtlas.edge(-39002000001L));
        Assert.assertNull(changeAtlas.edge(39002000002L));
        Assert.assertNull(changeAtlas.edge(-39002000002L));

        // Check that appropriate features were left alone
        Assert.assertNotNull(changeAtlas.node(38990000000L));
        Assert.assertNotNull(changeAtlas.node(38978000000L));
        Assert.assertNotNull(changeAtlas.node(38986000000L));
        Assert.assertNotNull(changeAtlas.node(38984000000L));
        Assert.assertNotNull(changeAtlas.edge(39006000001L));
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
        changeBuilder.add(
                new FeatureChange(ChangeType.ADD, CompleteRelation.shallowFrom(disconnectedFeatures)
                        .withMembersAndSource(newMembers, disconnectedFeatures)));
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompletePoint.shallowFrom(atlas.point(41822000000L))));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(newMembers.asBean(),
                changeAtlas.relation(41834000000L).members().asBean());

        final Relation parentRelation = changeAtlas.relation(41860000000L);
        final Relation fromRelation = (Relation) Iterables.stream(parentRelation.members())
                .firstMatching(member -> "child1".equals(member.getRole())).get().getEntity();
        Assert.assertEquals(newMembers.asBean(), fromRelation.members().asBean());
    }

    @Test
    public void testRemoveRelationMemberIsReflectedInMemberListAutomatically()
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
                CompletePoint.shallowFrom(atlas.point(41822000000L))));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(newMembers.asBean(),
                changeAtlas.relation(41834000000L).members().asBean());

        final Relation parentRelation = changeAtlas.relation(41860000000L);
        final Relation fromRelation = (Relation) Iterables.stream(parentRelation.members())
                .firstMatching(member -> "child1".equals(member.getRole())).get().getEntity();
        Assert.assertEquals(newMembers.asBean(), fromRelation.members().asBean());
    }

    @Test
    public void testRemoveShallowRelations()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        // These changes remove all members of relations 41834000000 and 39008000000. Both of these
        // relations will be dropped, due to becoming empty. Additionally, relation 41860000000
        // will also become shallow and be dropped, because its only 2 members are the
        // aforementioned empty relations. Finally. relation 41861000000 will also be dropped, since
        // its only member was relation 41860000000, which was dropped due to becoming shallow.
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompleteEdge.shallowFrom(atlas.edge(39002000001L))));
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompleteEdge.shallowFrom(atlas.edge(-39002000001L))));
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompleteEdge.shallowFrom(atlas.edge(39002000002L))));
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompleteEdge.shallowFrom(atlas.edge(-39002000002L))));
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompleteEdge.shallowFrom(atlas.edge(39006000001L))));
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompletePoint.shallowFrom(atlas.point(41822000000L))));
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompleteLine.shallowFrom(atlas.line(41771000000L))));
        changeBuilder.add(new FeatureChange(ChangeType.REMOVE,
                CompleteArea.shallowFrom(atlas.area(41795000000L))));

        // Now, we are going to add a new relation 41862000000 to the changeset. However, the new
        // relation will only have a single member, which is a shallow relation that will be
        // removed. So this brand new relation will also become shallow, and should also be removed.
        final RelationBean newBean = new RelationBean();
        newBean.addItem(41861000000L, "someChild", ItemType.RELATION);
        changeBuilder.add(
                new FeatureChange(ChangeType.ADD, new CompleteRelation(41862000000L, Maps.hashMap(),
                        atlas.relation(41861000000L).bounds(), newBean, null, null, null, null)));

        // Build the ChangeAtlas and verify relations were removed correctly
        final Atlas changeAtlas = new ChangeAtlas(atlas, changeBuilder.get());
        Assert.assertNull(changeAtlas.relation(41834000000L));
        Assert.assertNull(changeAtlas.relation(39008000000L));
        Assert.assertNull(changeAtlas.relation(41860000000L));
        Assert.assertNull(changeAtlas.relation(41861000000L));
        Assert.assertNull(changeAtlas.relation(41862000000L));

        // Check to make sure we did not accidentally drop relation 39010000000. This relation
        // contains members which still exist and so must be preserved.
        Assert.assertFalse(changeAtlas.relation(39010000000L).members().isEmpty());
        Assert.assertNotNull(changeAtlas.relation(39010000000L));
    }

    /**
     * @return Feature change 2: Update the edge 39001000001L's start node: 38999000000L
     */
    private FeatureChange getFeatureChangeMovedNode()
    {
        final Atlas atlas = this.rule.getAtlasEdge();

        final Node originalNode = atlas.node(38999000000L);
        final CompleteNode bloatedNode = CompleteNode.shallowFrom(originalNode)
                .withLocation(NEW_LOCATION);
        return new FeatureChange(ChangeType.ADD, bloatedNode);
    }

    /**
     * @return Feature change 1: Update the first location in the edge 39001000001L's polyLine
     */
    private Tuple<FeatureChange, FeatureChange> getFeatureChangeUpdatedEdgePolyLine()
    {
        final Atlas atlas = this.rule.getAtlasEdge();

        final Edge originalEdge1 = atlas.edge(39001000001L);
        final Edge originalEdge1Reverse = atlas.edge(-39001000001L);

        // Forward:
        final PolyLine originalPolyLine1 = originalEdge1.asPolyLine();
        final PolyLine originalPolyLine1Modified = new PolyLine(
                originalPolyLine1.prepend(new PolyLine(NEW_LOCATION, originalPolyLine1.first())));
        final CompleteEdge bloatedEdge1 = CompleteEdge.shallowFrom(originalEdge1)
                .withPolyLine(originalPolyLine1Modified);
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, bloatedEdge1);

        // Backward
        final PolyLine originalPolyLine1Reverse = originalEdge1Reverse.asPolyLine();
        final PolyLine originalPolyLine1ModifiedReverse = new PolyLine(originalPolyLine1Reverse
                .append(new PolyLine(originalPolyLine1Reverse.last(), NEW_LOCATION)));
        final CompleteEdge bloatedEdge1Reverse = CompleteEdge.shallowFrom(originalEdge1Reverse)
                .withPolyLine(originalPolyLine1ModifiedReverse);
        final FeatureChange featureChange1Reverse = new FeatureChange(ChangeType.ADD,
                bloatedEdge1Reverse);

        return new Tuple<>(featureChange1, featureChange1Reverse);
    }
}
