package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ConnectedEdgeType;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;

/**
 * @author Yazad Khambata
 */
public class CascadeDeleteTest
{
    @Rule
    public final CascadeDeleteTestRule rule = new CascadeDeleteTestRule();

    private static <E extends AtlasEntity> long count(final Iterable<E> entities)
    {
        return StreamSupport.stream(entities.spliterator(), false).count();
    }

    private boolean isEntityPresentInRelation(final Atlas atlas, final ItemType itemType,
                                              final long entityRelationIdentifier, final long entityIdentifier)
    {
        final Relation relation = atlas.relation(entityRelationIdentifier);

        return relation.members().stream()
                .filter(relationMember -> relationMember.getEntity().getType() == itemType)
                .filter(relationMember -> relationMember.getEntity()
                        .getIdentifier() == entityIdentifier)
                .findFirst().isPresent();
    }

    @Test
    public void deleteUnrelatedNode() {
        final Atlas atlas = getAtlas();
        final long nodeIdToDelete = CascadeDeleteTestRule.NON_EDGE_NODE_IDENTIFIER;
        final long nodeRelationId = CascadeDeleteTestRule.NON_EDGE_NODE_RELATION_IDENTIFIER;

        final Atlas changeAtlas = deleteNode(atlas, nodeIdToDelete, nodeRelationId);

        verifyCounts(changeAtlas, override(ItemType.NODE, -1));
    }

    @Test
    public void deleteEdgeStartNode() {
        final Atlas atlas = getAtlas();
        final long nodeIdToDelete = CascadeDeleteTestRule.START_END_EDGE_NODE;
        final long nodeRelationId = CascadeDeleteTestRule.NON_EDGE_NODE_RELATION_IDENTIFIER;

        final Atlas changeAtlas = deleteNode(atlas, nodeIdToDelete, nodeRelationId);

        verifyCounts(changeAtlas, override(ItemType.NODE,-1), override(ItemType.EDGE, -2));
    }

    private Atlas deleteNode(final Atlas atlas, final long nodeIdToDelete, final long relationId) {
        verifyCounts(atlas);
        final Node node = atlas.node(nodeIdToDelete);
        Assert.assertNotNull(node);

        final Relation relation = atlas.relation(relationId);
        Assert.assertNotNull(relation);
        Assert.assertFalse(relation.membersOfType(ItemType.NODE).isEmpty());
        Assert.assertTrue(isNodeMemberPresent(atlas, nodeIdToDelete, relationId));

        final FeatureChange featureChangeRemoveNode = FeatureChange
                .remove(CompleteEntity.shallowFrom(node), atlas);
        final Change change = ChangeBuilder.newInstance().add(featureChangeRemoveNode).get();
        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertNull(changeAtlas.node(nodeIdToDelete));
        Assert.assertFalse(isNodeMemberPresent(changeAtlas, nodeIdToDelete, relationId));
        return changeAtlas;
    }

    private boolean isNodeMemberPresent(final Atlas atlas, final long nodeIdToDelete, final long relationId) {
        final Predicate<RelationMember> relationMemberPredicate =
                member -> member.getEntity().getType() == ItemType.NODE && member.getEntity().getIdentifier() == nodeIdToDelete;

        return !atlas.relation(relationId)
                .membersMatching(relationMemberPredicate)
                .isEmpty();
    }

    @Test
    public void testDeleteForwardEdgeToRelationMemberCascadeButNotReverseEdge()
    {
        final long edgeIdentifier = CascadeDeleteTestRule.EDGE_IDENTIFIER;
        deleteEdgeButNotReverse(edgeIdentifier, CascadeDeleteTestRule.EDGE_RELATION_IDENTIFIER);
    }

    @Test
    public void testDeleteReverseEdgeToRelationMemberCascadeButNotForwardEdge()
    {
        final long edgeIdentifier = -(CascadeDeleteTestRule.EDGE_IDENTIFIER);
        deleteEdgeButNotReverse(edgeIdentifier, CascadeDeleteTestRule.EDGE_RELATION_IDENTIFIER);
    }

