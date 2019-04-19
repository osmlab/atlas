package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.utilities.function.QuaternaryOperator;
import org.openstreetmap.atlas.utilities.function.TernaryOperator;

/**
 * This class encapsulates the logic and configuration for {@link CompleteEntity} member merging in
 * the context of {@link FeatureChange} merges.
 *
 * @author lcram
 * @param <M>
 *            the type of the member this {@link MemberMerger} will be merging
 */
public final class MemberMerger<M>
{
    /**
     * A builder class for {@link MemberMerger}.
     *
     * @author lcram
     * @param <M>
     *            the type of the member this {@link MemberMerger} will be merging
     */
    public static class Builder<M>
    {
        private String memberName;
        private AtlasEntity beforeEntityLeft;
        private AtlasEntity afterEntityLeft;
        private AtlasEntity beforeEntityRight;
        private AtlasEntity afterEntityRight;
        private Function<AtlasEntity, M> memberExtractor;
        private BinaryOperator<M> afterViewNoBeforeViewMerger;
        private TernaryOperator<M> afterViewConsistentBeforeViewMerger;
        private QuaternaryOperator<M> afterViewConflictingBeforeViewMerger;
        private BinaryOperator<M> beforeViewMerger;
        private boolean useHackForMergingConflictingConnectedEdgeSetBeforeViews;

        private Optional<CompleteNode> leftNode;
        private Optional<CompleteNode> rightNode;

        public MemberMerger<M> build()
        {
            assertRequiredFieldsNonNull();

            final MemberMerger<M> merger = new MemberMerger<>();
            merger.memberName = this.memberName;
            merger.beforeEntityLeft = this.beforeEntityLeft;
            merger.afterEntityLeft = this.afterEntityLeft;
            merger.beforeEntityRight = this.beforeEntityRight;
            merger.afterEntityRight = this.afterEntityRight;
            merger.memberExtractor = this.memberExtractor;
            merger.afterViewNoBeforeViewMerger = this.afterViewNoBeforeViewMerger;
            merger.afterViewConsistentBeforeViewMerger = this.afterViewConsistentBeforeViewMerger;
            merger.afterViewConflictingBeforeViewMerger = this.afterViewConflictingBeforeViewMerger;
            merger.beforeViewMerger = this.beforeViewMerger;
            merger.useHackForMergingConflictingConnectedEdgeSetBeforeViews = this.useHackForMergingConflictingConnectedEdgeSetBeforeViews;
            merger.leftNode = this.leftNode;
            merger.rightNode = this.rightNode;

            return merger;
        }

        public Builder<M> useHackForMergingConflictingConnectedEdgeSetBeforeViews(
                final boolean useHack, final CompleteNode left, final CompleteNode right)
        {
            this.useHackForMergingConflictingConnectedEdgeSetBeforeViews = useHack;
            this.leftNode = Optional.ofNullable(left);
            this.rightNode = Optional.ofNullable(right);
            return this;
        }

        public Builder<M> withAfterEntityLeft(final AtlasEntity afterEntityLeft)
        {
            this.afterEntityLeft = afterEntityLeft;
            return this;
        }

        public Builder<M> withAfterEntityRight(final AtlasEntity afterEntityRight)
        {
            this.afterEntityRight = afterEntityRight;
            return this;
        }

        public Builder<M> withAfterViewConflictingBeforeViewMerger(
                final QuaternaryOperator<M> afterViewConflictingBeforeViewMerger)
        {
            this.afterViewConflictingBeforeViewMerger = afterViewConflictingBeforeViewMerger;
            return this;
        }

        public Builder<M> withAfterViewConsistentBeforeViewMerger(
                final TernaryOperator<M> afterViewConsistentBeforeViewMerger)
        {
            this.afterViewConsistentBeforeViewMerger = afterViewConsistentBeforeViewMerger;
            return this;
        }

        public Builder<M> withAfterViewNoBeforeMerger(
                final BinaryOperator<M> afterViewNoBeforeMerger)
        {
            this.afterViewNoBeforeViewMerger = afterViewNoBeforeMerger;
            return this;
        }

        public Builder<M> withBeforeEntityLeft(final AtlasEntity beforeEntityLeft)
        {
            this.beforeEntityLeft = beforeEntityLeft;
            return this;
        }

        public Builder<M> withBeforeEntityRight(final AtlasEntity beforeEntityRight)
        {
            this.beforeEntityRight = beforeEntityRight;
            return this;
        }

        public Builder<M> withBeforeViewMerger(final BinaryOperator<M> beforeViewMerger)
        {
            this.beforeViewMerger = beforeViewMerger;
            return this;
        }

