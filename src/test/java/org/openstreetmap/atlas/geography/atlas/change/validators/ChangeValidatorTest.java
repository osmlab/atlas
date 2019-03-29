package org.openstreetmap.atlas.geography.atlas.change.validators;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class ChangeValidatorTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testMatchingEdgeEndNodes()
    {
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange
                .add(new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, 456L, null)));
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                null, 456L, null, null)));
        builder.get();
    }

    @Test
    public void testMatchingEdgeNodes()
    {
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange
                .add(new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, 654L, 456L, null)));
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                null, 456L, 654L, null)));
        builder.get();
    }

    @Test
    public void testMatchingEdgePolyLines()
    {
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange
                .add(new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, null, null)));
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                null, null, null, null)));
        builder.get();
    }

    @Test
    public void testMatchingEdgeStartNodes()
    {
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange
                .add(new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, 456L, null, null)));
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                null, null, 456L, null)));
        builder.get();
    }

    @Test
    public void testMatchingEdgeTags()
    {
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange.add(new CompleteEdge(123L, PolyLine.TEST_POLYLINE,
                Maps.hashMap("key1", "value1"), null, null, null)));
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                Maps.hashMap("key1", "value1"), null, null, null)));
        builder.get();
    }

    @Test
    public void testMismatchingEdgeAsymmetricParentRelations()
    {
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                null, null, null, Sets.hashSet(13L))));
        builder.get();
    }

    @Test
    public void testMismatchingEdgeEndNodes()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not match its backward edge start node");

        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange
                .add(new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, 456L, null)));
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                null, 654L, null, null)));
        builder.get();
    }

    @Test
    public void testMismatchingEdgeNodes()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not match its backward edge start node");

        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange.add(
                new CompleteEdge(123L, PolyLine.TEST_POLYLINE, Maps.hashMap(), null, 456L, null)));
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                Maps.hashMap(), null, null, null)));
        builder.get();
    }

    @Test
    public void testMismatchingEdgeParentRelations()
    {
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange.add(new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null,
                null, Sets.hashSet(12L))));
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                null, null, null, Sets.hashSet(13L))));
        builder.get();
    }

    @Test
    public void testMismatchingEdgePolyLines()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not match its backward edge polyline");

        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange
                .add(new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, null, null)));
        builder.add(FeatureChange
                .add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE, null, null, null, null)));
        builder.get();
    }

    @Test
    public void testMismatchingEdgeStartNodes()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not match its backward edge end node");

        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange
                .add(new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, 456L, null, null)));
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                null, null, 654L, null)));
        builder.get();
    }

    @Test
    public void testMismatchingEdgeTags()
    {
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange.add(new CompleteEdge(123L, PolyLine.TEST_POLYLINE,
                Maps.hashMap("key1", "value1"), null, null, null)));
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                Maps.hashMap("key2", "value2"), null, null, null)));
        builder.get();
    }

    @Test
    public void testMissingForwardEdge()
    {
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(FeatureChange.add(new CompleteEdge(-123L, PolyLine.TEST_POLYLINE.reversed(),
                Maps.hashMap(), null, null, null)));
        builder.get();
    }
}