    private void deleteEdgeButNotReverse(final long edgeIdentifier, final long relationIdentifier)
    {
        final long reverseIdentifier = -edgeIdentifier;

        final Atlas atlas = getAtlas();
        final long countBefore = count(atlas.edges());
        Assert.assertTrue(countBefore > 2);

        final Edge edge = atlas.edge(edgeIdentifier);
        Assert.assertNotNull(edge);
        final Edge negativeEdge = atlas.edge(reverseIdentifier);
        Assert.assertNotNull(negativeEdge);

        final Node startForward = edge.start();
        final Node endForward = edge.end();

        final Node startReverse = negativeEdge.start();
        final Node endReverse = negativeEdge.end();

        Assert.assertEquals(startForward, endReverse);
        Assert.assertEquals(endForward, startReverse);

        Assert.assertTrue(isEdgeRelatedToNode(startForward, edgeIdentifier, ConnectedEdgeType.OUT));
        Assert.assertTrue(isEdgeRelatedToNode(endForward, edgeIdentifier, ConnectedEdgeType.IN));
        Assert.assertTrue(isEdgeRelatedToNode(endReverse, edgeIdentifier, ConnectedEdgeType.OUT));
        Assert.assertTrue(isEdgeRelatedToNode(startReverse, edgeIdentifier, ConnectedEdgeType.IN));

        final Set<Long> edgeIdentifiers = getMatchingEdgesInRelation(atlas, relationIdentifier,
                edgeIdentifier);

        Assert.assertEquals(edgeIdentifiers.size(), 2);
        Assert.assertTrue(edgeIdentifiers.contains(edgeIdentifier));
        Assert.assertTrue(edgeIdentifiers.contains(reverseIdentifier));

        Assert.assertNotNull(atlas.node(startForward.getIdentifier()));
        Assert.assertNotNull(atlas.node(endForward.getIdentifier()));

        verifyCounts(atlas);

        final FeatureChange removeFeatureChange = FeatureChange
                .remove(CompleteEdge.shallowFrom(edge), atlas);

        final Change change = ChangeBuilder.newInstance().add(removeFeatureChange).get();

        final Atlas changedAtlas = new ChangeAtlas(atlas, change);

        Assert.assertNull(changedAtlas.edge(edgeIdentifier));
        Assert.assertNotNull(changedAtlas.edge(reverseIdentifier));

        final long countAfter = count(changedAtlas.edges());

        Assert.assertEquals(countBefore - 1, countAfter);

        Assert.assertNotNull(changedAtlas.node(startForward.getIdentifier()));
        Assert.assertNotNull(changedAtlas.node(endForward.getIdentifier()));

        final Set<Long> edgeIdentifiersAfterDelete = getMatchingEdgesInRelation(changedAtlas,
                relationIdentifier, edgeIdentifier);

        Assert.assertEquals(edgeIdentifiersAfterDelete.size(), 1);
        Assert.assertFalse(edgeIdentifiersAfterDelete.contains(edgeIdentifier));
        Assert.assertTrue(edgeIdentifiersAfterDelete.contains(reverseIdentifier));

        Assert.assertFalse(isEdgeRelatedToNode(changedAtlas, startForward.getIdentifier(), edgeIdentifier, ConnectedEdgeType.OUT));
        Assert.assertFalse(isEdgeRelatedToNode(changedAtlas, endForward.getIdentifier(), edgeIdentifier, ConnectedEdgeType.IN));
        Assert.assertFalse(isEdgeRelatedToNode(changedAtlas, endReverse.getIdentifier(), edgeIdentifier, ConnectedEdgeType.OUT));
        Assert.assertFalse(isEdgeRelatedToNode(changedAtlas, startReverse.getIdentifier(), edgeIdentifier, ConnectedEdgeType.IN));

        verifyCounts(changedAtlas, Pair.of(ItemType.EDGE, CascadeDeleteTestRule.EDGE_COUNT - 1));
    }