        public Builder<M> withMemberExtractor(final Function<AtlasEntity, M> memberExtractor)
        {
            this.memberExtractor = memberExtractor;
            return this;
        }

        public Builder<M> withMemberName(final String memberName)
        {
            this.memberName = memberName;
            return this;
        }

        private void assertRequiredFieldsNonNull()
        {
            if (this.memberName == null)
            {
                throw new CoreException("Required field \'memberName\' was unset");
            }

            if (this.afterEntityLeft == null)
            {
                throw new CoreException("Required field \'afterEntityLeft\' was unset");
            }

            if (this.afterEntityRight == null)
            {
                throw new CoreException("Required field \'afterEntityRight\' was unset");
            }

            if (this.beforeEntityLeft != null && this.beforeEntityRight == null
                    || this.beforeEntityLeft == null && this.beforeEntityRight != null)
            {
                throw new CoreException("Both \'beforeEntity\' fields must either be set or null");
            }
        }
    }

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

    private String memberName;
    private AtlasEntity beforeEntityLeft;
    private AtlasEntity afterEntityLeft;
    private AtlasEntity beforeEntityRight;
    private AtlasEntity afterEntityRight;
    private Function<AtlasEntity, M> memberExtractor;
    private BinaryOperator<M> afterViewNoBeforeViewMerger;
    private TernaryOperator<M> afterViewConsistentBeforeViewMerger;
    private QuaternaryOperator<M> afterViewConflictingBeforeViewMerger;
    private BinaryOperator<M> beforeViewMerger;
    private boolean useHackForMergingConflictingConnectedEdgeSetBeforeViews;

    private Optional<CompleteNode> leftNode;
    private Optional<CompleteNode> rightNode;

    private MemberMerger()
    {

    }

