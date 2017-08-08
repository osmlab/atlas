package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests RelationMember the compareTo function
 *
 * @author cstaylor
 */
public class RelationMemberComparisonTestCase
{
    @Rule
    public RelationMemberComparisonTestCaseRule setup = new RelationMemberComparisonTestCaseRule();

    @Test
    public void bothRuleRolesNull()
    {
        final RelationMember first = new RelationMember(null, this.setup.area1(), 0L);
        final RelationMember second = new RelationMember(null, this.setup.area1(), 0L);
        Assert.assertEquals(0, first.compareTo(second));
    }

    @Test
    public void deltaNegative()
    {
        final RelationMember first = new RelationMember("Something", this.setup.area1(), 0L);
        final RelationMember second = new RelationMember("Something", this.setup.area2(), 0L);
        Assert.assertEquals(-1, first.compareTo(second));
    }

    @Test
    public void deltaPositive()
    {
        final RelationMember first = new RelationMember("Something", this.setup.area1(), 0L);
        final RelationMember second = new RelationMember("Something", this.setup.area2(), 0L);
        Assert.assertEquals(1, second.compareTo(first));
    }

    @Test
    public void firstRuleRoleNull()
    {
        final RelationMember first = new RelationMember(null, this.setup.area1(), 0L);
        final RelationMember second = new RelationMember("Something", this.setup.area1(), 0L);
        Assert.assertEquals(-1, first.compareTo(second));
    }

    @Test
    public void neitherRuleRollNull()
    {
        final RelationMember first = new RelationMember("Something", this.setup.area1(), 0L);
        final RelationMember second = new RelationMember("Something", this.setup.area1(), 0L);
        Assert.assertEquals(0, first.compareTo(second));
    }

    @Ignore("We don't verify null")
    @Test
    public void secondNull()
    {
        final RelationMember first = new RelationMember("Something", this.setup.area1(), 0L);
        Assert.assertEquals(1, first.compareTo(null));
    }

    @Test
    public void secondRuleRollNull()
    {
        final RelationMember first = new RelationMember("Something", this.setup.area1(), 0L);
        final RelationMember second = new RelationMember(null, this.setup.area1(), 0L);
        Assert.assertEquals(1, first.compareTo(second));
    }
}
