package org.openstreetmap.atlas.geography.atlas.change.merge;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.items.Node;
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
                new CompleteNode(123L, Location.COLOSSEUM, Maps.hashMap("a", "1", "c", "3"),
                        Sets.treeSet(1L, 2L, 3L, 4L), Sets.treeSet(10L, 11L, 12L, 13L), null),
                beforeNode1);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteNode(123L, Location.COLOSSEUM,
                        Maps.hashMap("a", "1", "b", "2", "c", "3"), Sets.treeSet(1L, 2L, 3L, 5L),
                        Sets.treeSet(10L, 11L), null),
                beforeNode1);

        final FeatureChange merged = featureChange1.merge(featureChange2);
        Assert.assertEquals(Maps.hashMap("a", "1", "c", "3"),
                ((Node) merged.getAfterView()).getTags());
        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L, 4L, 5L), ((Node) merged.getAfterView())
                .inEdges().stream().map(edge -> edge.getIdentifier()).collect(Collectors.toSet()));
        Assert.assertEquals(Sets.hashSet(10L, 11L, 13L), ((Node) merged.getAfterView()).outEdges()
                .stream().map(edge -> edge.getIdentifier()).collect(Collectors.toSet()));
    }

    @Test
    public void testMergeNodesUnrelated()
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
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "12"),
                ((Node) merged.getAfterView()).getTags());
        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L, 4L), ((Node) merged.getAfterView()).inEdges()
                .stream().map(edge -> edge.getIdentifier()).collect(Collectors.toSet()));
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2"),
                ((Node) merged.getBeforeView()).getTags());
        // Test that the beforeView was merged properly
        Assert.assertEquals(Sets.treeSet(1L, 2L, 3L), ((Node) merged.getBeforeView()).inEdges()
                .stream().map(edge -> edge.getIdentifier()).collect(Collectors.toSet()));
    }
}
