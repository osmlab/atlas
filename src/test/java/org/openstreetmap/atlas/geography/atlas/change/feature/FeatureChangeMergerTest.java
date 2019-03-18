package org.openstreetmap.atlas.geography.atlas.change.feature;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
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
        final CompleteArea beforeArea1 = new CompleteArea(123L, Polygon.SILICON_VALLEY,
                Maps.hashMap("a", "1", "b", "2"), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY_2, null, null), beforeArea1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.SILICON_VALLEY, Maps.hashMap("a", "1"), null),
                beforeArea1);

        /*
         * This merge will fail, because the FeatureChanges have conflicting polygons. There is no
         * way to resolve conflicting geometry during a merge.
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
        final CompleteEdge beforeEdge1 = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, 1L,
                null, null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, 2L, null, null), beforeEdge1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, 3L, null, null), beforeEdge1);

        /*
         * This merge will fail, because the FeatureChanges have different start node identifiers.
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
    public void testMergeLinesFail()
    {
        final CompleteLine beforeLine1 = new CompleteLine(123L, PolyLine.TEST_POLYLINE, null,
                Sets.hashSet(1L, 2L, 3L));

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteLine(123L, PolyLine.TEST_POLYLINE_2, null, Sets.hashSet(4L, 5L, 6L)),
                beforeLine1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteLine(123L, PolyLine.TEST_POLYLINE, null, Sets.hashSet(1L, 2L)),
                beforeLine1);

        /*
         * This merge will fail, because the FeatureChanges have conflicting geometry.
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
        final CompletePoint beforePoint1 = new CompletePoint(123L, Location.COLOSSEUM,
                Maps.hashMap("a", "1", "b", "2"), null);

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.COLOSSEUM, Maps.hashMap("a", "1", "b", "2"), null),
                beforePoint1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompletePoint(123L, Location.EIFFEL_TOWER,
                        Maps.hashMap("a", "1", "b", "2", "c", "3"), null),
                beforePoint1);

        /*
         * This merge will fail, because featureChange1 and featureChange2 have conflicting
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
}
