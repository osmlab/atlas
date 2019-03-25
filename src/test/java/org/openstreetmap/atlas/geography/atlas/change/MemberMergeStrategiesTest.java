package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author lcram
 */
public class MemberMergeStrategiesTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testDiffBasedLongSetMergeSuccess()
    {
        final Set<Long> before1 = Sets.hashSet(1L, 2L, 3L, 4L);
        final Set<Long> after1A = Sets.hashSet(2L, 3L, 4L);
        final Set<Long> after1B = Sets.hashSet(1L, 2L, 3L, 4L, 5L);
        Assert.assertEquals(Sets.hashSet(2L, 3L, 4L, 5L),
                MemberMergeStrategies.diffBasedLongSetMerger.apply(before1, after1A, after1B));
        Assert.assertEquals(Sets.hashSet(2L, 3L, 4L, 5L),
                MemberMergeStrategies.diffBasedLongSetMerger.apply(before1, after1B, after1A));

        final Set<Long> before2 = Sets.hashSet(1L, 2L, 3L, 4L);
        final Set<Long> after2A = Sets.hashSet(1L, 2L, 3L, 4L, 5L);
        final Set<Long> after2B = Sets.hashSet(1L, 2L, 3L, 4L, 6L);
        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L, 4L, 5L, 6L),
                MemberMergeStrategies.diffBasedLongSetMerger.apply(before2, after2A, after2B));
        Assert.assertEquals(Sets.hashSet(1L, 2L, 3L, 4L, 5L, 6L),
                MemberMergeStrategies.diffBasedLongSetMerger.apply(before2, after2B, after2A));

        final Set<Long> before3 = Sets.hashSet(1L, 2L, 3L);
        final Set<Long> after3A = Sets.hashSet();
        final Set<Long> after3B = Sets.hashSet(1L, 2L, 3L);
        Assert.assertEquals(Sets.hashSet(),
                MemberMergeStrategies.diffBasedLongSetMerger.apply(before3, after3A, after3B));
        Assert.assertEquals(Sets.hashSet(),
                MemberMergeStrategies.diffBasedLongSetMerger.apply(before3, after3B, after3A));
    }

    @Test
    public void testDiffBasedLongSortedSetMergeSuccess()
    {
        final SortedSet<Long> before1 = Sets.treeSet(1L, 2L, 3L, 4L);
        final SortedSet<Long> after1A = Sets.treeSet(2L, 3L, 4L);
        final SortedSet<Long> after1B = Sets.treeSet(1L, 2L, 3L, 4L, 5L);
        Assert.assertEquals(Sets.treeSet(2L, 3L, 4L, 5L),
                MemberMergeStrategies.diffBasedLongSortedSetMerger.apply(before1, after1A,
                        after1B));
        Assert.assertEquals(Sets.treeSet(2L, 3L, 4L, 5L),
                MemberMergeStrategies.diffBasedLongSortedSetMerger.apply(before1, after1B,
                        after1A));

        final SortedSet<Long> before2 = Sets.treeSet(1L, 2L, 3L, 4L);
        final SortedSet<Long> after2A = Sets.treeSet(1L, 2L, 3L, 4L, 5L);
        final SortedSet<Long> after2B = Sets.treeSet(1L, 2L, 3L, 4L, 6L);
        Assert.assertEquals(Sets.treeSet(1L, 2L, 3L, 4L, 5L, 6L),
                MemberMergeStrategies.diffBasedLongSortedSetMerger.apply(before2, after2A,
                        after2B));
        Assert.assertEquals(Sets.treeSet(1L, 2L, 3L, 4L, 5L, 6L),
                MemberMergeStrategies.diffBasedLongSortedSetMerger.apply(before2, after2B,
                        after2A));

        final SortedSet<Long> before3 = Sets.treeSet(1L, 2L, 3L);
        final SortedSet<Long> after3A = Sets.treeSet();
        final SortedSet<Long> after3B = Sets.treeSet(1L, 2L, 3L);
        Assert.assertEquals(Sets.treeSet(), MemberMergeStrategies.diffBasedLongSortedSetMerger
                .apply(before3, after3A, after3B));
        Assert.assertEquals(Sets.treeSet(), MemberMergeStrategies.diffBasedLongSortedSetMerger
                .apply(before3, after3B, after3A));
    }

    @Test
    public void testDiffBasedRelationBeanMergeADDADDConflict()
    {
        final RelationBean beforeBean = new RelationBean();
        beforeBean.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        beforeBean.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));

        /*
         * Add one instance of [2, AREA, areaRole2].
         */
        final RelationBean afterBean1 = new RelationBean();
        afterBean1.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        afterBean1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterBean1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));

        /*
         * Add two instances of [2, AREA, areaRole2].
         */
        final RelationBean afterBean2 = new RelationBean();
        afterBean2.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        afterBean2.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterBean2.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        afterBean2.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));

        /*
         * The merge will fail, since the number of added [2, AREA, areaRole2] conflict.
         */
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "diffBasedRelationBeanMerger failed due to ADD/ADD conflict on key: [AREA, 2, areaRole2]: beforeValue absolute count was 0 but addedLeft/Right diff counts conflict [1 vs 2]");
        MemberMergeStrategies.diffBasedRelationBeanMerger.apply(beforeBean, afterBean1, afterBean2);
    }

    @Test
    public void testDiffBasedRelationBeanMergeADDREMOVEConflict()
    {
        final RelationBean beforeBean = new RelationBean();
        beforeBean.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        beforeBean.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));

        /*
         * Add an additional instance of [1, LINE, lineRole1].
         */
        final RelationBean afterBean1 = new RelationBean();
        afterBean1.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        afterBean1.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        afterBean1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterBean1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));

        /*
         * Remove the instance of [1, LINE, lineRole1].
         */
        final RelationBean afterBean2 = new RelationBean();
        afterBean2.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterBean2.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));

        /*
         * The merge will fail, since one afterView tries to add an additional [1, LINE, lineRole1]
         * while the other afterView removes it entirely.
         */
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "diffBasedRelationBeanMerger failed due to ADD/REMOVE conflict(s) on key(s): [LINE, 1, lineRole1]");
        MemberMergeStrategies.diffBasedRelationBeanMerger.apply(beforeBean, afterBean1, afterBean2);
    }

    @Test
    public void testDiffBasedRelationBeanMergeREMOVEREMOVEConflict()
    {
        final RelationBean beforeBean = new RelationBean();
        beforeBean.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        beforeBean.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        beforeBean.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));

        /*
         * Remove one instance of [1, AREA, areaRole1].
         */
        final RelationBean afterBean1 = new RelationBean();
        afterBean1.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        afterBean1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));

        /*
         * Remove both instances of [1, AREA, areaRole1].
         */
        final RelationBean afterBean2 = new RelationBean();
        afterBean2.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));

        /*
         * The merge will fail, since the number of removed [1, AREA, areaRole1] conflict.
         */
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "diffBasedRelationBeanMerger failed due to REMOVE/REMOVE conflict on key: [AREA, 1, areaRole1]: beforeValue absolute count was 2 but removedLeft/Right diff counts conflict [1 vs 2]");
        MemberMergeStrategies.diffBasedRelationBeanMerger.apply(beforeBean, afterBean1, afterBean2);
    }

    @Test
    public void testDiffBasedRelationBeanMergeSuccess()
    {
        final RelationBean beforeBean = new RelationBean();
        beforeBean.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(3L, "pointRole3", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        beforeBean.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));

        /*
         * ADD two Areas with ID 2. REMOVE Point with ID 2. Add 2 Lines with ID 2 - this ADD is
         * shared by the other afterBean. REMOVE Point with ID 3 - this REMOVE is shared by the
         * other afterBean.
         */
        final RelationBean afterBean1 = new RelationBean();
        afterBean1.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        afterBean1.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        afterBean1.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        afterBean1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterBean1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        afterBean1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));

        /*
         * Change role for Point with ID 1. This effectively REMOVEs the original [1, POINT,
         * pointRole1] and replaces it with [1, POINT, newPointRole1]. Add 2 Lines with ID 2 - this
         * ADD is shared by the other afterBean. REMOVE Point with ID 3 - this REMOVE is shared by
         * the other afterBean.
         */
        final RelationBean afterBean2 = new RelationBean();
        afterBean2.addItem(new RelationBeanItem(1L, "newPointRole1", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        afterBean2.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        afterBean2.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        afterBean2.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));

        /*
         * The expected result of merging afterBean1 and afterBean2.
         */
        final RelationBean goldenImage1 = new RelationBean();
        goldenImage1.addItem(new RelationBeanItem(1L, "newPointRole1", ItemType.POINT));
        goldenImage1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        goldenImage1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        goldenImage1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
        goldenImage1.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        goldenImage1.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));
        goldenImage1.addItem(new RelationBeanItem(2L, "lineRole2", ItemType.LINE));

        Assert.assertEquals(goldenImage1, MemberMergeStrategies.diffBasedRelationBeanMerger
                .apply(beforeBean, afterBean1, afterBean2));
    }

    @Test
    public void testDiffBasedTagMergeADDADDConflictFail()
    {
        final Map<String, String> before1 = Maps.hashMap("a", "1", "b", "2");
        final Map<String, String> after1A = Maps.hashMap("a", "10", "b", "2");
        final Map<String, String> after1B = Maps.hashMap("a", "12", "b", "2");

        this.expectedException.expect(CoreException.class);
        this.expectedException
                .expectMessage("diffBasedTagMerger failed due to ADD/ADD collision(s) on keys");
        MemberMergeStrategies.diffBasedTagMerger.apply(before1, after1A, after1B);
    }

    @Test
    public void testDiffBasedTagMergeADDREMOVEConflictFail()
    {
        final Map<String, String> before1 = Maps.hashMap("a", "1", "b", "2");
        final Map<String, String> after1A = Maps.hashMap("a", "10", "b", "2");
        final Map<String, String> after1B = Maps.hashMap("b", "2");

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "diffBasedTagMerger failed due to ADD/REMOVE collision(s) on key(s): [a]");
        MemberMergeStrategies.diffBasedTagMerger.apply(before1, after1A, after1B);
    }

    @Test
    public void testDiffBasedTagMergeSuccess()
    {
        final Map<String, String> before1 = Maps.hashMap("a", "1", "b", "2", "c", "3", "d", "4");
        final Map<String, String> after1A = Maps.hashMap("b", "2", "c", "3", "d", "4");
        final Map<String, String> after1B = Maps.hashMap("a", "1", "b", "2", "c", "3", "d", "4",
                "e", "5");
        Assert.assertEquals(Maps.hashMap("b", "2", "c", "3", "d", "4", "e", "5"),
                MemberMergeStrategies.diffBasedTagMerger.apply(before1, after1A, after1B));
        Assert.assertEquals(Maps.hashMap("b", "2", "c", "3", "d", "4", "e", "5"),
                MemberMergeStrategies.diffBasedTagMerger.apply(before1, after1B, after1A));

        final Map<String, String> before2 = Maps.hashMap("water", "lake");
        final Map<String, String> after2A = Maps.hashMap("water", "lake", "seasonal", "yes");
        final Map<String, String> after2B = Maps.hashMap("water", "lake", "salt", "yes");
        Assert.assertEquals(Maps.hashMap("water", "lake", "seasonal", "yes", "salt", "yes"),
                MemberMergeStrategies.diffBasedTagMerger.apply(before2, after2A, after2B));
        Assert.assertEquals(Maps.hashMap("water", "lake", "seasonal", "yes", "salt", "yes"),
                MemberMergeStrategies.diffBasedTagMerger.apply(before2, after2B, after2A));

        final Map<String, String> before3 = Maps.hashMap("a", "1", "b", "2", "c", "3", "d", "4");
        final Map<String, String> after3A = Maps.hashMap();
        final Map<String, String> after3B = Maps.hashMap("a", "1", "b", "2", "c", "3", "d", "4");
        Assert.assertEquals(Maps.hashMap(),
                MemberMergeStrategies.diffBasedTagMerger.apply(before3, after3A, after3B));
        Assert.assertEquals(Maps.hashMap(),
                MemberMergeStrategies.diffBasedTagMerger.apply(before3, after3B, after3A));

        final Map<String, String> before4 = Maps.hashMap();
        final Map<String, String> after4A = Maps.hashMap("a", "1");
        final Map<String, String> after4B = Maps.hashMap("b", "2");
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2"),
                MemberMergeStrategies.diffBasedTagMerger.apply(before4, after4A, after4B));
        Assert.assertEquals(Maps.hashMap("a", "1", "b", "2"),
                MemberMergeStrategies.diffBasedTagMerger.apply(before4, after4B, after4A));

        final Map<String, String> before5 = Maps.hashMap();
        final Map<String, String> after5A = Maps.hashMap("a", "1");
        final Map<String, String> after5B = Maps.hashMap("a", "1");
        Assert.assertEquals(Maps.hashMap("a", "1"),
                MemberMergeStrategies.diffBasedTagMerger.apply(before5, after5A, after5B));
        Assert.assertEquals(Maps.hashMap("a", "1"),
                MemberMergeStrategies.diffBasedTagMerger.apply(before5, after5B, after5A));
    }
}
