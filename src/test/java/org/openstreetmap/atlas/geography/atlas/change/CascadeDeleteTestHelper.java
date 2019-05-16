package org.openstreetmap.atlas.geography.atlas.change;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ConnectedEdgeType;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Yazad Khambata
 */
class CascadeDeleteTestHelper {

    private CascadeDeleteTestRule rule;

    CascadeDeleteTestHelper(final CascadeDeleteTestRule rule) {
        this.rule = rule;
    }

    protected static <E extends AtlasEntity> long count(final Iterable<E> entities) {
        return StreamSupport.stream(entities.spliterator(), false).count();
    }

    protected boolean isEntityPresentInRelation(final Atlas atlas, final ItemType itemType,
                                              final long entityRelationIdentifier, final long entityIdentifier) {
        final Relation relation = atlas.relation(entityRelationIdentifier);

        Assert.assertNotNull(relation);

        return relation.members().stream()
                .filter(relationMember -> relationMember.getEntity().getType() == itemType)
                .filter(relationMember -> relationMember.getEntity().getIdentifier() == entityIdentifier)
                .findFirst()
                .isPresent();
    }

    protected Atlas deleteNode(final Atlas atlas, final long nodeIdToDelete, final long relationId) {
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

    protected boolean isNodeMemberPresent(final Atlas atlas, final long nodeIdToDelete, final long relationId) {
        final Predicate<RelationMember> relationMemberPredicate =
                member -> member.getEntity().getType() == ItemType.NODE && member.getEntity().getIdentifier() == nodeIdToDelete;

        return !atlas.relation(relationId)
                .membersMatching(relationMemberPredicate)
                .isEmpty();
    }

    protected void deleteEdgeButNotReverse(final long edgeIdentifier, final long relationIdentifier) {
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

        Assert.assertFalse(isEdgeRelatedToNode(changedAtlas, startForward.getIdentifier(), edgeIdentifier,
                ConnectedEdgeType.OUT));
        Assert.assertFalse(isEdgeRelatedToNode(changedAtlas, endForward.getIdentifier(), edgeIdentifier,
                ConnectedEdgeType.IN));
        Assert.assertFalse(isEdgeRelatedToNode(changedAtlas, endReverse.getIdentifier(), edgeIdentifier,
                ConnectedEdgeType.OUT));
        Assert.assertFalse(isEdgeRelatedToNode(changedAtlas, startReverse.getIdentifier(), edgeIdentifier,
                ConnectedEdgeType.IN));

        verifyCounts(changedAtlas, Pair.of(ItemType.EDGE, CascadeDeleteTestRule.EDGE_COUNT - 1));
    }

    protected boolean isEdgeRelatedToNode(final Atlas atlas, final long nodeId, final long edgeId,
                                        final ConnectedEdgeType connectedEdgeType) {
        final Node node = atlas.node(nodeId);
        Assert.assertNotNull(node);
        return isEdgeRelatedToNode(node, edgeId, connectedEdgeType);
    }

    protected boolean isEdgeRelatedToNode(final Node node, final long edgeId, final ConnectedEdgeType connectedEdgeType) {
        return node.connectedEdges(connectedEdgeType).stream()
                .filter(edge -> edge.getIdentifier() == edgeId)
                .findFirst()
                .isPresent();
    }

    protected Atlas getAtlas() {
        return rule.getAtlas();
    }

    protected Set<Long> getMatchingEdgesInRelation(final Atlas atlas, final long relationIdentifier,
                                                 final long edgeIdentifier) {
        return atlas.relation(relationIdentifier).members().stream()
                .filter(relationMember -> relationMember.getEntity().getType() == ItemType.EDGE)
                .filter(relationMember -> Math.abs(
                        relationMember.getEntity().getIdentifier()) == Math.abs(edgeIdentifier))
                .map(relationMember -> relationMember.getEntity().getIdentifier())
                .collect(Collectors.toSet());
    }


    protected void testDeleteSimple(final ItemType itemType, final long entityIdentifier,
                                  final long entityRelationIdentifier) {
        final boolean relationEmptiesAndAutoDeletes = false;

        testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier, relationEmptiesAndAutoDeletes);
    }

    protected void testDeleteSimple(final ItemType itemType, final long entityIdentifier,
                                  final long entityRelationIdentifier, final boolean relationEmptiesAndAutoDeletes) {
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

        if (!relationEmptiesAndAutoDeletes) {
            Assert.assertNotNull(changeAtlas.relation(entityRelationIdentifier));
            Assert.assertFalse(isEntityPresentInRelation(changeAtlas, itemType, entityRelationIdentifier, entityIdentifier));
            verifyCounts(changeAtlas, override(itemType, -1));
        } else {
            Assert.assertNull(changeAtlas.relation(entityRelationIdentifier));
            verifyCounts(changeAtlas, override(itemType, -1), override(ItemType.RELATION, -1));
        }
    }

    protected Pair<ItemType, Long> override(final ItemType itemType, final int override) {
        return Pair.of(itemType, rule.getCountExpectationMapping().get(itemType) + override);
    }

    protected void verifyCounts(final Atlas atlas, final Pair<ItemType, Long>... overrides) {
        final Map<ItemType, Long> countExpectationMapping = rule.getCountExpectationMapping();

        final Map<ItemType, Long> overrideMapping = Arrays.stream(overrides)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        countExpectationMapping.putAll(overrideMapping);

        Assert.assertEquals(ItemType.values().length, countExpectationMapping.size());

        for (final Map.Entry<ItemType, Long> entry : countExpectationMapping.entrySet()) {
            Assert.assertEquals("entry failed: " + entry, (long) entry.getValue(),
                    entry.getKey().numberOfEntities(atlas));
        }
    }

    protected Atlas deleteSimpleRelation(final long entityIdentifier, final Atlas atlas) {
        verifyCounts(atlas);

        final Relation relation = atlas.relation(entityIdentifier);

        final FeatureChange featureChange = FeatureChange.remove(CompleteRelation.shallowFrom(relation), atlas);

        final Change change = ChangeBuilder.newInstance().add(featureChange).get();

        return new ChangeAtlas(atlas, change);
    }
}
