package org.openstreetmap.atlas.geography.atlas.change.merge;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.function.TernaryOperator;

/**
 * A utility class for the various {@link FeatureChange} merge helper functions.
 *
 * @author lcram
 */
public final class FeatureChangeMergeHelpers
{
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
         * makes more sense to just fail.
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

    public static FeatureChange mergeTwoADDFeatureChanges(final FeatureChange left,
            final FeatureChange right)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity beforeEntityRight = right.getBeforeView();
        final AtlasEntity afterEntityRight = right.getAfterView();
        final MergedMemberBean<Map<String, String>> mergedTagsBean = mergeMember("tags",
                beforeEntityLeft, afterEntityLeft, beforeEntityRight, afterEntityRight,
                Taggable::getTags, MemberMergeStrategies.tagMerger,
                MemberMergeStrategies.diffBasedTagMerger);
        final Set<Long> mergedParentRelations = mergeMember_OldStrategy("parentRelations",
                afterEntityLeft, afterEntityRight,
                atlasEntity -> atlasEntity.relations() == null ? null
                        : atlasEntity.relations().stream().map(Relation::getIdentifier)
                                .collect(Collectors.toSet()),
                MemberMergeStrategies.directReferenceMergerLoose);

        if (afterEntityLeft instanceof LocationItem)
        {
            return mergeLocationItems(left, right, mergedTagsBean, mergedParentRelations);
        }
        else if (afterEntityLeft instanceof LineItem)
        {
            return mergeLineItems(other, mergedTagsBean.getMergedAfterMember(),
                    mergedParentRelations);
        }
        else if (afterEntityLeft instanceof Area)
        {
            return mergeAreas(other, mergedTagsBean.getMergedAfterMember(), mergedParentRelations);
        }
        else if (afterEntityLeft instanceof Relation)
        {
            return mergeRelations(other, mergedTagsBean.getMergedAfterMember(),
                    mergedParentRelations);
        }
        else
        {
            throw new CoreException("Unknown AtlasEntity subtype {}",
                    afterEntityLeft.getClass().getName());
        }
    }

    private static FeatureChange mergeLocationItems(final FeatureChange left,
            final FeatureChange right, final MergedMemberBean<Map<String, String>> mergedTagsBean,
            final Set<Long> mergedParentRelations)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity beforeEntityRight = right.getBeforeView();
        final AtlasEntity afterEntityRight = right.getAfterView();

        /*
         * This merger will never do a proper merge. Rather, it will just ensure that the
         * afterEntities match. There is currently no reason to merge unequal locations.
         */
        final MergedMemberBean<Location> mergedLocationBean = mergeMember("location",
                beforeEntityLeft, afterEntityLeft, beforeEntityRight, afterEntityRight,
                atlasEntity -> ((LocationItem) atlasEntity).getLocation(), null, null);

        if (afterEntityLeft instanceof Node)
        {
            final MergedMemberBean<SortedSet<Long>> mergedInEdgeIdentifiersBean = mergeMember(
                    "inEdgeIdentifiers", beforeEntityLeft, afterEntityLeft, beforeEntityRight,
                    afterEntityRight,
                    atlasEntity -> ((Node) atlasEntity).inEdges() == null ? null
                            : ((Node) atlasEntity).inEdges().stream().map(Edge::getIdentifier)
                                    .collect(Collectors.toCollection(TreeSet::new)),
                    MemberMergeStrategies.directReferenceMergerSorted,
                    MemberMergeStrategies.diffBasedIntegerSortedSetMerger);

            final MergedMemberBean<SortedSet<Long>> mergedOutEdgeIdentifiersBean = mergeMember(
                    "outEdgeIdentifiers", beforeEntityLeft, afterEntityLeft, beforeEntityRight,
                    afterEntityRight,
                    atlasEntity -> ((Node) atlasEntity).outEdges() == null ? null
                            : ((Node) atlasEntity).outEdges().stream().map(Edge::getIdentifier)
                                    .collect(Collectors.toCollection(TreeSet::new)),
                    MemberMergeStrategies.directReferenceMergerSorted,
                    MemberMergeStrategies.diffBasedIntegerSortedSetMerger);

            final CompleteNode mergedAfterNode = new CompleteNode(left.getIdentifier(),
                    mergedLocationBean.getMergedAfterMember(),
                    mergedTagsBean.getMergedAfterMember(),
                    mergedInEdgeIdentifiersBean.getMergedAfterMember(),
                    mergedOutEdgeIdentifiersBean.getMergedAfterMember(), mergedParentRelations);

            final CompleteNode mergedBeforeNode = CompleteNode.shallowFrom((Node) afterEntityLeft)
                    .withInEdgeIdentifiers(mergedInEdgeIdentifiersBean.getMergedBeforeMember())
                    .withOutEdgeIdentifiers(mergedOutEdgeIdentifiersBean.getMergedBeforeMember())
                    .withTags(mergedTagsBean.getMergedBeforeMember());

            return new FeatureChange(ChangeType.ADD, mergedAfterNode, mergedBeforeNode);
        }
        else if (afterEntityLeft instanceof Point)
        {
            final CompletePoint mergedAfterPoint = new CompletePoint(left.getIdentifier(),
                    mergedLocationBean.getMergedAfterMember(),
                    mergedTagsBean.getMergedAfterMember(), mergedParentRelations);

            final CompletePoint mergedBeforePoint = CompletePoint
                    .shallowFrom((Point) afterEntityLeft)
                    .withTags(mergedTagsBean.getMergedBeforeMember());

            return new FeatureChange(ChangeType.ADD, mergedAfterPoint, mergedBeforePoint);
        }
        else
        {
            throw new CoreException("Unknown LocationItem subtype {}",
                    afterEntityLeft.getClass().getName());
        }
    }

    private FeatureChangeMergeHelpers()
    {
    }
}
