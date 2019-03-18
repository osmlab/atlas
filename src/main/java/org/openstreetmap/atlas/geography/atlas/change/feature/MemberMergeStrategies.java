package org.openstreetmap.atlas.geography.atlas.change.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;
import org.openstreetmap.atlas.utilities.function.TernaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to store the various merge strategies utilized by the {@link FeatureChange} merge
 * code. These are low-level strategies used to merge the underlying data structures.
 *
 * @author lcram
 */
public final class MemberMergeStrategies
{
    private static final Logger logger = LoggerFactory.getLogger(MemberMergeStrategies.class);

    static final BinaryOperator<Map<String, String>> simpleTagMerger = Maps::withMaps;

    static final BinaryOperator<Set<Long>> simpleLongSetMerger = Sets::withSets;

    static final BinaryOperator<Set<Long>> simpleLongSetAllowCollisionsMerger = (left,
            right) -> Sets.withSets(false, left, right);

    static final BinaryOperator<SortedSet<Long>> simpleLongSortedSetMerger = Sets::withSortedSets;

    static final BinaryOperator<SortedSet<Long>> simpleLongSortedSetAllowCollisionsMerger = (left,
            right) -> Sets.withSortedSets(false, left, right);

    static final BinaryOperator<RelationBean> simpleRelationBeanMerger = RelationBean::merge;

    /*
     * Merge two differing Long Sets created using ADDs and REMOVEs on a common ancestor. For
     * example, consider set A: [2, 3, 4] and set B: [1, 2, 3, 5]. Assume these were both based on
     * initial set P: [1, 2, 3, 4]. Neither A nor B make any changes that conflict with each other,
     * so we should be able to merge them into set C: [2, 3, 5].
     */
    static final TernaryOperator<Set<Long>> diffBasedLongSetMerger = (before, afterLeft,
            afterRight) ->
    {
        final Set<Long> removedFromLeftView = com.google.common.collect.Sets.difference(before,
                afterLeft);
        final Set<Long> removedFromRightView = com.google.common.collect.Sets.difference(before,
                afterRight);
        final Set<Long> addedToLeftView = com.google.common.collect.Sets.difference(afterLeft,
                before);
        final Set<Long> addedToRightView = com.google.common.collect.Sets.difference(afterRight,
                before);

        /*
         * Easy key-merge of left and right ADDs and REMOVEs. We can safely ignore duplicate keys,
         * since it is feasible that two FeatureChanges made the same ADD or REMOVE. We also do not
         * need to rectify the ADD/ADD or ADD/REMOVE conflicts. Since these are keys-only, there is
         * no possibility of an ADD/ADD conflict. And because we enforce a shared beforeView, there
         * is no possibility of an ADD/REMOVE conflict.
         */
        final Set<Long> removedMerged = Sets.withSets(false, removedFromLeftView,
                removedFromRightView);
        final Set<Long> addedMerged = Sets.withSets(false, addedToLeftView, addedToRightView);

        /*
         * Build the result set by performing the REMOVEs on the beforeView, then performing the
         * ADDs on the beforeView.
         */
        final Set<Long> result = new HashSet<>(before);
        result.removeAll(removedMerged);
        result.addAll(addedMerged);

        return result;
    };

    /*
     * This is essentially the same thing as diffBasedLongSetMerger. However, we construct a
     * SortedSet before returning the result. This is useful for members that require a sorted
     * property.
     */
    static final TernaryOperator<SortedSet<Long>> diffBasedLongSortedSetMerger = (before, afterLeft,
            afterRight) ->
    {
        return new TreeSet<>(diffBasedLongSetMerger.apply(before, afterLeft, afterRight));
    };

    /*
     * Merge two differing tag maps creating using ADD/MODIFYs and REMOVEs on a common ancestor. For
     * example, consider map A: [a=1, b=12, c=3] and map B: [a=1, b=2, c=3, d=4, e=5]. Assume these
     * were both based on initial map P: [a=1, b=2, c=3, d=4]. Neither A nor B make any changes that
     * conflict with each other, so we should be able to merge them into map C: [a=1, b=12, c=3,
     * e=5].
     */
    static final TernaryOperator<Map<String, String>> diffBasedTagMerger = (before, afterLeft,
            afterRight) ->
    {
        /*
         * Simple key removal. A REMOVE looks like [a=1] -> []
         */
        final Set<String> keysRemovedFromLeftView = com.google.common.collect.Sets
                .difference(before.keySet(), afterLeft.keySet());
        final Set<String> keysRemovedFromRightView = com.google.common.collect.Sets
                .difference(before.keySet(), afterRight.keySet());

        /*
         * Consider the difference between an ADD [] -> [a=1] and a MODIFY [a=1] -> [a=2]. Below we
         * group key ADDs and MODIFYs together, since we operate on the entire key->value pair. In
         * light of this fact, ADDs and MODIFYs are effectively the same operation as far as merge
         * logic is concerned. From here on, we will only refer to ADD and never MODIFY.
         */
        final Map<String, String> addedToLeftView = com.google.common.collect.Sets
                .difference(afterLeft.entrySet(), before.entrySet()).stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final Map<String, String> addedToRightView = com.google.common.collect.Sets
                .difference(afterRight.entrySet(), before.entrySet()).stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        /*
         * Check to see if any of the shared ADD keys generate an ADD/ADD collision. An ADD/ADD
         * collision is when the same key maps to two different values.
         */
        final Set<String> sharedAddedKeys = com.google.common.collect.Sets
                .intersection(addedToLeftView.keySet(), addedToRightView.keySet());
        for (final String sharedKey : sharedAddedKeys)
        {
            final String leftValue = addedToLeftView.get(sharedKey);
            final String rightValue = addedToRightView.get(sharedKey);
            if (!Objects.equals(leftValue, rightValue))
            {
                throw new CoreException(
                        "diffBasedTagMerger failed due to ADD/ADD collision(s) on keys(s): [{} -> {}] vs [{} -> {}]",
                        sharedKey, leftValue, sharedKey, rightValue);
            }
        }

        /*
         * Now, check for any ADD/REMOVE collisions. An ADD/REMOVE collision is when one view of the
         * map contains an update to a key, but another view of the map removes the key entirely.
         */
        final Set<String> keysRemovedMerged = Sets.withSets(false, keysRemovedFromLeftView,
                keysRemovedFromRightView);
        final Set<String> keysAddedMerged = Sets.withSets(false, addedToLeftView.keySet(),
                addedToRightView.keySet());
        final Set<String> collision = com.google.common.collect.Sets.intersection(keysRemovedMerged,
                keysAddedMerged);
        if (!collision.isEmpty())
        {
            throw new CoreException(
                    "diffBasedTagMerger failed due to ADD/REMOVE collision(s) on key(s): {}",
                    collision);
        }

        /*
         * Now construct the merged map. Take the beforeView and REMOVE all keys that were in the
         * removedMerged set. Then, ADD all keys in the addMerged set. To get the values for those
         * keys, we select from whichever addedTo view (left or right) gives us a non-null mapping.
         * This correctly handles the case where one of the FeatureChanges is ADDing a brand new key
         * value pair (since the other FeatureChange will not contain a mapping).
         */
        final Map<String, String> result = new HashMap<>(before);
        keysRemovedMerged.forEach(result::remove);
        keysAddedMerged.forEach(key ->
        {
            String value = addedToLeftView.get(key);
            if (value == null)
            {
                value = addedToRightView.get(key);
            }
            result.put(key, value);
        });

        return result;
    };

    private MemberMergeStrategies()
    {
    }
}
