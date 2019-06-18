package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.diff.AtlasDiff;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * There are 2 edges AB and BC. Node B is common between the 2 edges.
 *
 * @author Yazad Khambata
 */
public class MultiCascadeDeleteTest
{
    private static final Logger log = LoggerFactory.getLogger(MultiCascadeDeleteTest.class);

    @Rule
    public final MultiCascadeDeleteTestRule rule = new MultiCascadeDeleteTestRule();

    @Test
    public void deleteEdgeAB()
    {
        final Atlas atlas = originalAtlas();

        // Step-1: Delete edgeAB
        final ItemType itemType = ItemType.EDGE;
        final Long entityIdToDelete = MultiCascadeDeleteTestRule.edgeAB;
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
        final Relation relation = changeAtlas.relation(MultiCascadeDeleteTestRule.relationX);
        Assert.assertNotNull(relation);
        Assert.assertEquals(1, relation.membersOfType(ItemType.EDGE).size());
        Assert.assertEquals(1, relation.membersOfType(ItemType.NODE).size());

        // Step-3 Verify AtlasDiff
        final Map<AtlasEntityKey, Boolean> expectedChangedAndDeleted = new HashMap<AtlasEntityKey, Boolean>()
        {
            private static final long serialVersionUID = 454060048188157314L;

            {
                put(AtlasEntityKey.from(ItemType.NODE, MultiCascadeDeleteTestRule.nodeA), false);
                put(AtlasEntityKey.from(ItemType.NODE, MultiCascadeDeleteTestRule.nodeB), false);
                put(AtlasEntityKey.from(ItemType.EDGE, MultiCascadeDeleteTestRule.edgeAB), true);
                put(AtlasEntityKey.from(ItemType.RELATION, MultiCascadeDeleteTestRule.relationX),
                        false);
            }
        };

        verifyAtlasDiff(atlas, changeAtlas, expectedChangedAndDeleted);
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
        final Relation relation = changeAtlas.relation(MultiCascadeDeleteTestRule.relationX);
        Assert.assertNotNull(relation);
        Assert.assertEquals(1, relation.membersOfType(ItemType.EDGE).size());
        Assert.assertEquals(1, relation.membersOfType(ItemType.NODE).size());

        // Step-3 Verify AtlasDiff
        final Map<AtlasEntityKey, Boolean> expectedChangedAndDeleted = new HashMap<AtlasEntityKey, Boolean>()
        {
            private static final long serialVersionUID = 3346327267408295085L;

            {
                put(AtlasEntityKey.from(ItemType.NODE, MultiCascadeDeleteTestRule.nodeA), true);
                put(AtlasEntityKey.from(ItemType.NODE, MultiCascadeDeleteTestRule.nodeB), false);
                put(AtlasEntityKey.from(ItemType.EDGE, MultiCascadeDeleteTestRule.edgeAB), true);
                put(AtlasEntityKey.from(ItemType.RELATION, MultiCascadeDeleteTestRule.relationX),
                        false);
            }
        };

        verifyAtlasDiff(atlas, changeAtlas, expectedChangedAndDeleted);
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
        final Relation relation = changeAtlas.relation(MultiCascadeDeleteTestRule.relationX);
        Assert.assertNull(relation);

        // Step-3 Verify AtlasDiff
        final Map<AtlasEntityKey, Boolean> expectedChangedAndDeleted = new HashMap<AtlasEntityKey, Boolean>()
        {
            private static final long serialVersionUID = -8963613354545135623L;

            {
                put(AtlasEntityKey.from(ItemType.NODE, MultiCascadeDeleteTestRule.nodeA), false);
                put(AtlasEntityKey.from(ItemType.NODE, MultiCascadeDeleteTestRule.nodeB), true);
                put(AtlasEntityKey.from(ItemType.NODE, MultiCascadeDeleteTestRule.nodeC), false);
                put(AtlasEntityKey.from(ItemType.EDGE, MultiCascadeDeleteTestRule.edgeAB), true);
                put(AtlasEntityKey.from(ItemType.EDGE, MultiCascadeDeleteTestRule.edgeBC), true);
                put(AtlasEntityKey.from(ItemType.RELATION, MultiCascadeDeleteTestRule.relationX),
                        true);
            }
        };

        verifyAtlasDiff(atlas, changeAtlas, expectedChangedAndDeleted);
    }

    private Atlas changeAtlasDeletingFeature(final Atlas atlas, final ItemType itemType,
            final Long entityIdToDelete, final int expectedNodes, final int expectedEdges)
    {
        final FeatureChange featureChange = createDeleteFeatureChange(atlas, itemType,
                entityIdToDelete);
        return changedAtlas(atlas, featureChange, expectedNodes, expectedEdges);
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

    private FeatureChange createDeleteFeatureChange(final Atlas atlas, final ItemType itemType,
            final Long entityIdToDelete)
    {
        return FeatureChange.remove(CompleteItemType
                .shallowFrom(itemType.entityForIdentifier(atlas, entityIdToDelete)));
    }

    private Atlas originalAtlas()
    {
        final Atlas atlas = this.rule.getAtlas();
        Assert.assertEquals(3, atlas.numberOfNodes());
        Assert.assertEquals(2, atlas.numberOfEdges());
        Assert.assertEquals(1, atlas.numberOfRelations());
        return atlas;
    }

    private void verifyAtlasDiff(final Atlas originalAtlas, final Atlas changeAtlas,
            final Map<AtlasEntityKey, Boolean> expectedChangedAndDeleted)
    {
        final AtlasDiff atlasDiff = new AtlasDiff(originalAtlas, changeAtlas);
        final Optional<Change> optionalChangeFromDiff = atlasDiff.generateChange();
        Assert.assertTrue(optionalChangeFromDiff.isPresent());
        final Change changeFromDiff = optionalChangeFromDiff.get();

        final Map<AtlasEntityKey, FeatureChange> atlasEntityKeyFeatureChangeMap = changeFromDiff
                .allChangesMappedByAtlasEntityKey();

        atlasEntityKeyFeatureChangeMap.entrySet().stream().forEach(entry ->
        {
            log.info("{} : {}", entry.getKey(), entry.getValue());
        });

        Assert.assertEquals(expectedChangedAndDeleted.size(),
                atlasEntityKeyFeatureChangeMap.size());

        expectedChangedAndDeleted.entrySet().stream().forEach(expectedEntry ->
        {
            Assert.assertNotNull(atlasEntityKeyFeatureChangeMap.get(expectedEntry.getKey()));

            final AtlasEntity changedAtlasEntity = expectedEntry.getKey()
                    .getAtlasEntity(changeAtlas);
            Assert.assertTrue((changedAtlasEntity == null) == expectedEntry.getValue());
        });
    }
}
