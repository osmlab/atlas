package org.openstreetmap.atlas.geography.atlas.packed;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.tags.RelationTypeTag;

/**
 * @author matthieun
 */
public class PackedRelationTest
{
    @Rule
    public PackedRelationTestCaseRule rule = new PackedRelationTestCaseRule();

    @Test
    public void testSameRelationMemberWithDifferentRole()
    {
        final Atlas atlas = this.rule.getAtlas();
        final RelationMemberList relationMembers = atlas
                .relation(PackedRelationTestCaseRule.RELATION_IDENTIFIER).members();
        Assert.assertEquals(3, relationMembers.size());
        Assert.assertTrue(listContains(relationMembers, RelationTypeTag.RESTRICTION_ROLE_FROM));
        Assert.assertTrue(listContains(relationMembers, RelationTypeTag.RESTRICTION_ROLE_VIA));
        Assert.assertTrue(listContains(relationMembers, RelationTypeTag.RESTRICTION_ROLE_TO));
    }

    private boolean listContains(final RelationMemberList list, final String role)
    {
        for (final RelationMember member : list)
        {
            if (member.getRole().equals(role))
            {
                return true;
            }
        }
        return false;
    }
}
