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

    private final CascadeDeleteTestHelper helper = new CascadeDeleteTestHelper(this.rule);

    @Test
    public void deleteEdgeStartNode()
    {
        final Atlas atlas = this.helper.getAtlas();
        final long nodeIdToDelete = CascadeDeleteTestRule.START_END_EDGE_NODE;
        final long nodeRelationId = CascadeDeleteTestRule.NON_EDGE_NODE_RELATION_IDENTIFIER;
        final Atlas changeAtlas = this.helper.deleteNode(atlas, nodeIdToDelete, nodeRelationId);
        this.helper.verifyCounts(changeAtlas, this.helper.override(ItemType.NODE, -1),
                this.helper.override(ItemType.EDGE, -2));
    }

    @Test
    public void deleteRelation()
    {
        final long entityIdentifier = CascadeDeleteTestRule.TOP_LEVEL_RELATION_IDENTIFIER;
        final Atlas atlas = this.helper.getAtlas();
        final Atlas changeAtlas = this.helper.deleteSimpleRelation(entityIdentifier, atlas);
        this.helper.verifyCounts(changeAtlas, this.helper.override(ItemType.RELATION, -1));
    }

    @Test
    public void deleteUnrelatedNode()
    {
        final Atlas atlas = this.helper.getAtlas();
        final long nodeIdToDelete = CascadeDeleteTestRule.NON_EDGE_NODE_IDENTIFIER;
        final long nodeRelationId = CascadeDeleteTestRule.NON_EDGE_NODE_RELATION_IDENTIFIER;
        final Atlas changeAtlas = this.helper.deleteNode(atlas, nodeIdToDelete, nodeRelationId);
        this.helper.verifyCounts(changeAtlas, this.helper.override(ItemType.NODE, -1));
    }

    @Test
    public void testAutoDeleteEmptyRelations()
    {
        final ItemType itemType = ItemType.POINT;
        final long entityIdentifier = CascadeDeleteTestRule.THE_ONLY_RELATION_MEMBER_POINT_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.ONE_MEMBER_RELATION_IDENTIFIER;
        this.helper.testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier, true);
    }

    @Test
    public void testDeleteAreaToRelationMemberCascade()
    {
        final ItemType itemType = ItemType.AREA;
        final long entityIdentifier = CascadeDeleteTestRule.AREA_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.AREA_RELATION_IDENTIFIER;

        this.helper.testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }

    @Test
    public void testDeleteForwardEdgeToRelationMemberCascadeButNotReverseEdge()
    {
        final long edgeIdentifier = CascadeDeleteTestRule.EDGE_IDENTIFIER;
        this.helper.deleteEdgeButNotReverse(edgeIdentifier,
                CascadeDeleteTestRule.EDGE_RELATION_IDENTIFIER);
    }

    @Test
    public void testDeleteLineToRelationMemberCascade()
    {
        final ItemType itemType = ItemType.LINE;
        final long entityIdentifier = CascadeDeleteTestRule.LINE_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.LINE_RELATION_IDENTIFIER;

        this.helper.testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }

    @Test
    public void testDeletePointToRelationMemberCascade()
    {
        final ItemType itemType = ItemType.POINT;
        final long entityIdentifier = CascadeDeleteTestRule.POINT_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.POINT_RELATION_IDENTIFIER;

        this.helper.testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }

    @Test
    public void testDeleteReverseEdgeToRelationMemberCascadeButNotForwardEdge()
    {
        final long edgeIdentifier = -(CascadeDeleteTestRule.EDGE_IDENTIFIER);
        this.helper.deleteEdgeButNotReverse(edgeIdentifier,
                CascadeDeleteTestRule.EDGE_RELATION_IDENTIFIER);
    }

    @Test
    public void testDeleteSubRelation()
    {
        final ItemType itemType = ItemType.RELATION;
        final long entityIdentifier = CascadeDeleteTestRule.SUB_RELATION_IDENTIFIER;
        final long entityRelationIdentifier = CascadeDeleteTestRule.PARENT_RELATION_IDENTIFIER;
        this.helper.testDeleteSimple(itemType, entityIdentifier, entityRelationIdentifier);
    }
}
