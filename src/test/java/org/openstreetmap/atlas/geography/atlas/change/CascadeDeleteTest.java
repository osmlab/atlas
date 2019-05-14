package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Yazad Khambata
 */
public class CascadeDeleteTest {
    public static final long EDGE_IDENTIFIER = 221434102000004L;
    public static final long EDGE_RELATION_IDENTIFIER = 8610465000000L;
    public static final long LINE_IDENTIFIER = 8920472000000L;
    public static final long LINE_RELATION_IDENTIFIER = 2768040000000L;
    @Rule
    public final CascadeDeleteTestRule rule = new CascadeDeleteTestRule();

    private static <E extends AtlasEntity> long count(Iterable<E> entities) {
        return StreamSupport.stream(entities.spliterator(), false).count();
    }

    public void testDeleteArea() {
        final Atlas atlas = getAtlas();

        
    }

    @Test
    public void testDeleteLineToRelationCascade() {
        final Atlas atlas = getAtlas();

        final Line line = atlas.line(LINE_IDENTIFIER);
        Assert.assertNotNull(line);
        final Relation relation = atlas.relation(LINE_RELATION_IDENTIFIER);
        Assert.assertNotNull(relation);

        Assert.assertTrue(isLinePresentInRelation(atlas, LINE_RELATION_IDENTIFIER, LINE_IDENTIFIER));

        final FeatureChange featureChange = FeatureChange.remove(CompleteLine.shallowFrom(line));
        final Change change = ChangeBuilder.newInstance().add(featureChange).get();

        final ChangeAtlas changeAtlas = new ChangeAtlas(atlas, change);

        Assert.assertNull(changeAtlas.line(LINE_IDENTIFIER));
        Assert.assertFalse(isLinePresentInRelation(changeAtlas, LINE_RELATION_IDENTIFIER, LINE_IDENTIFIER));
    }

    private boolean isLinePresentInRelation(final Atlas atlas, final long lineRelationIdentifier,
                                            final long lineIdentifier) {
        final Relation relation = atlas.relation(lineRelationIdentifier);

        return relation.members().stream()
                .filter(relationMember -> relationMember.getEntity().getType() == ItemType.LINE)
                .filter(relationMember -> relationMember.getEntity().getIdentifier() == lineIdentifier)
                .findFirst()
                .isPresent();
    }

    @Test
    public void testDeleteForwardEdgeToRelationCascadeButNotReverseEdge() {
        long edgeIdentifier = EDGE_IDENTIFIER;
        deleteEdgeBytNotReverse(edgeIdentifier, EDGE_RELATION_IDENTIFIER);
    }

    @Test
    public void testDeleteReverseEdgeToRelationCascadeButNotForwardEdge() {
        long edgeIdentifier = -EDGE_IDENTIFIER;
        deleteEdgeBytNotReverse(edgeIdentifier, EDGE_RELATION_IDENTIFIER);
    }

    private void deleteEdgeBytNotReverse(final long edgeIdentifier, final long relationIdentifier) {
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

        final Set<Long> edgeIdentifiers = getMatchingEdgesInRelation(atlas, relationIdentifier, edgeIdentifier);

        Assert.assertEquals(edgeIdentifiers.size(), 2);
        Assert.assertTrue(edgeIdentifiers.contains(edgeIdentifier));
        Assert.assertTrue(edgeIdentifiers.contains(reverseIdentifier));

        Assert.assertNotNull(atlas.node(startForward.getIdentifier()));
        Assert.assertNotNull(atlas.node(endForward.getIdentifier()));

        final FeatureChange removeFeatureChange = FeatureChange.remove(CompleteEdge.shallowFrom(edge), atlas);

        final Change change = ChangeBuilder.newInstance().add(removeFeatureChange).get();

        final Atlas changedAtlas = new ChangeAtlas(atlas, change);

        Assert.assertNull(changedAtlas.edge(edgeIdentifier));
        Assert.assertNotNull(changedAtlas.edge(reverseIdentifier));

        final long countAfter = count(changedAtlas.edges());

        Assert.assertEquals(countBefore - 1, countAfter);

        Assert.assertNotNull(changedAtlas.node(startForward.getIdentifier()));
        Assert.assertNotNull(changedAtlas.node(endForward.getIdentifier()));

        final Set<Long> edgeIdentifiersAfterDelete = getMatchingEdgesInRelation(changedAtlas, relationIdentifier,
                edgeIdentifier);

        Assert.assertEquals(edgeIdentifiersAfterDelete.size(), 1);
        Assert.assertFalse(edgeIdentifiersAfterDelete.contains(edgeIdentifier));
        Assert.assertTrue(edgeIdentifiersAfterDelete.contains(reverseIdentifier));
    }

    private Atlas getAtlas() {
        return rule.getAtlas();
    }

    private Set<Long> getMatchingEdgesInRelation(final Atlas atlas, final long relationIdentifier,
                                                 final long edgeIdentifier) {
        return atlas.relation(relationIdentifier)
                .members()
                .stream()
                .filter(relationMember -> relationMember.getEntity().getType() == ItemType.EDGE)
                .filter(relationMember -> Math.abs(relationMember.getEntity().getIdentifier()) == Math.abs(edgeIdentifier))
                .map(relationMember -> relationMember.getEntity().getIdentifier())
                .collect(Collectors.toSet());
    }
}
