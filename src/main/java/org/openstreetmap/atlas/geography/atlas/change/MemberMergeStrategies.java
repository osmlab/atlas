package org.openstreetmap.atlas.geography.atlas.change;

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
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
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

    /*
     * TODO This is deprecated because it relies on the explicityExcluded sets in RelationBean and
     * RelationMemberList. Ideally, we will remove this redundant state from
     * RelationBean/RelationMemberList. This will affect the non-diff-based merge logic.
     */
    @Deprecated
    static final BinaryOperator<RelationBean> simpleRelationBeanMerger = RelationBean::merge;

    static final TernaryOperator<Long> diffBasedLongMerger = (beforeLong, afterLongLeft,
            afterLongRight) ->
    {
        return (Long) getDiffBasedMutuallyExclusiveMerger().apply(beforeLong, afterLongLeft,
                afterLongRight);
    };

    static final TernaryOperator<Location> diffBasedLocationMerger = (beforeLocation,
            afterLocationLeft, afterLocationRight) ->
    {
        return (Location) getDiffBasedMutuallyExclusiveMerger().apply(beforeLocation,
                afterLocationLeft, afterLocationRight);
    };

    static final TernaryOperator<PolyLine> diffBasedPolyLineMerger = (beforePolyLine,
            afterPolyLineLeft, afterPolyLineRight) ->
    {
        return (PolyLine) getDiffBasedMutuallyExclusiveMerger().apply(beforePolyLine,
                afterPolyLineLeft, afterPolyLineRight);
    };

    static final TernaryOperator<Polygon> diffBasedPolygonMerger = (beforePolygon, afterPolygonLeft,
            afterPolygonRight) ->
    {
        return (Polygon) getDiffBasedMutuallyExclusiveMerger().apply(beforePolygon,
                afterPolygonLeft, afterPolygonRight);
    };

    static final TernaryOperator<RelationBean> diffBasedRelationBeanMerger = (beforeBean,
            afterLeftBean, afterRightBean) ->
    {
        final Map<RelationBeanItem, Integer> beforeBeanMap = beforeBean.asMap();
        final Map<RelationBeanItem, Integer> afterLeftBeanMap = afterLeftBean.asMap();
        final Map<RelationBeanItem, Integer> afterRightBeanMap = afterRightBean.asMap();

        /*
         * Compute the difference set between the beforeView and the afterViews (which is equivalent
         * to the keys removed from the after views). We filter any entries that have a count <= 0,
         * since this corresponds to unchanged/added keys in the afterView.
         */
        final Map<RelationBeanItem, Integer> removedFromLeftView = computeMapDifferenceCounts(
                beforeBeanMap, afterLeftBeanMap).entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final Map<RelationBeanItem, Integer> removedFromRightView = computeMapDifferenceCounts(
                beforeBeanMap, afterRightBeanMap).entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        /*
         * Compute the difference set between the afterViews and the beforeView (which is equivalent
         * to the keys added to the after views). We filter any entries that have a count <= 0,
         * since this corresponds to unchanged/removed keys in the afterView.
         */
        final Map<RelationBeanItem, Integer> addedToLeftView = computeMapDifferenceCounts(
                afterLeftBeanMap, beforeBeanMap).entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final Map<RelationBeanItem, Integer> addedToRightView = computeMapDifferenceCounts(
                afterRightBeanMap, beforeBeanMap).entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        /*
         * Check for REMOVE/REMOVE conflicts. A REMOVE/REMOVE conflict occurs when
         * removedFromLeftView and removedFromRightView share a key, but the values differ.
         */
        for (final Map.Entry<RelationBeanItem, Integer> removedFromLeftEntry : removedFromLeftView
                .entrySet())
        {
            final RelationBeanItem leftKey = removedFromLeftEntry.getKey();
            final Integer leftValue = removedFromLeftEntry.getValue();
            final Integer rightValue = removedFromRightView.get(leftKey);
            if (rightValue != null && !leftValue.equals(rightValue))
            {
                throw new CoreException(
                        "diffBasedRelationBeanMerger failed due to REMOVE/REMOVE conflict on key: [{}]: beforeValue absolute count was {} but removedLeft/Right diff counts conflict [{} vs {}]",
                        leftKey, beforeBeanMap.get(leftKey), leftValue, rightValue);
            }
        }

        /*
         * Check for ADD/REMOVE conflicts. An ADD/REMOVE conflict occurs when the addedToLeftView
         * and removedFromRightView maps share a key (or the addedToRightView and
         * removedFromLeftView share a key).
         */
        final Set<RelationBeanItem> addedLeftRemovedRightConflicts = com.google.common.collect.Sets
                .intersection(addedToLeftView.keySet(), removedFromRightView.keySet());
        if (!addedLeftRemovedRightConflicts.isEmpty())
        {
            throw new CoreException(
                    "diffBasedRelationBeanMerger failed due to ADD/REMOVE conflict(s) on key(s): {}",
                    addedLeftRemovedRightConflicts);
        }
        final Set<RelationBeanItem> addedRightRemovedLeftConflicts = com.google.common.collect.Sets
                .intersection(addedToRightView.keySet(), removedFromLeftView.keySet());
        if (!addedRightRemovedLeftConflicts.isEmpty())
        {
            throw new CoreException(
                    "diffBasedRelationBeanMerger failed due to ADD/REMOVE conflict(s) on key(s): {}",
                    addedRightRemovedLeftConflicts);
        }

        /*
         * Check for ADD/ADD conflicts. A ADD/ADD conflict occurs when addedToLeftView and
         * addedToRightView share a key, but the values differ.
         */
        for (final Map.Entry<RelationBeanItem, Integer> addedToLeftEntry : addedToLeftView
                .entrySet())
        {
            final RelationBeanItem leftKey = addedToLeftEntry.getKey();
            final Integer leftValue = addedToLeftEntry.getValue();
            final Integer rightValue = addedToRightView.get(leftKey);
            if (rightValue != null && !leftValue.equals(rightValue))
            {
                throw new CoreException(
                        "diffBasedRelationBeanMerger failed due to ADD/ADD conflict on key: [{}]: beforeValue absolute count was {} but addedLeft/Right diff counts conflict [{} vs {}]",
                        leftKey,
                        beforeBeanMap.get(leftKey) != null ? beforeBeanMap.get(leftKey) : 0,
                        leftValue, rightValue);
            }
        }

        /*
         * Since there were no ADD/REMOVE or REMOVE/REMOVE conflicts, we can safely merge the
         * REMOVED maps.
         */
        final Map<RelationBeanItem, Integer> removedMergedView = new HashMap<>();
        removedFromLeftView.entrySet().stream()
                .forEach(entry -> removedMergedView.put(entry.getKey(), entry.getValue()));
        removedFromRightView.entrySet().stream()
                .forEach(entry -> removedMergedView.put(entry.getKey(), entry.getValue()));

        /*
         * Since there were no ADD/REMOVE or ADD/ADD conflicts. we can safely merge the ADD maps.
         */
        final Map<RelationBeanItem, Integer> addedMergedView = new HashMap<>();
        addedToLeftView.entrySet().stream()
                .forEach(entry -> addedMergedView.put(entry.getKey(), entry.getValue()));
        addedToRightView.entrySet().stream()
                .forEach(entry -> addedMergedView.put(entry.getKey(), entry.getValue()));

        /*
         * Construct the final product using our merged REMOVE and ADD views. First we created a
         * resultMap with merged counts. We operate on the beforeView - subtract count for each key
         * in removedMergedView, add count for each key in addedMergedView. Then, using the
         * resultMap, we can construct the result bean with the proper key counts.
         */
        final Map<RelationBeanItem, Integer> resultMap = new HashMap<>(beforeBeanMap);
        for (final Map.Entry<RelationBeanItem, Integer> removedEntry : removedMergedView.entrySet())
        {
            final RelationBeanItem removedKey = removedEntry.getKey();
            final Integer removedCount = removedEntry.getValue();
            final Integer beforeValue = beforeBeanMap.get(removedKey);
            final Integer newValue = beforeValue - removedCount;
            resultMap.put(removedKey, newValue);
        }
        for (final Map.Entry<RelationBeanItem, Integer> addedEntry : addedMergedView.entrySet())
        {
            final RelationBeanItem addedKey = addedEntry.getKey();
            final Integer addedCount = addedEntry.getValue();
            final Integer beforeValue = beforeBeanMap.get(addedKey);
            final Integer newValue;

            /*
             * The beforeValue will be null if the added key is brand new. In that case, we just
             * need to set the count. Otherwise, we add the count to the before value.
             */
            if (beforeValue == null)
            {
                newValue = 0 + addedCount;
            }
            else
            {
                newValue = beforeValue + addedCount;
            }
            resultMap.put(addedKey, newValue);
        }

        final RelationBean resultBean = new RelationBean();
        for (final Map.Entry<RelationBeanItem, Integer> resultEntry : resultMap.entrySet())
        {
            final RelationBeanItem resultKey = resultEntry.getKey();
            final Integer resultCount = resultEntry.getValue();
            for (int count = 0; count < resultCount; count++)
            {
                resultBean.addItem(resultKey);
            }
        }
        /*
         * TODO note we currently do not properly merge the explicitlyExcluded sets. The plan is to
         * remove these sets. If we cannot find a way to do so, then we must merge them here.
         */
        return resultBean;
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
         * Check to see if any of the shared ADD keys generate an ADD/ADD conflict. An ADD/ADD
         * conflict is when the same key maps to two different values.
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
                        "diffBasedTagMerger failed due to ADD/ADD conflict on keys: [{} -> {}] vs [{} -> {}]",
                        sharedKey, leftValue, sharedKey, rightValue);
            }
        }

        /*
         * Now, check for any ADD/REMOVE conflicts. An ADD/REMOVE conflict is when one view of the
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
                    "diffBasedTagMerger failed due to ADD/REMOVE conflict(s) on key(s): {}",
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

    /**
     * Compute the difference counts between two Map<T, Integer>. The difference is computing by
     * subtracting the after Integer value from the before Integer value. If the before key is not
     * present in the after map, then we use the before Integer value for that key. Any after keys
     * not present in the before map are ignored.
     *
     * @param before
     *            the before view of the map
     * @param after
     *            the after view of the map
     * @return the difference map
     */
    private static <T> Map<T, Integer> computeMapDifferenceCounts(final Map<T, Integer> before,
            final Map<T, Integer> after)
    {
        final Map<T, Integer> result = new HashMap<>();

        for (final Map.Entry<T, Integer> beforeEntry : before.entrySet())
        {
            final T beforeKey = beforeEntry.getKey();
            final Integer beforeCount = beforeEntry.getValue();
            final Integer afterCount = after.get(beforeKey);
            if (afterCount != null)
            {
                result.put(beforeKey, beforeCount - afterCount);
            }
            else
            {
                result.put(beforeKey, beforeCount);
            }
        }
        return result;
    }

    /**
     * Returns a TernaryOperator that acts as a diff based, mutually exclusive chooser. The operator
     * can successfully merge two afterViews if: 1) both afterViews match OR 2) the afterViews are
     * mismatched, but one of the afterViews matches the beforeView. In case 2) the merger will
     * select that afterView that differs from the beforeView. In any other case, the operator will
     * fail with an ADD/ADD conflict.
     *
     * @return the operator
     */
    private static <T> TernaryOperator<T> getDiffBasedMutuallyExclusiveMerger()
    {
        return (beforeView, afterViewLeft, afterViewRight) ->
        {
            /*
             * If the afterViews are equivalent, arbitrarily return one of them.
             */
            if (afterViewLeft.equals(afterViewRight))
            {
                return afterViewLeft;
            }

            /*
             * afterViewLeft and afterViewRight were not equivalent. If one of them matches the
             * beforeView, return the opposing one.
             */
            if (afterViewLeft.equals(beforeView))
            {
                return afterViewRight;
            }
            if (afterViewRight.equals(beforeView))
            {
                return afterViewLeft;
            }

            /*
             * If we get here, we have an ADD/ADD conflict.
             */
            throw new CoreException(
                    "diffBasedMutuallyExclusiveMerger failed due to ADD/ADD conflict: beforeView was {} but afterViews were [{} vs {}]",
                    beforeView, afterViewLeft, afterViewRight);
        };
    }

    private MemberMergeStrategies()
    {
    }
}
