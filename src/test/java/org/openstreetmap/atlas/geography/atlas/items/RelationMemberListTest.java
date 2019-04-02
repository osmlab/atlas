package org.openstreetmap.atlas.geography.atlas.items;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;

public class RelationMemberListTest
{
    @Test
    public void testEqualsExplicitlyExcluded()
    {
        final RelationMember member = new RelationMember(null,
                new CompletePoint(1L, null, null, null), 1L);

        final RelationMemberList list1 = new RelationMemberList(Arrays.asList(member));
        list1.addItemExplicitlyExcluded(new RelationBeanItem(1L, "role", ItemType.AREA));

        final RelationMemberList list2 = new RelationMemberList(Arrays.asList(member));
        list2.addItemExplicitlyExcluded(new RelationBeanItem(1L, "role", ItemType.AREA));

        final RelationMemberList list3 = new RelationMemberList(Arrays.asList(member));
        list3.addItemExplicitlyExcluded(new RelationBeanItem(2L, "role", ItemType.AREA));

        Assert.assertTrue(list1.equalsIncludingExplicitlyExcluded(list2));
        Assert.assertFalse(list1.equalsIncludingExplicitlyExcluded(list3));
        Assert.assertFalse(list1.equalsIncludingExplicitlyExcluded("foo"));
    }
}
