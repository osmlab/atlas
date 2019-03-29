package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Arrays;
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
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author lcram
 */
public class FeatureChangeMergerTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testMergeAreasFail()
    {
        final CompleteArea beforeArea1 = new CompleteArea(123L, Polygon.CENTER,
                Maps.hashMap("a", "1", "b", "2"), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY_2, null, null), beforeArea1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY, Maps.hashMap("a", "1"), null),
                beforeArea1);

        /*
         * This merge will fail, because the FeatureChanges have conflicting changed polygons. There
         * is no way to resolve conflicting geometry during a merge.
         */
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeAreasSuccess()
    {
        final CompleteArea beforeArea1 = new CompleteArea(123L, Polygon.SILICON_VALLEY,
                Maps.hashMap("a", "1", "b", "2", "c", "3", "d", "4", "e", "5"), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY_2,
                        Maps.hashMap("a", "1", "b", "12", "d", "4", "y", "25"), null),
                beforeArea1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY_2,
                        Maps.hashMap("a", "1", "b", "12", "c", "3", "d", "4", "z", "26"), null),
                beforeArea1);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * Here we have a busy tag map merge. The left and right both add and remove things on their
         * own, as well as share some removes and modifies. We also check that the area geometry
         * updated correctly.
         */
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "12", "d", "4", "y", "25", "z", "26"),
                ((Area) merged.getAfterView()).getTags());

        Assert.assertEquals(Polygon.SILICON_VALLEY_2, ((Area) merged.getAfterView()).asPolygon());
    }

    @Test
    public void testMergeAreasWithNonConflictingChangedFields()
    {
        final CompleteArea beforeArea1 = new CompleteArea(123L, Polygon.SILICON_VALLEY, null,
                Sets.hashSet(1L, 2L, 3L));
        final CompleteArea beforeArea2 = new CompleteArea(123L, Polygon.SILICON_VALLEY,
                Maps.hashMap("a", "1", "b", "2"), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY, null, Sets.hashSet(1L, 2L, 3L, 4L)),
                beforeArea1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY,
                        Maps.hashMap("a", "1", "b", "2", "c", "3"), null),
                beforeArea2);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * We check that the simple tag ADD [c=3] gets merged properly. Also, we check that the
         * parent relations set is merged.
         */
        Assert.assertEquals(Polygon.SILICON_VALLEY, ((Area) merged.getAfterView()).asPolygon());

        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2", "c", "3"),
                ((Area) merged.getAfterView()).getTags());

        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L, 4L), ((Area) merged.getAfterView()).relations()
                .stream().map(relation -> relation.getIdentifier()).collect(Collectors.toSet()));

        // Test that the beforeView was merged properly
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2"),
                ((Area) merged.getBeforeView()).getTags());

        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L), ((Area) merged.getBeforeView()).relations()
                .stream().map(relation -> relation.getIdentifier()).collect(Collectors.toSet()));
    }

    @Test
    public void testMergeEdgesFail()
    {
        final CompleteEdge beforeEdge1 = new CompleteEdge(123L, PolyLine.TEST_POLYLINE,
                Maps.hashMap("a", "1"), 1L, null, null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, new CompleteEdge(
                123L, PolyLine.TEST_POLYLINE, Maps.hashMap("a", "2"), 2L, null, null), beforeEdge1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, new CompleteEdge(
                123L, PolyLine.TEST_POLYLINE, Maps.hashMap("a", "3"), 1L, null, null), beforeEdge1);

        /*
         * This merge will fail, because the FeatureChanges have conflicting tag changes.
         */
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeEdgesSuccess()
    {
        final CompleteEdge beforeEdge1 = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, 1L,
                null, null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteEdge(123L, PolyLine.TEST_POLYLINE_2, null, 2L, null, null),
                beforeEdge1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteEdge(123L, PolyLine.TEST_POLYLINE_2, null, null, null, null),
                beforeEdge1);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * Check that the polyline change and the start node change merged properly.
         */
        Assert.assertEquals(PolyLine.TEST_POLYLINE_2, ((Edge) merged.getAfterView()).asPolyLine());

        Assert.assertEquals(2L, ((Edge) merged.getAfterView()).start().getIdentifier());
    }

    @Test
    public void testMergeEdgesWithNonConflictingChangedFields()
    {
        final CompleteEdge beforeEdge1 = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, 1L,
                null, null);
        final CompleteEdge beforeEdge2 = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null,
                2L, null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, 3L, null, null), beforeEdge1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, 4L, null), beforeEdge2);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * We check that the start and end node changes were merged properly.
         */
        Assert.assertEquals(3L, ((Edge) merged.getAfterView()).start().getIdentifier());

        Assert.assertEquals(4L, ((Edge) merged.getAfterView()).end().getIdentifier());

        // Test that the beforeView was merged properly
        Assert.assertEquals(1L, ((Edge) merged.getBeforeView()).start().getIdentifier());

        Assert.assertEquals(2L, ((Edge) merged.getBeforeView()).end().getIdentifier());
    }

    @Test
    public void testMergeFailMismatchChangeType()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY, null, null), null);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new CompleteArea(123L, Polygon.CENTER, null, null), null);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeFailMismatchIdentifier()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY, null, null), null);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(456L, Polygon.SILICON_VALLEY, null, null), null);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeFailMismatchItemType()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.REMOVE,
                new CompleteArea(123L, Polygon.SILICON_VALLEY, null, null), null);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new CompletePoint(123L, Location.CENTER, null, null), null);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeLinesFail()
    {
        final CompleteLine beforeLine1 = new CompleteLine(123L, PolyLine.TEST_POLYLINE, null,
                Sets.hashSet(1L, 2L, 3L));

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteLine(123L, PolyLine.TEST_POLYLINE_2, Maps.hashMap("a", "1"),
                        Sets.hashSet(4L, 5L, 6L)),
                beforeLine1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteLine(123L, PolyLine.TEST_POLYLINE, Maps.hashMap("a", "2"),
                        Sets.hashSet(1L, 2L)),
                beforeLine1);

        /*
         * This merge will fail, because the FeatureChanges have conflicting tags.
         */
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeLinesSuccess()
    {
        final CompleteLine beforeLine1 = new CompleteLine(123L, PolyLine.TEST_POLYLINE,
                Maps.hashMap("a", "1", "b", "2"), Sets.hashSet(1L, 2L, 3L));

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteLine(123L, PolyLine.TEST_POLYLINE_2,
                        Maps.hashMap("a", "1", "b", "2", "c", "3"), Sets.hashSet(1L, 2L, 3L, 4L)),
                beforeLine1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteLine(123L, PolyLine.TEST_POLYLINE_2, Maps.hashMap("a", "1", "b", "12"),
                        Sets.hashSet(2L, 3L)),
                beforeLine1);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * Check that the geometry, tag map, and parent relations sets merged properly.
         */
        Assert.assertEquals(PolyLine.TEST_POLYLINE_2, ((Line) merged.getAfterView()).asPolyLine());

        Assert.assertEquals(Maps.hashMap("a", "1", "b", "12", "c", "3"),
                ((Line) merged.getAfterView()).getTags());

        Assert.assertEquals(Sets.hashSet(2L, 3L, 4L), ((Line) merged.getAfterView()).relations()
                .stream().map(relations -> relations.getIdentifier()).collect(Collectors.toSet()));
    }

    @Test
    public void testMergeLinesWithNonConflictingChangedFields()
    {
        final CompleteLine beforeLine1 = new CompleteLine(123L, PolyLine.TEST_POLYLINE,
                Maps.hashMap("a", "1", "b", "2"), null);
        final CompleteLine beforeLine2 = new CompleteLine(123L, PolyLine.TEST_POLYLINE, null,
                Sets.hashSet(1L, 2L, 3L));

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteLine(123L, PolyLine.TEST_POLYLINE,
                        Maps.hashMap("a", "1", "b", "2", "c", "3"), null),
                beforeLine1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteLine(123L, PolyLine.TEST_POLYLINE, null, Sets.hashSet(1L, 2L, 3L, 4L)),
                beforeLine2);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * Check that the tag map and parent relations sets merged properly.
         */

        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2", "c", "3"),
                ((Line) merged.getAfterView()).getTags());

        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L, 4L), ((Line) merged.getAfterView()).relations()
                .stream().map(relations -> relations.getIdentifier()).collect(Collectors.toSet()));

        // Test that the beforeView was merged properly
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2"),
                ((Line) merged.getBeforeView()).getTags());
        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L), ((Line) merged.getBeforeView()).relations()
                .stream().map(relations -> relations.getIdentifier()).collect(Collectors.toSet()));
    }

    @Test
    public void testMergeNodesFail()
    {
        final CompleteNode beforeNode1 = new CompleteNode(123L, Location.COLOSSEUM,
                Maps.hashMap("a", "1", "b", "2"), Sets.treeSet(1L, 2L, 3L),
                Sets.treeSet(10L, 11L, 12L), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteNode(123L, Location.COLOSSEUM, Maps.hashMap("a", "1", "c", "3"),
                        Sets.treeSet(1L, 2L, 3L, 4L), Sets.treeSet(10L, 11L, 12L, 13L), null),
                beforeNode1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteNode(123L, Location.COLOSSEUM,
                        Maps.hashMap("a", "1", "b", "3", "c", "3"), Sets.treeSet(1L, 2L, 3L, 5L),
                        Sets.treeSet(10L, 11L), null),
                beforeNode1);

        /*
         * This merge will fail, because featureChange1 removes tag [b=2], while featureChange2
         * modifies it to [b=3]. This generates an ADD/REMOVE collision.
         */
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeNodesSuccess()
    {
        final CompleteNode beforeNode1 = new CompleteNode(123L, Location.COLOSSEUM,
                Maps.hashMap("a", "1", "b", "2"), Sets.treeSet(1L, 2L, 3L),
                Sets.treeSet(10L, 11L, 12L), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteNode(123L, Location.EIFFEL_TOWER, Maps.hashMap("a", "1", "c", "3"),
                        Sets.treeSet(1L, 2L, 3L, 4L), Sets.treeSet(10L, 11L, 12L, 13L), null),
                beforeNode1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteNode(123L, Location.EIFFEL_TOWER,
                        Maps.hashMap("a", "1", "b", "2", "c", "3"), Sets.treeSet(1L, 2L, 3L, 5L),
                        Sets.treeSet(10L, 11L), null),
                beforeNode1);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * Here, we merged [a=1,c=3] and [a=1,b=2,c=3] given beforeView [a=1,b=2]. We can safely
         * remove [b=2] from the merged result, since one side did not modify and one side removed.
         * We can safely add [c=3] since one side added, and the other did not remove or add a
         * conflicting value for 'c'.
         */
        Assert.assertEquals(Maps.hashMap("a", "1", "c", "3"),
                ((Node) merged.getAfterView()).getTags());

        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L, 4L, 5L), ((Node) merged.getAfterView())
                .inEdges().stream().map(edge -> edge.getIdentifier()).collect(Collectors.toSet()));

        Assert.assertEquals(Sets.hashSet(10L, 11L, 13L), ((Node) merged.getAfterView()).outEdges()
                .stream().map(edge -> edge.getIdentifier()).collect(Collectors.toSet()));

        Assert.assertEquals(Location.EIFFEL_TOWER, ((Node) merged.getAfterView()).getLocation());
    }

    @Test
    public void testMergeNodesWithNonConflictingChangedFields()
    {
        final CompleteNode beforeNode1 = new CompleteNode(123L, Location.COLOSSEUM,
                Maps.hashMap("a", "1", "b", "2"), null, null, null);
        final CompleteNode beforeNode2 = new CompleteNode(123L, Location.COLOSSEUM, null,
                Sets.treeSet(1L, 2L, 3L), null, null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteNode(123L, Location.COLOSSEUM, Maps.hashMap("a", "1", "b", "12"), null,
                        null, null),
                beforeNode1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteNode(123L, Location.COLOSSEUM, null, Sets.treeSet(1L, 2L, 3L, 4L), null,
                        null),
                beforeNode2);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * We check that the modify tag ADD [b=12] gets merged properly. Also, we check that the
         * inEdges set merged properly.
         */
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "12"),
                ((Node) merged.getAfterView()).getTags());

        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L, 4L), ((Node) merged.getAfterView()).inEdges()
                .stream().map(edge -> edge.getIdentifier()).collect(Collectors.toSet()));

        // Test that the beforeView was merged properly
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2"),
                ((Node) merged.getBeforeView()).getTags());

        Assert.assertEquals(Sets.treeSet(1L, 2L, 3L), ((Node) merged.getBeforeView()).inEdges()
                .stream().map(edge -> edge.getIdentifier()).collect(Collectors.toSet()));
    }

    @Test
    public void testMergePointsFail()
    {
        final CompletePoint beforePoint1 = new CompletePoint(123L, Location.CENTER,
                Maps.hashMap("a", "1", "b", "2"), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.COLOSSEUM, Maps.hashMap("a", "1"), null),
                beforePoint1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.EIFFEL_TOWER,
                        Maps.hashMap("a", "1", "b", "2", "c", "3"), null),
                beforePoint1);

        /*
         * This merge will fail, because featureChange1 and featureChange2 have conflicting changed
         * locations.
         */
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergePointsSuccess()
    {
        final CompletePoint beforePoint1 = new CompletePoint(123L, Location.COLOSSEUM,
                Maps.hashMap("a", "1", "b", "2"), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, new CompletePoint(
                123L, Location.EIFFEL_TOWER, Maps.hashMap("a", "1", "b", "2"), null), beforePoint1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.EIFFEL_TOWER,
                        Maps.hashMap("a", "1", "b", "2", "c", "3"), null),
                beforePoint1);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * We check that the simple tag ADD [c=3] gets merged properly. Also, we check that the
         * location updated properly.
         */
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2", "c", "3"),
                ((Point) merged.getAfterView()).getTags());

        Assert.assertEquals(Location.EIFFEL_TOWER, ((Point) merged.getAfterView()).getLocation());
    }

    @Test
    public void testMergePointsWithNonConflictingChangedFields()
    {
        final CompletePoint beforePoint1 = new CompletePoint(123L, Location.COLOSSEUM, null,
                Sets.hashSet(1L, 2L, 3L));
        final CompletePoint beforePoint2 = new CompletePoint(123L, Location.COLOSSEUM,
                Maps.hashMap("a", "1", "b", "2"), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.COLOSSEUM, null, Sets.hashSet(1L, 2L, 3L, 4L)),
                beforePoint1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.COLOSSEUM,
                        Maps.hashMap("a", "1", "b", "2", "c", "3"), null),
                beforePoint2);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * We check that the simple tag ADD [c=3] gets merged properly. Also, we check that the
         * parent relations set is merged.
         */
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2", "c", "3"),
                ((Point) merged.getAfterView()).getTags());

        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L, 4L),
                ((Point) merged.getAfterView()).relations().stream()
                        .map(relation -> relation.getIdentifier()).collect(Collectors.toSet()));

        // Test that the beforeView was merged properly
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2"),
                ((Point) merged.getBeforeView()).getTags());

        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L), ((Point) merged.getBeforeView()).relations()
                .stream().map(relation -> relation.getIdentifier()).collect(Collectors.toSet()));
    }

    @Test
    public void testMergeRelationsFail()
    {
        final RelationBean beforeMemberBean = new RelationBean();
        beforeMemberBean.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        beforeMemberBean.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        beforeMemberBean.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        final CompleteRelation beforeRelation = new CompleteRelation(123L,
                Maps.hashMap("a", "1", "b", "2"), Rectangle.TEST_RECTANGLE, beforeMemberBean,
                Arrays.asList(10L, 11L, 12L), null, 123456L, Sets.hashSet(1L, 2L, 3L));

        final RelationBean afterMemberBean1 = new RelationBean();
        afterMemberBean1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterMemberBean1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        afterMemberBean1.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterMemberBean1.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        afterMemberBean1.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteRelation(123L, Maps.hashMap("a", "1", "b", "2", "c", "3"),
                        Rectangle.TEST_RECTANGLE, afterMemberBean1,
                        Arrays.asList(10L, 11L, 12L, 13L), null, 1234567L,
                        Sets.hashSet(1L, 2L, 3L, 4L)),
                beforeRelation);

        final RelationBean afterMemberBean2 = new RelationBean();
        afterMemberBean2.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterMemberBean2.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        afterMemberBean2.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterMemberBean2.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteRelation(123L, Maps.hashMap("b", "100"), Rectangle.TEST_RECTANGLE_2,
                        afterMemberBean2, Arrays.asList(11L, 12L), null, 1234567L,
                        Sets.hashSet(2L, 3L)),
                beforeRelation);

        /*
         * This merge will fail due to an ADD/ADD conflict in the member list bean.
         */
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");
        featureChange1.merge(featureChange2);
    }

    @Test
    public void testMergeRelationsSuccess()
    {
        final RelationBean beforeMemberBean = new RelationBean();
        beforeMemberBean.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        beforeMemberBean.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        beforeMemberBean.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        beforeMemberBean.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        final RelationBean beforeAllKnownOsmBean = new RelationBean();
        beforeAllKnownOsmBean.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        beforeAllKnownOsmBean.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        final CompleteRelation beforeRelation = new CompleteRelation(123L,
                Maps.hashMap("a", "1", "b", "2"), Rectangle.TEST_RECTANGLE, beforeMemberBean,
                Arrays.asList(10L, 11L, 12L), beforeAllKnownOsmBean, 123456L,
                Sets.hashSet(1L, 2L, 3L));

        final RelationBean afterMemberBean1 = new RelationBean();
        afterMemberBean1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterMemberBean1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        afterMemberBean1.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        final RelationBean afterAllKnownOsmBean1 = new RelationBean();
        afterAllKnownOsmBean1.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        afterAllKnownOsmBean1.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        afterAllKnownOsmBean1.addItem(new RelationBeanItem(3L, "lineRole3", ItemType.LINE));
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteRelation(123L, Maps.hashMap("a", "1", "b", "2", "c", "3"),
                        Rectangle.TEST_RECTANGLE, afterMemberBean1,
                        Arrays.asList(10L, 11L, 12L, 13L), afterAllKnownOsmBean1, 123456L,
                        Sets.hashSet(1L, 2L, 3L, 4L)),
                beforeRelation);

        final RelationBean afterMemberBean2 = new RelationBean();
        afterMemberBean2.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterMemberBean2.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        afterMemberBean2.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterMemberBean2.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        afterMemberBean2.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        final RelationBean afterAllKnownOsmBean2 = new RelationBean();
        afterAllKnownOsmBean2.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteRelation(123L, Maps.hashMap("b", "100"), Rectangle.TEST_RECTANGLE_2,
                        afterMemberBean2, Arrays.asList(11L, 12L), afterAllKnownOsmBean2, 1234567L,
                        Sets.hashSet(2L, 3L)),
                beforeRelation);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * Check that all the relation fields merged correctly.
         */
        Assert.assertEquals(Maps.hashMap("b", "100", "c", "3"),
                ((Relation) merged.getAfterView()).getTags());

        final RelationBean goldenMergedMemberBean = new RelationBean();
        goldenMergedMemberBean.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        goldenMergedMemberBean.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        goldenMergedMemberBean.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        goldenMergedMemberBean.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        Assert.assertEquals(goldenMergedMemberBean,
                ((Relation) merged.getAfterView()).members().asBean());

        Assert.assertEquals(
                Rectangle.forLocated(Rectangle.TEST_RECTANGLE, Rectangle.TEST_RECTANGLE_2),
                ((Relation) merged.getAfterView()).bounds());

        final RelationBean goldenMergedOsmBean = new RelationBean();
        goldenMergedOsmBean.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        goldenMergedOsmBean.addItem(new RelationBeanItem(3L, "lineRole3", ItemType.LINE));
        Assert.assertEquals(goldenMergedOsmBean,
                ((Relation) merged.getAfterView()).allKnownOsmMembers().asBean());

        Assert.assertEquals(Sets.hashSet(11L, 12L, 13L),
                ((Relation) merged.getAfterView()).allRelationsWithSameOsmIdentifier().stream()
                        .map(Relation::getIdentifier).collect(Collectors.toSet()));

        Assert.assertEquals(new Long(1234567L),
                ((Relation) merged.getAfterView()).osmRelationIdentifier());

        Assert.assertEquals(Sets.hashSet(2L, 3L, 4L), ((Relation) merged.getAfterView()).relations()
                .stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    @Test
    public void testMergeRelationsWithNonConflictingChangeFields()
    {
        final RelationBean beforeMemberBean1 = new RelationBean();
        beforeMemberBean1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        beforeMemberBean1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        beforeMemberBean1.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        beforeMemberBean1.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        final CompleteRelation beforeRelation1 = new CompleteRelation(123L,
                Maps.hashMap("a", "1", "b", "2", "c", "3"), Rectangle.TEST_RECTANGLE,
                beforeMemberBean1, Arrays.asList(10L, 11L, 12L), null, null,
                Sets.hashSet(1L, 2L, 3L));

        final RelationBean beforeAllKnownOsmBean2 = new RelationBean();
        beforeAllKnownOsmBean2.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        beforeAllKnownOsmBean2.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        final CompleteRelation beforeRelation2 = new CompleteRelation(123L, null,
                Rectangle.TEST_RECTANGLE, null, null, beforeAllKnownOsmBean2, 123456L, null);

        final RelationBean afterMemberBean1 = new RelationBean();
        afterMemberBean1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterMemberBean1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        afterMemberBean1.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteRelation(123L, Maps.hashMap("a", "1", "b", "2"),
                        Rectangle.TEST_RECTANGLE, afterMemberBean1,
                        Arrays.asList(10L, 11L, 12L, 13L), null, null, Sets.hashSet(1L, 2L)),
                beforeRelation1);

        final RelationBean afterAllKnownOsmBean2 = new RelationBean();
        afterAllKnownOsmBean2.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteRelation(123L, null, Rectangle.TEST_RECTANGLE, null, null,
                        afterAllKnownOsmBean2, 1234567L, null),
                beforeRelation2);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        /*
         * Check that the before and after views merged correctly.
         */
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2"),
                ((Relation) merged.getAfterView()).getTags());

        final RelationBean goldenMergedMemberBean = new RelationBean();
        goldenMergedMemberBean.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        goldenMergedMemberBean.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        goldenMergedMemberBean.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        Assert.assertEquals(goldenMergedMemberBean,
                ((Relation) merged.getAfterView()).members().asBean());

        Assert.assertEquals(Sets.hashSet(10L, 11L, 12L, 13L),
                ((Relation) merged.getAfterView()).allRelationsWithSameOsmIdentifier().stream()
                        .map(Relation::getIdentifier).collect(Collectors.toSet()));

        final RelationBean goldenMergedOsmBean = new RelationBean();
        goldenMergedOsmBean.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        Assert.assertEquals(goldenMergedOsmBean,
                ((Relation) merged.getAfterView()).allKnownOsmMembers().asBean());

        Assert.assertEquals(new Long(1234567L),
                ((Relation) merged.getAfterView()).osmRelationIdentifier());

        Assert.assertEquals(Sets.hashSet(1L, 2L), ((Relation) merged.getAfterView()).relations()
                .stream().map(Relation::getIdentifier).collect(Collectors.toSet()));

        // Check merged before view
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2", "c", "3"),
                ((Relation) merged.getBeforeView()).getTags());

        Assert.assertEquals(beforeMemberBean1,
                ((Relation) merged.getBeforeView()).members().asBean());

        Assert.assertEquals(Sets.hashSet(10L, 11L, 12L),
                ((Relation) merged.getBeforeView()).allRelationsWithSameOsmIdentifier().stream()
                        .map(Relation::getIdentifier).collect(Collectors.toSet()));

        Assert.assertEquals(beforeAllKnownOsmBean2,
                ((Relation) merged.getBeforeView()).allKnownOsmMembers().asBean());

        Assert.assertEquals(new Long(123456L),
                ((Relation) merged.getBeforeView()).osmRelationIdentifier());

        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L), ((Relation) merged.getBeforeView())
                .relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }
}
