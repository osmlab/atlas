package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class FeatureChangeTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAreaSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD, new CompleteArea(123L, null, null, null));
    }

    @Test
    public void testEdgeSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD, new CompleteEdge(123L, null, null, null, null, null));
    }

    @Test
    public void testLineSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD, new CompleteLine(123L, null, null, null));
    }

    @Test
    public void testMergeAddRemove()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, null, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new CompletePoint(123L, null, null, null));
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeDifferentType()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, null, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, Maps.hashMap(), null));
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeLocationCollision()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.COLOSSEUM, null, null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.EIFFEL_TOWER, null, null));
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeLocations()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.COLOSSEUM, null, null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.COLOSSEUM, null, null));
        Assert.assertEquals(Location.COLOSSEUM,
                ((LocationItem) featureChange1.merge(featureChange2).getUpdatedView())
                        .getLocation());
    }

    @Test
    public void testMergeNodes()
    {
        final CompleteNode beforeNode = new CompleteNode(123L, null,
                Maps.hashMap("a", "1", "b", "2"), Sets.treeSet(1L, 2L, 3L),
                Sets.treeSet(10L, 11L, 12L), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteNode(123L, null, Maps.hashMap("a", "1", "c", "3"),
                        Sets.treeSet(1L, 2L, 3L, 4L), Sets.treeSet(10L, 11L, 12L, 13L), null),
                beforeNode);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteNode(123L, null, Maps.hashMap("a", "1", "b", "2", "c", "3"),
                        Sets.treeSet(1L, 2L, 3L, 5L), Sets.treeSet(10L, 11L), null),
                beforeNode);

        // Testing with a hashset instead of a treeset for ease of typing
        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L, 4L, 5L),
                ((Node) featureChange1.merge(featureChange2).getUpdatedView()).inEdges().stream()
                        .map(edge -> edge.getIdentifier()).collect(Collectors.toSet()));
        Assert.assertEquals(Sets.hashSet(10L, 11L, 13L),
                ((Node) featureChange1.merge(featureChange2).getUpdatedView()).outEdges().stream()
                        .map(edge -> edge.getIdentifier()).collect(Collectors.toSet()));
        Assert.assertEquals(Maps.hashMap("a", "1", "c", "3"),
                ((Node) featureChange1.merge(featureChange2).getUpdatedView()).getTags());
    }

    @Test
    public void testMergePolygons()
    {
        final Polygon result = new Polygon(Location.COLOSSEUM, Location.EIFFEL_TOWER,
                Location.CENTER);
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, result, null, null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, result, null, null));
        Assert.assertEquals(result,
                ((Area) featureChange1.merge(featureChange2).getUpdatedView()).asPolygon());
    }

    @Test
    public void testMergePolygonsCollision()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L,
                        new Polygon(Location.COLOSSEUM, Location.EIFFEL_TOWER, Location.CENTER),
                        null, null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L,
                        new Polygon(Location.EIFFEL_TOWER, Location.COLOSSEUM, Location.CENTER),
                        null, null));
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergePolyLines()
    {
        final PolyLine result = new PolyLine(Location.COLOSSEUM, Location.EIFFEL_TOWER);
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteLine(123L, result, null, null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteLine(123L, result, null, null));
        Assert.assertEquals(result,
                ((LineItem) featureChange1.merge(featureChange2).getUpdatedView()).asPolyLine());
    }

    @Test
    public void testMergePolyLinesCollision()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, new CompleteLine(
                123L, new PolyLine(Location.COLOSSEUM, Location.EIFFEL_TOWER), null, null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, new CompleteLine(
                123L, new PolyLine(Location.EIFFEL_TOWER, Location.COLOSSEUM), null, null));
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeRelationMembers()
    {
        final RelationBean members = new RelationBean();
        members.addItem(new RelationBeanItem(456L, "myRole", ItemType.AREA));
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, new CompleteRelation(
                123L, null, Rectangle.TEST_RECTANGLE, members, null, null, null, null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, new CompleteRelation(
                123L, null, Rectangle.TEST_RECTANGLE, members, null, null, null, null));
        Assert.assertEquals(members,
                ((Relation) featureChange1.merge(featureChange2).getUpdatedView()).members()
                        .asBean());
    }

    @Test
    public void testMergeRelationMembersPartial()
    {
        final RelationBean members1 = new RelationBean();
        members1.addItem(new RelationBeanItem(456L, "myRole1", ItemType.AREA));
        final RelationBean members2 = new RelationBean();
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, new CompleteRelation(
                123L, null, Rectangle.TEST_RECTANGLE, members1, null, null, null, null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, new CompleteRelation(
                123L, null, Rectangle.TEST_RECTANGLE, members2, null, null, null, null));
        Assert.assertEquals(members1,
                ((Relation) featureChange1.merge(featureChange2).getUpdatedView()).members()
                        .asBean());
    }

    @Test
    public void testMergeRelationMembersRoleCollision()
    {
        // OSM allows (but discourages) the same feature to appear multiple times with the same or
        // different roles.
        final RelationBean members1 = new RelationBean();
        members1.addItem(new RelationBeanItem(456L, "myRole1", ItemType.AREA));
        final RelationBean members2 = new RelationBean();
        members2.addItem(new RelationBeanItem(456L, "myRole2", ItemType.AREA));
        final RelationBean result = new RelationBean();
        result.addItem(new RelationBeanItem(456L, "myRole1", ItemType.AREA));
        result.addItem(new RelationBeanItem(456L, "myRole2", ItemType.AREA));
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, new CompleteRelation(
                123L, null, Rectangle.TEST_RECTANGLE, members1, null, null, null, null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, new CompleteRelation(
                123L, null, Rectangle.TEST_RECTANGLE, members2, null, null, null, null));
        Assert.assertEquals(result,
                ((Relation) featureChange1.merge(featureChange2).getUpdatedView()).members()
                        .asBean());
    }

    @Test
    public void testMergeRelations()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, null, Sets.hashSet(456L)));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, null, Sets.hashSet(567L)));
        Assert.assertEquals(Sets.hashSet(456L, 567L),
                Iterables.stream(featureChange1.merge(featureChange2).getUpdatedView().relations())
                        .map(Relation::getIdentifier).collectToSet());
    }

    @Test
    public void testMergeRelationsCollision()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, null, Sets.hashSet(456L)));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, null, Sets.hashSet(456L)));
        Assert.assertEquals(Sets.hashSet(456L),
                Iterables.stream(featureChange1.merge(featureChange2).getUpdatedView().relations())
                        .map(Relation::getIdentifier).collectToSet());
    }

    @Test
    public void testMergeTags()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, Maps.hashMap("key1", "value1"), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, Maps.hashMap("key2", "value2"), null));
        Assert.assertEquals(Maps.hashMap("key1", "value1", "key2", "value2"),
                featureChange1.merge(featureChange2).getUpdatedView().getTags());
    }

    @Test
    public void testMergeTagsCollision()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, Maps.hashMap("key1", "value1"), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, Maps.hashMap("key1", "value2"), null));
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testNodeSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD, new CompleteNode(123L, null, null, null, null, null));
    }

    @Test
    public void testPointSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD, new CompletePoint(123L, null, null, null));
    }

    @Test
    public void testRelationSuperShallow()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not contain anything useful.");

        new FeatureChange(ChangeType.ADD,
                new CompleteRelation(123L, null, null, null, null, null, null, null));
    }

    @Test
    public void testTags()
    {
        final String key = "key1";
        final String value = "value1";
        final Map<String, String> tags = Maps.hashMap(key, value, "key2", "value2");
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, tags, null));
        Assert.assertEquals(new HashMap<>(tags), featureChange.getTags());
        Assert.assertEquals(value, featureChange.getTag(key).get());
        Assert.assertTrue(featureChange.toString().contains(tags.toString()));
    }
}
