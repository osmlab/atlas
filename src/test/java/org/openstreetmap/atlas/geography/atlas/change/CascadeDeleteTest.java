package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * @author Yazad Khambata
 */
public class CascadeDeleteTest
{
    @Rule
    public final CascadeDeleteTestRule rule = new CascadeDeleteTestRule();

    private CascadeDeleteTestHelper helper = new CascadeDeleteTestHelper(rule);

    @Test
    public void deleteUnrelatedNode()
    {
        final Atlas atlas = helper.getAtlas();
        final long nodeIdToDelete = CascadeDeleteTestRule.NON_EDGE_NODE_IDENTIFIER;
        final long nodeRelationId = CascadeDeleteTestRule.NON_EDGE_NODE_RELATION_IDENTIFIER;

        final Atlas changeAtlas = helper.deleteNode(atlas, nodeIdToDelete, nodeRelationId);

        helper.verifyCounts(changeAtlas, helper.override(ItemType.NODE, -1));
    }

    @Test
    public void deleteEdgeStartNode()
    {
        final Atlas atlas = helper.getAtlas();
        final long nodeIdToDelete = CascadeDeleteTestRule.START_END_EDGE_NODE;
        final long nodeRelationId = CascadeDeleteTestRule.NON_EDGE_NODE_RELATION_IDENTIFIER;

        final Atlas changeAtlas = helper.deleteNode(atlas, nodeIdToDelete, nodeRelationId);

        helper.verifyCounts(changeAtlas, helper.override(ItemType.NODE, -1),
                helper.override(ItemType.EDGE, -2));
    }

    @Test
    public void testDeleteForwardEdgeToRelationMemberCascadeButNotReverseEdge()
    {
        final long edgeIdentifier = CascadeDeleteTestRule.EDGE_IDENTIFIER;
        helper.deleteEdgeButNotReverse(edgeIdentifier,
                CascadeDeleteTestRule.EDGE_RELATION_IDENTIFIER);
    }

    @Test
    public void testDeleteReverseEdgeToRelationMemberCascadeButNotForwardEdge()
    {
        final long edgeIdentifier = -(CascadeDeleteTestRule.EDGE_IDENTIFIER);
        helper.deleteEdgeButNotReverse(edgeIdentifier,
                CascadeDeleteTestRule.EDGE_RELATION_IDENTIFIER);
    }

    @Test
    public void testDeleteLineToRelationMemberCascade()
    {
        final ItemType itemType = ItemType.LINE;
        final long entityIdentifier = CascadeDeleteTestRule.LINE_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.LINE_RELATION_IDENTIFIER;

        helper.testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }

    @Test
    public void testDeleteAreaToRelationMemberCascade()
    {
        final ItemType itemType = ItemType.AREA;
        final long entityIdentifier = CascadeDeleteTestRule.AREA_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.AREA_RELATION_IDENTIFIER;

        helper.testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }

    @Test
    public void testDeletePointToRelationMemberCascade()
    {
        final ItemType itemType = ItemType.POINT;
        final long entityIdentifier = CascadeDeleteTestRule.POINT_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.POINT_RELATION_IDENTIFIER;

        helper.testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }

    @Test
    public void deleteRelation()
    {
        final long entityIdentifier = CascadeDeleteTestRule.TOP_LEVEL_RELATION_IDENTIFIER;
        final Atlas atlas = helper.getAtlas();
        final Atlas changeAtlas = helper.deleteSimpleRelation(entityIdentifier, atlas);

        helper.verifyCounts(changeAtlas, helper.override(ItemType.RELATION, -1));
    }

    @Test
    public void testDeleteSubRelation()
    {
        final ItemType itemType = ItemType.RELATION;
        final long entityIdentifier = CascadeDeleteTestRule.SUB_RELATION_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.PARENT_RELATION_IDENTIFIER;

        helper.testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }

    @Test
    public void testAutoDeleteEmptyRelations()
    {
        final ItemType itemType = ItemType.POINT;
        final long entityIdentifier = CascadeDeleteTestRule.THE_ONLY_RELATION_MEMBER_POINT_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.ONE_MEMBER_RELATION_IDENTIFIER;

        helper.testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier, true);
    }
}
