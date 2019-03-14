package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;
import org.openstreetmap.atlas.utilities.function.TernaryOperator;

/**
 * A utility class to store the various merger operations utilized by {@link FeatureChange}.
 *
 * @author lcram
 */
public final class FeatureChangeMergers
{
    /**
     * A bean class to store the merged before and after members. This is useful as a return type
     * for the member merger, which needs to correctly merge the before and after entity view of
     * each {@link FeatureChange}.
     *
     * @author lcram
     * @param <M>
     *            the member type
     */
    public static class MergedMemberBean<M>
    {
        private final M beforeMemberMerged;
        private final M afterMemberMerged;

        public MergedMemberBean(final M before, final M after)
        {
            this.beforeMemberMerged = before;
            this.afterMemberMerged = after;
        }

        public M getMergedAfterMember()
        {
            return this.afterMemberMerged;
        }

        public M getMergedBeforeMember()
        {
            return this.beforeMemberMerged;
        }
    }

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

        return null;
    };

    /**
     * TODO fill in this doc comment.
     *
     * @param memberName
     * @param beforeEntityLeft
     * @param afterEntityLeft
     * @param beforeEntityRight
     * @param afterEntityRight
     * @param memberExtractor
     * @param simpleMergeStrategy
     * @param diffBasedMergeStrategy
     * @return a {@link MergedMemberBean} containing the merged beforeMember view and the merged
     *         afterMember view.
     */
    static <M> MergedMemberBean<M> mergeMember(final String memberName,
            final AtlasEntity beforeEntityLeft, final AtlasEntity afterEntityLeft,
            final AtlasEntity beforeEntityRight, final AtlasEntity afterEntityRight,
            final Function<AtlasEntity, M> memberExtractor,
            final BinaryOperator<M> simpleMergeStrategy,
            final TernaryOperator<M> diffBasedMergeStrategy)
    {
        final M beforeMemberResult;
        final M afterMemberResult;

        final M beforeMemberLeft = memberExtractor.apply(beforeEntityLeft);
        final M afterMemberLeft = memberExtractor.apply(afterEntityLeft);
        final M beforeMemberRight = memberExtractor.apply(beforeEntityRight);
        final M afterMemberRight = memberExtractor.apply(afterEntityRight);

        /*
         * In the case that both beforeMembers are present, we assert their equivalence before
         * continuing. It does not make sense to merge two FeatureChanges with conflicting
         * beforeMember views.
         */
        /*
         * TODO it is theoretically possible to allow for conflicting beforeMember views, and simply
         * fall back on the simpleMergeStrategy in this case. Is this something we want? I think it
         * makes more sense to just fail. It seems to me that the only time we would have
         * conflicting beforeMember views is when two corrections which are operating on the same
         * level should actually be on separate levels.
         */
        if (beforeMemberLeft != null && beforeMemberRight != null
                && !beforeMemberLeft.equals(beforeMemberRight))
        {
            throw new CoreException(
                    "Attempted merged failed for {}: beforeMembers did not match: {} vs {}",
                    memberName, beforeMemberLeft, beforeMemberRight);
        }
        /*
         * Properly merge the beforeMembers. If both are non-null, we arbitrarily take the left,
         * since we have already asserted they are equivalent. If one is null and one is not, then
         * we take the non-null. If both were null, then the result remains null.
         */
        if (beforeMemberLeft != null && beforeMemberRight != null)
        {
            beforeMemberResult = beforeMemberLeft;
        }
        else if (beforeMemberLeft != null)
        {
            beforeMemberResult = beforeMemberLeft;
        }
        else if (beforeMemberRight != null)
        {
            beforeMemberResult = beforeMemberRight;
        }
        else
        {
            beforeMemberResult = null;
        }

        /*
         * Properly merge the afterMembers. In the case that both afterMembers are present, then we
         * will need to resolve the afterMember merge using one of the supplied merge strategies.
         */
        if (afterMemberLeft != null && afterMemberRight != null)
        {
            /*
             * In the case that both afterMembers are non-null and equivalent, we arbitrarily pick
             * the left one.
             */
            if (afterMemberLeft.equals(afterMemberRight))
            {
                afterMemberResult = afterMemberLeft;
            }
            /*
             * Otherwise, we need to attempt one of the merging strategies.
             */
            else
            {
                /*
                 * If both beforeMembers are present (we have already asserted their equivalence so
                 * we just arbitrarily use beforeMemberLeft), we use the diffBased strategy if
                 * present.
                 */
                if (beforeMemberLeft != null && beforeMemberRight != null
                        && diffBasedMergeStrategy != null)
                {
                    try
                    {
                        afterMemberResult = diffBasedMergeStrategy.apply(beforeMemberLeft,
                                afterMemberLeft, afterMemberRight);
                    }
                    catch (final Exception exception)
                    {
                        throw new CoreException(
                                "Attempted merge failed for {} with beforeView: {}; afterView: {} vs {}",
                                memberName, beforeMemberLeft, afterMemberLeft, afterMemberRight,
                                exception);
                    }
                }
                /*
                 * If either/both beforeMember is not present, or we don't have a diffBased
                 * strategy, we try the simple strategy.
                 */
                else
                {
                    if (simpleMergeStrategy != null)
                    {
                        try
                        {
                            afterMemberResult = simpleMergeStrategy.apply(afterMemberLeft,
                                    afterMemberRight);
                        }
                        catch (final CoreException exception)
                        {
                            throw new CoreException("Attempted merge failed for {}: {} and {}",
                                    memberName, afterMemberLeft, afterMemberRight, exception);
                        }
                    }
                    else
                    {
                        throw new CoreException(
                                "Conflicting members and no simple merge strategy for {}; afterView: {} vs {}",
                                memberName, afterMemberLeft, afterMemberRight);
                    }
                }
            }
        }
        /*
         * If only one of the afterMembers is present, we just take whichever one is present.
         */
        else if (afterMemberLeft != null)
        {
            afterMemberResult = afterMemberLeft;
        }
        else if (afterMemberRight != null)
        {
            afterMemberResult = afterMemberRight;
        }
        /*
         * If neither afterMember is present, then just move on.
         */
        else
        {
            afterMemberResult = null;
        }

        return new MergedMemberBean<>(beforeMemberResult, afterMemberResult);
    }

    static <M> M mergeMember_OldStrategy(final String memberName, final AtlasEntity afterEntityLeft,
            final AtlasEntity afterEntityRight, final Function<AtlasEntity, M> memberExtractor,
            final BinaryOperator<M> memberMerger)
    {
        final M result;
        final M afterMemberLeft = memberExtractor.apply(afterEntityLeft);
        final M afterMemberRight = memberExtractor.apply(afterEntityRight);
        if (afterMemberLeft != null && afterMemberRight != null)
        {
            // Both are not null, merge evaluated
            if (afterMemberLeft.equals(afterMemberRight))
            {
                // They are equal, arbitrarily pick one.
                result = afterMemberLeft;
            }
            else if (memberMerger != null)
            {
                // They are unequal, but we can attempt a merge
                try
                {
                    result = memberMerger.apply(afterMemberLeft, afterMemberRight);
                }
                catch (final CoreException exception)
                {
                    throw new CoreException("Attempted merge failed for {}: {} and {}", memberName,
                            afterMemberLeft, afterMemberRight, exception);
                }
            }
            else
            {
                // They are unequal and we do not have a tool to merge them.
                throw new CoreException("Conflicting members, no merge option for {}: {} and {}",
                        memberName, afterMemberLeft, afterMemberRight);
            }
        }
        else
        {
            // One is not null, or both are.
            if (afterMemberLeft != null)
            {
                result = afterMemberLeft;
            }
            else
            {
                result = afterMemberRight;
            }
        }
        return result;
    }

    private FeatureChangeMergers()
    {

    }
}
