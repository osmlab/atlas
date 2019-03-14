package org.openstreetmap.atlas.geography.atlas.change.feature;

import java.util.HashMap;
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

    static final BinaryOperator<Map<String, String>> tagMerger = Maps::withMaps;

    static final BinaryOperator<Set<Long>> directReferenceMerger = Sets::withSets;

    static final BinaryOperator<SortedSet<Long>> directReferenceMergerSorted = Sets::withSortedSets;

    static final BinaryOperator<SortedSet<Long>> directReferenceMergerLooseSorted = (left,
            right) -> Sets.withSortedSets(false, left, right);

    static final BinaryOperator<Set<Long>> directReferenceMergerLoose = (left, right) -> Sets
            .withSets(false, left, right);

    static final BinaryOperator<RelationBean> relationBeanMerger = RelationBean::merge;

    static final TernaryOperator<SortedSet<Long>> diffBasedIntegerSortedSetMerger = (before,
            afterLeft, afterRight) ->
    {
        final Set<Long> removedFromLeftView = com.google.common.collect.Sets.difference(before,
                afterLeft);
        final Set<Long> removedFromRightView = com.google.common.collect.Sets.difference(before,
                afterRight);
        final Set<Long> addedToLeftView = com.google.common.collect.Sets.difference(afterLeft,
                before);
        final Set<Long> addedToRightView = com.google.common.collect.Sets.difference(afterRight,
                before);

        final Set<Long> removedMerged = Sets.withSets(false, removedFromLeftView,
                removedFromRightView);
        final Set<Long> addedMerged = Sets.withSets(false, addedToLeftView, addedToRightView);

        final Set<Long> collision = com.google.common.collect.Sets.intersection(removedMerged,
                addedMerged);
        if (!collision.isEmpty())
        {
            throw new CoreException(
                    "diffBasedIntegerSortedSetMerger failed due to ADD/REMOVE collision(s) on: {}",
                    collision);
        }

        final SortedSet<Long> result = new TreeSet<>(before);
        result.removeAll(removedMerged);
        result.addAll(addedMerged);

        return result;
    };

    static final TernaryOperator<Map<String, String>> diffBasedTagMerger = (before, afterLeft,
            afterRight) ->
    {
        final Set<String> keysRemovedFromLeftView = com.google.common.collect.Sets
                .difference(before.keySet(), afterLeft.keySet());
        final Set<String> keysRemovedFromRightView = com.google.common.collect.Sets
                .difference(before.keySet(), afterRight.keySet());

        /*
         * Here, we effectively group key ADDs and MODIFYs together, since we operate on the entire
         * key->value pair. In light of this fact, ADDs and MODIFYs are effectively the same
         * operation.
         */
        final Map<String, String> addedToLeftView = com.google.common.collect.Sets
                .difference(afterLeft.entrySet(), before.entrySet()).stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final Map<String, String> addedToRightView = com.google.common.collect.Sets
                .difference(afterRight.entrySet(), before.entrySet()).stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        /*
         * Check to see if any of the shared ADD keys generate an ADD collision. An ADD collision is
         * when the same key maps to two different values.
         */
        final Set<String> sharedKeys = com.google.common.collect.Sets
                .intersection(addedToLeftView.keySet(), addedToRightView.keySet());
        for (final String sharedKey : sharedKeys)
        {
            final String leftValue = addedToLeftView.get(sharedKey);
            final String rightValue = addedToRightView.get(sharedKey);
            if (!Objects.equals(leftValue, rightValue))
            {
                throw new CoreException(
                        "diffBasedTagMergerFailed due to key ADD collision: [{} -> {}] vs [{} -> {}]",
                        sharedKey, leftValue, sharedKey, rightValue);
            }
        }

        /*
         * Now, check for any ADD/REMOVE collisions.
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
         * Now construct the merged map. Take the beforeView and remove all keys that were in the
         * removedMerged set. Then, add all keys in the addMerged set. To get the values for those
         * keys, we arbitrarily use the leftView, since we already asserted that there are no ADD
         * collisions between left and right.
         */
        final Map<String, String> result = new HashMap<>(before);
        keysRemovedMerged.forEach(result::remove);
        keysAddedMerged.forEach(key -> result.put(key, addedToLeftView.get(key)));

        return result;
    };

    private MemberMergeStrategies()
    {
    }
}
