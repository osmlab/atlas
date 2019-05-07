package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

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
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.validators.FeatureChangeUsefulnessValidator;
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
    public void testAfterViewIsFull()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY, null, null));
        Assert.assertFalse(featureChange1.afterViewIsFull());
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, new CompleteArea(
                123L, Polygon.SILICON_VALLEY, Maps.hashMap("key1", "value2"), Sets.hashSet(123L)));
        Assert.assertTrue(featureChange2.afterViewIsFull());
    }

    @Test
    public void testBeforeViewUsefulnessValidationArea()
    {
        final Polygon polygon = Polygon.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteArea before = new CompleteArea(123L, polygon, tags, relations);

        final CompleteArea after = CompleteArea.shallowFrom(before).withPolygon(polygon)
                .withTags(tags).withRelationIdentifiers(relations);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testBeforeViewUsefulnessValidationEdge()
    {
        final PolyLine line = PolyLine.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Long startNode = 1L;
        final Long endNode = 2L;
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteEdge before = new CompleteEdge(123L, line, tags, startNode, endNode,
                relations);

        final CompleteEdge after = CompleteEdge.shallowFrom(before).withPolyLine(line)
                .withTags(tags).withStartNodeIdentifier(startNode).withEndNodeIdentifier(endNode)
                .withRelationIdentifiers(relations);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testBeforeViewUsefulnessValidationLine()
    {
        final PolyLine line = PolyLine.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteLine before = new CompleteLine(123L, line, tags, relations);

        final CompleteLine after = CompleteLine.shallowFrom(before).withPolyLine(line)
                .withTags(tags).withRelationIdentifiers(relations);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testBeforeViewUsefulnessValidationNode()
    {
        final Location location = Location.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final SortedSet<Long> inEdges = Sets.treeSet(1L, 2L);
        final SortedSet<Long> outEdges = Sets.treeSet(3L, 4L);
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteNode before = new CompleteNode(123L, location, tags, inEdges, outEdges,
                relations);

        final CompleteNode after = CompleteNode.shallowFrom(before).withLocation(location)
                .withTags(tags).withInEdgeIdentifiers(inEdges).withOutEdgeIdentifiers(outEdges)
                .withRelationIdentifiers(relations);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testBeforeViewUsefulnessValidationPoint()
    {
        final Location location = Location.CENTER;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompletePoint before = new CompletePoint(123L, location, tags, relations);

        final CompletePoint after = CompletePoint.shallowFrom(before).withLocation(location)
                .withTags(tags).withRelationIdentifiers(relations);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testBeforeViewUsefulnessValidationRelation()
    {
        final Rectangle bounds = Rectangle.TEST_RECTANGLE;
        final Map<String, String> tags = Maps.hashMap("a", "1", "b", "2");
        final RelationBean members = new RelationBean();
        members.addItem(new RelationBeanItem(1L, "role", ItemType.POINT));
        final List<Long> allRelationsWithSameOsmIdentifier = Arrays.asList(1L, 2L);
        final RelationBean allKnownOsmMembers = new RelationBean();
        allKnownOsmMembers.addItem(new RelationBeanItem(2L, "role2", ItemType.AREA));
        final Long osmRelationIdentifier = 456L;
        final Set<Long> relations = Sets.hashSet(1L, 2L);

        final CompleteRelation before = new CompleteRelation(123L, tags, bounds, members,
                allRelationsWithSameOsmIdentifier, allKnownOsmMembers, osmRelationIdentifier,
                relations);

        final CompleteRelation after = CompleteRelation.shallowFrom(before).withTags(tags)
                .withMembers(members, bounds).withRelationIdentifiers(relations)
                .withAllRelationsWithSameOsmIdentifier(allRelationsWithSameOsmIdentifier)
                .withAllKnownOsmMembers(allKnownOsmMembers)
                .withOsmRelationIdentifier(osmRelationIdentifier);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is not useful");
        new FeatureChangeUsefulnessValidator(new FeatureChange(ChangeType.ADD, after, before))
                .validate();
    }

    @Test
    public void testShallowValidation()
    {
        final CompletePoint before = new CompletePoint(123L, Location.CENTER,
                Maps.hashMap("a", "1", "b", "2"), Sets.hashSet(1L, 2L));

        final CompletePoint after = CompletePoint.shallowFrom(before);
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("was shallow");
        FeatureChange.add(after);
    }

    @Test
    public void testTags()
    {
        final String key = "key1";
        final String value = "value1";
        final Map<String, String> tags = Maps.hashMap(key, value, "key2", "value2");
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.CENTER, tags, null));
        Assert.assertEquals(new HashMap<>(tags), featureChange.getTags());
        Assert.assertEquals(value, featureChange.getTag(key).get());
        Assert.assertTrue(featureChange.toString().contains(tags.toString()));
    }
}
