package org.openstreetmap.atlas.geography.atlas.change.feature;

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
    public void testDiffBasedRelationBeanADDADDCollisionFail()
    {

    }

    @Test
    public void testDiffBasedRelationBeanMergeSuccess()
    {
        final RelationBean beforeBean = new RelationBean();
        beforeBean.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        beforeBean.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        beforeBean.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));

        /*
         * ADD an Area with ID 2 and a new role. REMOVE Point with ID 2.
         */
        final RelationBean afterBean1 = new RelationBean();
        afterBean1.addItem(new RelationBeanItem(1L, "pointRole1", ItemType.POINT));
        afterBean1.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        afterBean1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        afterBean1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));

        /*
         * MODIFY role of Point with ID 1.
         */
        final RelationBean afterBean2 = new RelationBean();
        afterBean2.addItem(new RelationBeanItem(1L, "newPointRole1", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(2L, "pointRole2", ItemType.POINT));
        afterBean2.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        afterBean2.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));

        /*
         * The expected result of merging afterBean1 and afterBean2.
         */
        final RelationBean goldenImage1 = new RelationBean();
        goldenImage1.addItem(new RelationBeanItem(1L, "newPointRole1", ItemType.POINT));
        goldenImage1.addItem(new RelationBeanItem(1L, "lineRole1", ItemType.LINE));
        goldenImage1.addItem(new RelationBeanItem(1L, "areaRole1", ItemType.AREA));
        goldenImage1.addItem(new RelationBeanItem(2L, "areaRole2", ItemType.AREA));
    }

    @Test
    public void testDiffBasedRelationBeanMODIFYREMOVECollisionFail()
    {

    }

    @Test
    public void testDiffBasedTagMergeADDADDCollisionFail()
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
    public void testDiffBasedTagMergeADDREMOVECollisionFail()
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