    /**
     * Merge some feature member using a left and right before/after view.
     *
     * @return a {@link MergedMemberBean} containing the merged beforeMember view and the merged
     *         afterMember view
     */
    public MergedMemberBean<M> mergeMember()
    {
        final M beforeMemberResult;
        final M afterMemberResult;

        final M beforeMemberLeft = this.beforeEntityLeft == null ? null
                : this.memberExtractor.apply(this.beforeEntityLeft);
        final M afterMemberLeft = this.afterEntityLeft == null ? null
                : this.memberExtractor.apply(this.afterEntityLeft);
        final M beforeMemberRight = this.beforeEntityRight == null ? null
                : this.memberExtractor.apply(this.beforeEntityRight);
        final M afterMemberRight = this.afterEntityRight == null ? null
                : this.memberExtractor.apply(this.afterEntityRight);

        /*
         * In the case that both beforeMembers are present, we check their equivalence before
         * continuing. If they are not equivalent, then we try to use our special beforeView
         * conflict resolution merge logic. Otherwise, we can continue as normal.
         */
        if (beforeMemberLeft != null && beforeMemberRight != null
                && !beforeMemberLeft.equals(beforeMemberRight))
        {
            /*
             * In the case that we are merging the inEdges or outEdges members of Node, we perform a
             * different merge logic. The in/outEdge sets have a possibility of beforeView
             * conflicts, and since we are unable to attach additional explicitlyExcluded state
             * directly to a set, we cannot use the same logic utilized for merging other members
             * with conflicting beforeViews.
             */
            if (this.useHackForMergingConflictingConnectedEdgeSetBeforeViews)
            {
                return mergeMemberHackForConflictingConnectedEdgeSetBeforeViews(beforeMemberLeft,
                        afterMemberLeft, beforeMemberRight, afterMemberRight);
            }
            return mergeMemberWithConflictingBeforeViews(beforeMemberLeft, afterMemberLeft,
                    beforeMemberRight, afterMemberRight);
        }

        beforeMemberResult = chooseNonNullMemberIfPossible(beforeMemberLeft, beforeMemberRight);

        /*
         * In the case that both afterMembers are present, then we will need to resolve the
         * afterMember merge using one of the supplied merge strategies. In this case, beforeMembers
         * that are either consistent or both null - so we can use the merged beforeMemberResult.
         */
        if (afterMemberLeft != null && afterMemberRight != null)
        {
            return mergeMembersWithConsistentBeforeViews(beforeMemberResult, afterMemberLeft,
                    afterMemberRight);
        }

        /*
         * If only one of the afterMembers is present, we just take whichever one is present.
         */
        if (afterMemberLeft != null)
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

    /**
     * Choose the non-null member between two choices if possible. If both the left and right
     * members are non-null, then this method will arbitrarily select one of them. Due to this
     * condition, you may see unexpected results if you pass two non-null members that are unequal.
     *
     * @param memberLeft
     *            the left side before view of the member
     * @param memberRight
     *            the right side before view of the member
     * @return The non-null beforeMember among the two if present. Otherwise, returns {@code null};
     */
    private M chooseNonNullMemberIfPossible(final M memberLeft, final M memberRight)
    {
        /*
         * Properly merge the members. If both are non-null, we arbitrarily take the left (since
         * this method makes no guarantee on which side it will select when both are non-null). If
         * one is null and one is not, then we take the non-null. If both were null, then the result
         * remains null.
         */
        if (memberLeft != null && memberRight != null)
        {
            return memberLeft;
        }
        else if (memberLeft != null)
        {
            return memberLeft;
        }
        else if (memberRight != null)
        {
            return memberRight;
        }
        else
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private MergedMemberBean<M> mergeMemberHackForConflictingConnectedEdgeSetBeforeViews(
            final M beforeMemberLeft, final M afterMemberLeft, final M beforeMemberRight,
            final M afterMemberRight)
    {
        final M beforeMemberResult;
        final M afterMemberResult;

        final Set<Long> explicitlyExcludedLeft;
        final Set<Long> explicitlyExcludedRight;

        if (!this.leftNode.isPresent())
        {
            throw new CoreException(
                    "Attempted merge failed for {}: tried to use hackForConflictingConnectedEdgeSet but was missing leftNode",
                    this.memberName);
        }
        if (!this.rightNode.isPresent())
        {
            throw new CoreException(
                    "Attempted merge failed for {}: tried to use hackForConflictingConnectedEdgeSet but was missing rightNode",
                    this.memberName);
        }

        if (FeatureChangeMergingHelpers.IN_EDGE_IDENTIFIERS_FIELD.equals(this.memberName))
        {
            explicitlyExcludedLeft = this.leftNode.get().explicitlyExcludedInEdgeIdentifiers();
            explicitlyExcludedRight = this.rightNode.get().explicitlyExcludedInEdgeIdentifiers();
        }
        else if (FeatureChangeMergingHelpers.OUT_EDGE_IDENTIFIERS_FIELD.equals(this.memberName))
        {
            explicitlyExcludedLeft = this.leftNode.get().explicitlyExcludedOutEdgeIdentifiers();
            explicitlyExcludedRight = this.rightNode.get().explicitlyExcludedOutEdgeIdentifiers();
        }
        else
        {
            throw new CoreException(
                    "Attempted merge failed for {}: hackForConflictingConnectedEdgeSet is not a valid strategy for {}",
                    this.memberName, this.memberName);
        }

        if (this.beforeViewMerger == null)
        {
            throw new CoreException(
                    "Conflicting beforeMembers {} and no beforeView merge strategy was provided; beforeView: {} vs {}",
                    this.memberName, beforeMemberLeft, beforeMemberRight);
        }

        try
        {
            beforeMemberResult = this.beforeViewMerger.apply(beforeMemberLeft, beforeMemberRight);
        }
        catch (final Exception exception)
        {
            throw new CoreException(
                    "Attempted beforeView merge strategy failed for {} with beforeView: {} vs {}",
                    this.memberName, beforeMemberLeft, beforeMemberRight, exception);
        }

        /*
         * Here we hardcode the application of the SenaryOperator node connected edge set merger. We
         * can cast back and forth between M and Set<Long> here, since we know that M is of type
         * Set<Long> based on the constraints imposed when calling this function.
         */
        try
        {
            afterMemberResult = (M) MemberMergeStrategies.conflictingBeforeViewSetMerger.apply(
                    (Set<Long>) beforeMemberLeft, (Set<Long>) afterMemberLeft,
                    explicitlyExcludedLeft, (Set<Long>) beforeMemberRight,
                    (Set<Long>) afterMemberRight, explicitlyExcludedRight);
        }
        catch (final Exception exception)
        {
            throw new CoreException(
                    "Tried merge strategy for hackForConflictingConnectedEdgeSet, but it failed for {}"
                            + "\nbeforeView: {} vs {};\nafterView: {} vs {}",
                    this.memberName, beforeMemberLeft, beforeMemberRight, afterMemberLeft,
                    afterMemberRight, exception);
        }

        return new MergedMemberBean<>(beforeMemberResult, afterMemberResult);
    }

    /**
     * Merge a member that has consistent (possibly null) beforeViews.
     *
     * @param beforeMemberResult
     *            the pre-merged before member view
     * @param afterMemberLeft
     *            the left side after view of the member
     * @param afterMemberRight
     *            the right side after view of the member
     * @return a {@link MergedMemberBean} containing the merged beforeMember view and the merged
     *         afterMember view
     */
    private MergedMemberBean<M> mergeMembersWithConsistentBeforeViews(final M beforeMemberResult,
            final M afterMemberLeft, final M afterMemberRight)
    {
        final M afterMemberResult;

        /*
         * In the case that both afterMembers are non-null and equivalent, we arbitrarily pick the
         * left one.
         */
        if (afterMemberLeft.equals(afterMemberRight))
        {
            return new MergedMemberBean<>(beforeMemberResult, afterMemberLeft);
        }

        /*
         * If both beforeMembers are present (we have already asserted their equivalence so we just
         * arbitrarily use beforeMemberLeft), we use the diffBased strategy if present.
         */
        if (beforeMemberResult != null && this.afterViewConsistentBeforeViewMerger != null)
        {
            try
            {
                afterMemberResult = this.afterViewConsistentBeforeViewMerger
                        .apply(beforeMemberResult, afterMemberLeft, afterMemberRight);
            }
            catch (final Exception exception)
            {
                throw new CoreException(
                        "Attempted afterViewConsistentBeforeMerge failed for {} with beforeView: {}; afterView: {} vs {}",
                        this.memberName, beforeMemberResult, afterMemberLeft, afterMemberRight,
                        exception);
            }
        }
        /*
         * If the beforeMember is not present, or we don't have a diffBased strategy, we try the
         * simple strategy.
         */
        else if (this.afterViewNoBeforeViewMerger != null)
        {
            try
            {
                afterMemberResult = this.afterViewNoBeforeViewMerger.apply(afterMemberLeft,
                        afterMemberRight);
            }
            catch (final CoreException exception)
            {
                throw new CoreException(
                        "Attempted afterViewNoBeforeMerge failed for {}; afterView: {} vs {}",
                        this.memberName, afterMemberLeft, afterMemberRight, exception);
            }
        }
        /*
         * If there was no simple strategy, we have to fail.
         */
        else
        {
            throw new CoreException(
                    "Conflicting members and no merge strategy for {}; afterView: {} vs {}",
                    this.memberName, afterMemberLeft, afterMemberRight);
        }

        return new MergedMemberBean<>(beforeMemberResult, afterMemberResult);
    }

    /**
     * Merge a member that has conflicting beforeViews. This can happen occasionally with
     * {@link RelationBean}s and the in/out {@link Edge} identifier sets in {@link Node}, since
     * these may be inconsistent across shards.
     *
     * @param beforeMemberLeft
     *            the left side before view of the member
     * @param afterMemberLeft
     *            the left side after view of the member
     * @param beforeMemberRight
     *            the right side before view of the member
     * @param afterMemberRight
     *            the right side after view of the member
     * @return a {@link MergedMemberBean} containing the merged beforeMember view and the merged
     *         afterMember view
     */
    private MergedMemberBean<M> mergeMemberWithConflictingBeforeViews(final M beforeMemberLeft,
            final M afterMemberLeft, final M beforeMemberRight, final M afterMemberRight)
    {
        final M beforeMemberResult;
        final M afterMemberResult;

        if (this.afterViewConflictingBeforeViewMerger == null)
        {
            throw new CoreException(
                    "Conflicting beforeMembers {} and no afterView merge strategy capable of handling"
                            + " conflicting beforeViews was provided; beforeView: {} vs {}",
                    this.memberName, beforeMemberLeft, beforeMemberRight);
        }

        if (this.beforeViewMerger == null)
        {
            throw new CoreException(
                    "Conflicting beforeMembers {} and no beforeView merge strategy was provided; beforeView: {} vs {}",
                    this.memberName, beforeMemberLeft, beforeMemberRight);
        }

        try
        {
            beforeMemberResult = this.beforeViewMerger.apply(beforeMemberLeft, beforeMemberRight);
        }
        catch (final Exception exception)
        {
            throw new CoreException(
                    "Attempted beforeView merge strategy failed for {} with beforeView: {} vs {}",
                    this.memberName, beforeMemberLeft, beforeMemberRight, exception);
        }

        try
        {
            afterMemberResult = this.afterViewConflictingBeforeViewMerger.apply(beforeMemberLeft,
                    afterMemberLeft, beforeMemberRight, afterMemberRight);
        }
        catch (final Exception exception)
        {
            throw new CoreException(
                    "Tried merge strategy for handling conflicting beforeViews. but it failed for {}"
                            + "\nbeforeView: {} vs {};\nafterView: {} vs {}",
                    this.memberName, beforeMemberLeft, beforeMemberRight, afterMemberLeft,
                    afterMemberRight, exception);
        }

        return new MergedMemberBean<>(beforeMemberResult, afterMemberResult);
    }
}