    private boolean isEdgeRelatedToNode(final Atlas atlas, final long nodeId, final long edgeId, final ConnectedEdgeType connectedEdgeType) {
        final Node node = atlas.node(nodeId);
        Assert.assertNotNull(node);
        return isEdgeRelatedToNode(node, edgeId, connectedEdgeType);
    }

    private boolean isEdgeRelatedToNode(final Node node, final long edgeId, final ConnectedEdgeType connectedEdgeType) {
        return node.connectedEdges(connectedEdgeType).stream()
                .filter(edge -> edge.getIdentifier() == edgeId)
                .findFirst()
                .isPresent();
    }

    private Atlas getAtlas()
    {
        return rule.getAtlas();
    }

    private Set<Long> getMatchingEdgesInRelation(final Atlas atlas, final long relationIdentifier,
                                                 final long edgeIdentifier)
    {
        return atlas.relation(relationIdentifier).members().stream()
                .filter(relationMember -> relationMember.getEntity().getType() == ItemType.EDGE)
                .filter(relationMember -> Math.abs(
                        relationMember.getEntity().getIdentifier()) == Math.abs(edgeIdentifier))
                .map(relationMember -> relationMember.getEntity().getIdentifier())
                .collect(Collectors.toSet());
    }

    @Test
    public void testDeleteLineToRelationMemberCascade()
    {
        final ItemType itemType = ItemType.LINE;
        final long entityIdentifier = CascadeDeleteTestRule.LINE_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.LINE_RELATION_IDENTIFIER;

        testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }

    @Test
    public void testDeleteAreaToRelationMemberCascade()
    {
        final ItemType itemType = ItemType.AREA;
        final long entityIdentifier = CascadeDeleteTestRule.AREA_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.AREA_RELATION_IDENTIFIER;

        testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }

    @Test
    public void testDeletePointToRelationMemberCascade()
    {
        final ItemType itemType = ItemType.POINT;
        final long entityIdentifier = CascadeDeleteTestRule.POINT_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.POINT_RELATION_IDENTIFIER;

        testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }

    private void testDeleteSimple(final ItemType itemType, final long entityIdentifier,
                                  final long entityRelationIdentifier)
    {
        final Atlas atlas = getAtlas();

        final AtlasEntity atlasEntity = itemType.entityForIdentifier(atlas, entityIdentifier);
        Assert.assertNotNull(atlasEntity);

        final Relation relation = atlas.relation(entityRelationIdentifier);
        Assert.assertNotNull(relation);
        Assert.assertTrue(isEntityPresentInRelation(atlas, itemType, entityRelationIdentifier,
                entityIdentifier));

        verifyCounts(atlas);

        final CompleteItemType completeItemType = CompleteItemType.from(itemType);
        final FeatureChange featureChange = FeatureChange
                .remove(completeItemType.completeEntityShallowFrom(atlasEntity), atlas);
        final Change change = ChangeBuilder.newInstance().add(featureChange).get();

        final ChangeAtlas changeAtlas = new ChangeAtlas(atlas, change);

        Assert.assertNull(itemType.entityForIdentifier(changeAtlas, entityIdentifier));
        Assert.assertFalse(isEntityPresentInRelation(changeAtlas, itemType,
                entityRelationIdentifier, entityIdentifier));

        verifyCounts(changeAtlas, override(itemType, -1));
    }

    private Pair<ItemType, Long> override(final ItemType itemType, final int override) {
        return Pair.of(itemType, rule.getCountExpectationMapping().get(itemType) + override);
    }

    private void verifyCounts(final Atlas atlas, final Pair<ItemType, Long>... overrides)
    {
        final Map<ItemType, Long> countExpectationMapping = rule.getCountExpectationMapping();

        final Map<ItemType, Long> overrideMapping = Arrays.stream(overrides)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        countExpectationMapping.putAll(overrideMapping);

        Assert.assertEquals(ItemType.values().length, countExpectationMapping.size());

        for (final Map.Entry<ItemType, Long> entry : countExpectationMapping.entrySet())
        {
            Assert.assertEquals("entry failed: " + entry, (long) entry.getValue(),
                    entry.getKey().numberOfEntities(atlas));
        }
    }
}
