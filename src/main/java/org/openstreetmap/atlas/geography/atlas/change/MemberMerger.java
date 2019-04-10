package org.openstreetmap.atlas.geography.atlas.change;

import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.utilities.function.QuaternaryOperator;
import org.openstreetmap.atlas.utilities.function.TernaryOperator;

/**
 * @author lcram
 * @param <M>
 */
public class MemberMerger<M>
{
    /**
     * @author lcram
     * @param <M>
     */
    public static class Builder<M>
    {
        private String memberName;
        private AtlasEntity beforeEntityLeft;
        private AtlasEntity afterEntityLeft;
        private AtlasEntity beforeEntityRight;
        private AtlasEntity afterEntityRight;
        private Function<AtlasEntity, M> memberExtractor;
        private BinaryOperator<M> afterViewNoBeforeMerger;
        private TernaryOperator<M> afterViewConsistentBeforeMerger;
        private QuaternaryOperator<M> afterViewConflictingBeforeMerger;
        private TernaryOperator<M> beforeViewMerger;

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
            merger.afterViewNoBeforeMerger = this.afterViewNoBeforeMerger;
            merger.afterViewConsistentBeforeMerger = this.afterViewConsistentBeforeMerger;
            merger.afterViewConflictingBeforeMerger = this.afterViewConflictingBeforeMerger;
            merger.beforeViewMerger = this.beforeViewMerger;

            return merger;
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

        public Builder<M> withAfterViewConflictingBeforeMerger(
                final QuaternaryOperator<M> afterViewConflictingBeforeMerger)
        {
            this.afterViewConflictingBeforeMerger = afterViewConflictingBeforeMerger;
            return this;
        }

        public Builder<M> withAfterViewConsistentBeforeMerger(
                final TernaryOperator<M> afterViewConsistentBeforeMerger)
        {
            this.afterViewConsistentBeforeMerger = afterViewConsistentBeforeMerger;
            return this;
        }

        public Builder<M> withAfterViewNoBeforeMerger(
                final BinaryOperator<M> afterViewNoBeforeMerger)
        {
            this.afterViewNoBeforeMerger = afterViewNoBeforeMerger;
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

        public Builder<M> withBeforeViewMerger(final TernaryOperator<M> beforeViewMerger)
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
    private BinaryOperator<M> afterViewNoBeforeMerger;
    private TernaryOperator<M> afterViewConsistentBeforeMerger;
    private QuaternaryOperator<M> afterViewConflictingBeforeMerger;
    private TernaryOperator<M> beforeViewMerger;

    private MemberMerger()
    {

    }

    public MergedMemberBean<M> mergeMember()
    {

    }
}
