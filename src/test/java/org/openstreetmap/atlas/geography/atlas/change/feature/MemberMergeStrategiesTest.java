package org.openstreetmap.atlas.geography.atlas.change.feature;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author lcram
 */
public class MemberMergeStrategiesTest
{
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
        final Map<String, String> after2A = Maps.hashMap("water", "lake", "seaonsal", "yes");
        final Map<String, String> after2B = Maps.hashMap("water", "lake", "salt", "yes");
        Assert.assertEquals(Maps.hashMap("water", "lake", "seaonsal", "yes", "salt", "yes"),
                MemberMergeStrategies.diffBasedTagMerger.apply(before2, after2A, after2B));
        Assert.assertEquals(Maps.hashMap("water", "lake", "seaonsal", "yes", "salt", "yes"),
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
        Assert.assertEquals(Maps.hashMap("a", "1"),
                MemberMergeStrategies.simpleTagMerger.apply(after5B, after5A));
    }
}
