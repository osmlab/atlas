package org.openstreetmap.atlas.geography.atlas.change.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.Sets;
import org.openstreetmap.atlas.utilities.function.TernaryOperator;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
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
     *
     */
    static final TernaryOperator<RelationBean> diffBasedRelationBeanMerger = (beforeBean,
            afterLeftBean, afterRightBean) ->
    {
        /*
         * In the following merge logic, we treat RelationBeanItems as Key/Value pairs, where the
         * Key is a conglomerate of the ID/ItemType and the Value is the role. From here on, assume
         * a Key is an ID/ItemType combo and a Value is a role.
         */

        /*
         * Compute ADDs and MODIFYs for the left side. An ADD is a RelationBeanItem with a brand new
         * Key not seen in the beforeView. A MODIFY is a RelationBeanItem whose Key was seen in the
         * beforeView, but who has an updated Value. The only reason we make a distinction between
         * ADD and MODIFY is to simplify the MODIFY/REMOVE conflict check logic that occurs in a
         * later step.
         */
        final Set<RelationBeanItem> leftAdds = new HashSet<>();
        final Set<RelationBeanItem> leftModifies = new HashSet<>();
        for (final RelationBeanItem afterBeanItem : afterLeftBean)
        {
            final Optional<RelationBeanItem> beforeBeanItem = beforeBean
                    .getItemFor(afterBeanItem.getIdentifier(), afterBeanItem.getType());

            // ADD case, a brand new Key was added
            if (!beforeBeanItem.isPresent())
            {
                leftAdds.add(afterBeanItem);
            }
            else
            {
                // MODIFY case, the Key was found but its Value was changed
                if (!beforeBeanItem.get().getRole().equals(afterBeanItem.getRole()))
                {
                    leftModifies.add(afterBeanItem);
                }
                else
                {
                    /*
                     * NO CHANGE case, the Key/Value pair was unchanged between the beforeView and
                     * afterView
                     */
                }
            }
        }

        /*
         * Compute ADDs and MODIFYs for the right side. The same rules and logic from the left side
         * ADD/MODIFY logic apply. See above comment for more details.
         */
        final Set<RelationBeanItem> rightAdds = new HashSet<>();
        final Set<RelationBeanItem> rightModifies = new HashSet<>();
        for (final RelationBeanItem afterBeanItem : afterRightBean)
        {
            final Optional<RelationBeanItem> beforeBeanItem = beforeBean
                    .getItemFor(afterBeanItem.getIdentifier(), afterBeanItem.getType());

            // ADD case, a brand new Key was added
            if (!beforeBeanItem.isPresent())
            {
                rightAdds.add(afterBeanItem);
            }
            else
            {
                // MODIFY case, the Key was found but its Value was changed
                if (!beforeBeanItem.get().getRole().equals(afterBeanItem.getRole()))
                {
                    rightModifies.add(afterBeanItem);
                }
                else
                {
                    /*
                     * NO CHANGE case, the Key/Value pair was unchanged between the beforeView and
                     * afterView
                     */
                }
            }
        }

        /*
         * Compute REMOVEs for the left and right sides. A left REMOVE occurs when the beforeView
         * contains a Key that is not present in the afterLeft view. The same logic applies for
         * right REMOVEs.
         */
        final Set<RelationBeanItem> leftRemoves = new HashSet<>();
        final Set<RelationBeanItem> rightRemoves = new HashSet<>();
        for (final RelationBeanItem beforeBeanItem : beforeBean)
        {
            final Optional<RelationBeanItem> afterBeanLeftItem = afterLeftBean
                    .getItemFor(beforeBeanItem.getIdentifier(), beforeBeanItem.getType());
            final Optional<RelationBeanItem> afterBeanRightItem = afterRightBean
                    .getItemFor(beforeBeanItem.getIdentifier(), beforeBeanItem.getType());

            // REMOVE case for left side
            if (!afterBeanLeftItem.isPresent())
            {
                leftRemoves.add(beforeBeanItem);
            }

            // REMOVE case for right side
            if (!afterBeanRightItem.isPresent())
            {
                rightRemoves.add(beforeBeanItem);
            }
        }

        /*
         * Merge the left and right MODIFY sets into a single MODIFY set. This handles any duplicate
         * MODIFYs.
         */
        final Set<RelationBeanItem> mergedModifies = Sets.withSets(false, leftModifies,
                rightModifies);

        /*
         * Merge the left and right REMOVE sets into a single set. This handles any duplicate
         * REMOVEs.
         */
        final Set<RelationBeanItem> mergedRemoves = Sets.withSets(false, leftRemoves, rightRemoves);

        /*
         * Compute the set of MODIFY/REMOVE conflicts. This type of conflict occurs when one side
         * attempted to modify the Value of a Key/Value pair, while the other side attempted to
         * remove the Key. If the conflict set is non-empty, we must fail. We compute a
         * keyValueModifies map for use in this computation.
         */
        final Map<Tuple<Long, ItemType>, String> keyValueModifies = mergedModifies.stream()
                .collect(Collectors.toMap(item -> new Tuple<>(item.getIdentifier(), item.getType()),
                        item -> item.getRole()));
        for (final RelationBeanItem removedItem : mergedRemoves)
        {
            final Tuple<Long, ItemType> deletedKey = new Tuple<>(removedItem.getIdentifier(),
                    removedItem.getType());
            if (keyValueModifies.containsKey(deletedKey))
            {
                throw new CoreException(
                        "diffBasedRelationBeanMerger failed due to MODIFY/REMOVE conflict on ID/ItemType key: {}",
                        deletedKey.toString());
            }
        }

        /*
         * Compute the set of ADD/ADD conflicts. This type of conflict occurs when one side
         * attempted to add a Key k with Value v, while the other side attempted to add Key k with
         * Value v'. If the conflict set is non-empty, we must fail.
         */
        final Map<Tuple<Long, ItemType>, String> keyValueLeftAdds = leftAdds.stream()
                .collect(Collectors.toMap(item -> new Tuple<>(item.getIdentifier(), item.getType()),
                        item -> item.getRole()));
        final Map<Tuple<Long, ItemType>, String> keyValueRightAdds = rightAdds.stream()
                .collect(Collectors.toMap(item -> new Tuple<>(item.getIdentifier(), item.getType()),
                        item -> item.getRole()));
        for (final Tuple<Long, ItemType> sharedKey : com.google.common.collect.Sets
                .intersection(keyValueLeftAdds.keySet(), keyValueRightAdds.keySet()))
        {
            final String leftValue = keyValueLeftAdds.get(sharedKey);
            final String rightValue = keyValueRightAdds.get(sharedKey);

            if (!Objects.equals(leftValue, rightValue))
            {
                throw new CoreException(
                        "diffBasedRelationBeanMerger failed due to ADD/ADD conflict on Key {}: {} vs {}",
                        sharedKey.toString(), leftValue, rightValue);
            }
        }

        /*
         * Now merge the left and right ADD sets into a single set, handling any duplicates.
         */
        final Set<RelationBeanItem> mergedAdds = Sets.withSets(false, leftAdds, rightAdds);

        /*
         * Construct the result RelationBean based on the beforeView RelationBean. First, remove
         * anything that matches a member of the merged REMOVE set or MODIFY set. Then add anything
         * present in the merged ADD and MODIFY sets.
         */
        final RelationBean result = new RelationBean(beforeBean);
        for (final RelationBeanItem removedBeanItem : new MultiIterable<>(mergedRemoves,
                mergedModifies))
        {
            result.removeItem(removedBeanItem.getIdentifier(), removedBeanItem.getType());
        }
        for (final RelationBeanItem addedBeanItem : new MultiIterable<>(mergedAdds, mergedModifies))
        {
            result.addItem(addedBeanItem);
        }

        /*
         * TODO we do not handle merging the explcitlyExcluded set. Ideally, this set will be
         * removed. If it turns out we need it, then we will need to merge it properly here.
         */
        return result;
    };

    /*
     * Merge two differing Set<Long> created using ADDs and REMOVEs on a common ancestor. For
     * example, consider set A: [2, 3, 4] and set B: [1, 2, 3, 5]. Assume these were both based on
     * initial set I: [1, 2, 3, 4]. Neither A nor B make any changes that conflict with each other,
     * so we should be able to merge them into set C: [2, 3, 5].
     */
    static final TernaryOperator<Set<Long>> diffBasedLongSetMerger = (beforeSet, afterLeftSet,
            afterRightSet) ->
    {
        final Set<Long> removedFromLeftView = com.google.common.collect.Sets.difference(beforeSet,
                afterLeftSet);
        final Set<Long> removedFromRightView = com.google.common.collect.Sets.difference(beforeSet,
                afterRightSet);
        final Set<Long> addedToLeftView = com.google.common.collect.Sets.difference(afterLeftSet,
                beforeSet);
        final Set<Long> addedToRightView = com.google.common.collect.Sets.difference(afterRightSet,
                beforeSet);

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
        final Set<Long> result = new HashSet<>(beforeSet);
        result.removeAll(removedMerged);
        result.addAll(addedMerged);

        return result;
    };

    /*
     * This is essentially the same thing as diffBasedLongSetMerger. However, we construct a
     * SortedSet before returning the result. This is useful for members that require a sorted
     * property.
     */
    static final TernaryOperator<SortedSet<Long>> diffBasedLongSortedSetMerger = (beforeSet,
            afterLeftSet, afterRightSet) ->
    {
        return new TreeSet<>(diffBasedLongSetMerger.apply(beforeSet, afterLeftSet, afterRightSet));
    };

    /*
     * Merge two differing Map<String, String> created using ADD/MODIFYs and REMOVEs on a common
     * ancestor. For example, consider map A: [a=1, b=12, c=3] and map B: [a=1, b=2, c=3, d=4, e=5].
     * Assume these were both based on initial map I: [a=1, b=2, c=3, d=4]. Neither A nor B make any
     * changes that conflict with each other, so we should be able to merge them into map C: [a=1,
     * b=12, c=3, e=5].
     */
    static final TernaryOperator<Map<String, String>> diffBasedTagMerger = (beforeMap, afterLeftMap,
            afterRightMap) ->
    {
        /*
         * Simple key removal. A REMOVE looks like [a=1] -> []
         */
        final Set<String> keysRemovedFromLeftView = com.google.common.collect.Sets
                .difference(beforeMap.keySet(), afterLeftMap.keySet());
        final Set<String> keysRemovedFromRightView = com.google.common.collect.Sets
                .difference(beforeMap.keySet(), afterRightMap.keySet());

        /*
         * Consider the difference between an ADD [] -> [a=1] and a MODIFY [a=1] -> [a=2]. Below we
         * group key ADDs and MODIFYs together, since we operate on the entire key->value pair. In
         * light of this fact, ADDs and MODIFYs are effectively the same operation as far as merge
         * logic is concerned. From here on, we will only refer to ADD and never MODIFY.
         */
        final Map<String, String> addedToLeftView = com.google.common.collect.Sets
                .difference(afterLeftMap.entrySet(), beforeMap.entrySet()).stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final Map<String, String> addedToRightView = com.google.common.collect.Sets
                .difference(afterRightMap.entrySet(), beforeMap.entrySet()).stream()
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
                        "diffBasedTagMerger failed due to ADD/ADD collision(s) on keys: [{} -> {}] vs [{} -> {}]",
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
        final Map<String, String> result = new HashMap<>(beforeMap);
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
