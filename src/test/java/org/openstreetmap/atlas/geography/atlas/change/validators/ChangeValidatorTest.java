package org.openstreetmap.atlas.geography.atlas.change.validators;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author matthieun
 */
public class ChangeValidatorTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testMismatchingEdgeNodes()
    {
        // this.expectedException.expect(CoreException.class);
        // this.expectedException.expectMessage("already has a feature change");

        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(new FeatureChange(ChangeType.ADD,
                new BloatedEdge(123L, null, null, null, 456L, null)));
        builder.add(new FeatureChange(ChangeType.ADD,
                new BloatedEdge(-123L, null, null, null, null, null)));
        builder.get();
    }

    @Test
    public void testMismatchingEdgePolyLines()
    {
        // this.expectedException.expect(CoreException.class);
        // this.expectedException.expectMessage("already has a feature change");

        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(new FeatureChange(ChangeType.ADD,
                new BloatedEdge(123L, PolyLine.TEST_POLYLINE, null, null, null, null)));
        builder.add(new FeatureChange(ChangeType.ADD,
                new BloatedEdge(-123L, PolyLine.TEST_POLYLINE, null, null, null, null)));
        builder.get();
    }

    @Test
    public void testMismatchingEdgeTags()
    {
        // this.expectedException.expect(CoreException.class);
        // this.expectedException.expectMessage("already has a feature change");

        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(new FeatureChange(ChangeType.ADD,
                new BloatedEdge(123L, null, Maps.hashMap("key1", "value1"), null, null, null)));
        builder.add(new FeatureChange(ChangeType.ADD,
                new BloatedEdge(-123L, null, Maps.hashMap("key2", "value2"), null, null, null)));
        builder.get();
    }

    @Test
    public void testMismatchingEndEdgeNodes()
    {
        // this.expectedException.expect(CoreException.class);
        // this.expectedException.expectMessage("already has a feature change");

        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(new FeatureChange(ChangeType.ADD,
                new BloatedEdge(123L, null, null, null, 456L, null)));
        builder.add(new FeatureChange(ChangeType.ADD,
                new BloatedEdge(-123L, null, null, 654L, null, null)));
        builder.get();
    }

    @Test
    public void testMismatchingStartEdgeNodes()
    {
        // this.expectedException.expect(CoreException.class);
        // this.expectedException.expectMessage("already has a feature change");

        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(new FeatureChange(ChangeType.ADD,
                new BloatedEdge(123L, null, null, 456L, null, null)));
        builder.add(new FeatureChange(ChangeType.ADD,
                new BloatedEdge(-123L, null, null, null, 654L, null)));
        builder.get();
    }
}
