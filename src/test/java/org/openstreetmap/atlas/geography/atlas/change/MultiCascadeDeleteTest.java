package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * There are 2 edges AB and BC. Node B is common between the 2 edges.
 *
 * @author Yazad Khambata
 */
public class MultiCascadeDeleteTest
{
    @Rule
    public final MultiCascadeDeleteTestRule rule = new MultiCascadeDeleteTestRule();

    @Test
    public void deleteEdgeAB()
    {
        final Atlas atlas = originalAtlas();

        // Step-1: Delete edgeAB
        final ItemType itemType = ItemType.EDGE;
        final Long entityIdToDelete = MultiCascadeDeleteTestRule.edgeA;
        final int expectedNodes = 3;
        final int expectedEdges = 1;

        final Atlas changeAtlas = changeAtlasDeletingFeature(atlas, itemType, entityIdToDelete,
                expectedNodes, expectedEdges);

        // Step-2: check if Nodes A and B in / out edges have changed.
        Assert.assertTrue(changeAtlas.node(MultiCascadeDeleteTestRule.nodeA).outEdges().isEmpty());
        Assert.assertTrue(changeAtlas.node(MultiCascadeDeleteTestRule.nodeB).inEdges().isEmpty());
        Assert.assertFalse(changeAtlas.node(MultiCascadeDeleteTestRule.nodeB).outEdges().isEmpty());
        Assert.assertFalse(changeAtlas.node(MultiCascadeDeleteTestRule.nodeC).inEdges().isEmpty());
        Assert.assertTrue(changeAtlas.node(MultiCascadeDeleteTestRule.nodeC).outEdges().isEmpty());
    }

    @Test
    public void deleteNodeA()
    {
        final Atlas atlas = originalAtlas();

        // Step-1: Delete edgeAB
        final ItemType itemType = ItemType.NODE;
        final Long entityIdToDelete = MultiCascadeDeleteTestRule.nodeA;
        final int expectedNodes = 2;
        final int expectedEdges = 1;

        final Atlas changeAtlas = changeAtlasDeletingFeature(atlas, itemType, entityIdToDelete,
                expectedNodes, expectedEdges);

        // Step-2: check if Nodes A and B in / out edges have changed.
        Assert.assertTrue(changeAtlas.node(MultiCascadeDeleteTestRule.nodeB).inEdges().isEmpty());
        Assert.assertFalse(changeAtlas.node(MultiCascadeDeleteTestRule.nodeB).outEdges().isEmpty());
        Assert.assertFalse(changeAtlas.node(MultiCascadeDeleteTestRule.nodeC).inEdges().isEmpty());
        Assert.assertTrue(changeAtlas.node(MultiCascadeDeleteTestRule.nodeC).outEdges().isEmpty());
    }

    @Test
    public void deleteNodeB()
    {
        final Atlas atlas = originalAtlas();

        // Step-1: Delete edgeAB
        final ItemType itemType = ItemType.NODE;
        final Long entityIdToDelete = MultiCascadeDeleteTestRule.nodeB;
        final int expectedNodes = 2;
        final int expectedEdges = 0;

        final Atlas changeAtlas = changeAtlasDeletingFeature(atlas, itemType, entityIdToDelete,
                expectedNodes, expectedEdges);

        // Step-2: check if Nodes A and B in / out edges have changed.
        Assert.assertTrue(changeAtlas.node(MultiCascadeDeleteTestRule.nodeA).inEdges().isEmpty());
        Assert.assertTrue(changeAtlas.node(MultiCascadeDeleteTestRule.nodeA).outEdges().isEmpty());
        Assert.assertTrue(changeAtlas.node(MultiCascadeDeleteTestRule.nodeC).inEdges().isEmpty());
        Assert.assertTrue(changeAtlas.node(MultiCascadeDeleteTestRule.nodeC).outEdges().isEmpty());
    }

    private Atlas changeAtlasDeletingFeature(final Atlas atlas, final ItemType itemType,
            final Long entityIdToDelete, final int expectedNodes, final int expectedEdges)
    {
        final FeatureChange featureChange = createDeleteFeatureChange(atlas, itemType,
                entityIdToDelete);
        return changedAtlas(atlas, featureChange, expectedNodes, expectedEdges);
    }

    private FeatureChange createDeleteFeatureChange(final Atlas atlas, final ItemType itemType,
            final Long entityIdToDelete)
    {
        return FeatureChange.remove(CompleteItemType
                .shallowFrom(itemType.entityForIdentifier(atlas, entityIdToDelete)));
    }

    private Atlas changedAtlas(final Atlas atlas, final FeatureChange featureChange,
            final long expectedNodes, final long expectedEdges)
    {
        final Change change = ChangeBuilder.newInstance().add(featureChange).get();
        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(expectedNodes, changeAtlas.numberOfNodes());
        Assert.assertEquals(expectedEdges, changeAtlas.numberOfEdges());
        return changeAtlas;
    }

    private Atlas originalAtlas()
    {
        final Atlas atlas = rule.getAtlas();
        Assert.assertEquals(3, atlas.numberOfNodes());
        Assert.assertEquals(2, atlas.numberOfEdges());
        return atlas;
    }
}
