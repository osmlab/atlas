package org.openstreetmap.atlas.geography.atlas.change.feature;

/**
 * A bean class to store the merged before and after members. This is useful as a return type for
 * the member merger, which needs to correctly merge the before and after entity view of each
 * {@link FeatureChange}.
 *
 * @author lcram
 * @param <M>
 *            the member type
 */
public class MergedMemberBean<M>
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
