package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.function.TernaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for the various {@link FeatureChange} merge helper functions.
 *
 * @author lcram
 */
public final class FeatureChangeMergingHelpers
{
    private static final Logger logger = LoggerFactory.getLogger(FeatureChangeMergingHelpers.class);

    /**
     * Merge some feature member using a left and right before/after view.
     *
     * @param memberName
     *            the name of the member for logging purposes
     * @param beforeEntityLeft
     *            the left side before view of the entity
     * @param afterEntityLeft
     *            the left side after view of the entity
     * @param beforeEntityRight
     *            the right side before view of the entity
     * @param afterEntityRight
     *            the right side after view of the entity
     * @param memberExtractor
     *            a function that can extract the member from its entity (e.g. for the polyline of
     *            an Edge, this would be Edge::asPolyLine)
     * @param simpleMergeStrategy
     *            a simple merge strategy to use if the before views are missing
     * @param diffBasedMergeStrategy
     *            a merge strategy that relies on the before views to perform a more complex merge
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

        final M beforeMemberLeft = beforeEntityLeft == null ? null
                : memberExtractor.apply(beforeEntityLeft);
        final M afterMemberLeft = afterEntityLeft == null ? null
                : memberExtractor.apply(afterEntityLeft);
        final M beforeMemberRight = beforeEntityRight == null ? null
                : memberExtractor.apply(beforeEntityRight);
        final M afterMemberRight = afterEntityRight == null ? null
                : memberExtractor.apply(afterEntityRight);

        /*
         * In the case that both beforeMembers are present, we assert their equivalence before
         * continuing. It does not make sense to merge two FeatureChanges with conflicting
         * beforeMember views.
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

    static <M> M mergeMemberOldStrategy(final String memberName, final AtlasEntity afterEntityLeft,
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

    /**
     * Merge two {@link ChangeType#ADD} {@link FeatureChange}s into a single {@link FeatureChange}.
     *
     * @param left
     *            the left {@link FeatureChange}
     * @param right
     *            the right {@link FeatureChange}
     * @return the merged {@link FeatureChange}s
     */
    public static FeatureChange mergeADDFeatureChangePair(final FeatureChange left,
            final FeatureChange right)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity beforeEntityRight = right.getBeforeView();
        final AtlasEntity afterEntityRight = right.getAfterView();

        final MergedMemberBean<Map<String, String>> mergedTagsBean = mergeMember("tags",
                beforeEntityLeft, afterEntityLeft, beforeEntityRight, afterEntityRight,
                Taggable::getTags, MemberMergeStrategies.simpleTagMerger,
                MemberMergeStrategies.diffBasedTagMerger);

        final MergedMemberBean<Set<Long>> mergedParentRelationsBean = mergeMember("parentRelations",
                beforeEntityLeft, afterEntityLeft, beforeEntityRight, afterEntityRight,
                atlasEntity -> atlasEntity.relations() == null ? null
                        : atlasEntity.relations().stream().map(Relation::getIdentifier)
                                .collect(Collectors.toSet()),
                MemberMergeStrategies.simpleLongSetAllowCollisionsMerger,
                MemberMergeStrategies.diffBasedLongSetMerger);

        if (afterEntityLeft instanceof LocationItem)
        {
            return mergeLocationItems(left, right, mergedTagsBean, mergedParentRelationsBean);
        }
        else if (afterEntityLeft instanceof LineItem)
        {
            return mergeLineItems(left, right, mergedTagsBean, mergedParentRelationsBean);
        }
        else if (afterEntityLeft instanceof Area)
        {
            return mergeAreas(left, right, mergedTagsBean, mergedParentRelationsBean);
        }
        else if (afterEntityLeft instanceof Relation)
        {
            return mergeRelations(left, right, mergedTagsBean, mergedParentRelationsBean);
        }
        else
        {
            throw new CoreException("Unknown AtlasEntity subtype {}",
                    afterEntityLeft.getClass().getName());
        }
    }

    private static FeatureChange mergeAreas(final FeatureChange left, final FeatureChange right,
            final MergedMemberBean<Map<String, String>> mergedTagsBean,
            final MergedMemberBean<Set<Long>> mergedParentRelationsBean)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity beforeEntityRight = right.getBeforeView();
        final AtlasEntity afterEntityRight = right.getAfterView();

        if (afterEntityLeft == null)
        {
            throw new CoreException("afterEntityLeft was null, this should never happen!");
        }
        if (afterEntityRight == null)
        {
            throw new CoreException("afterEntityRight was null, this should never happen!");
        }

        /*
         * The polygon merger will not do a strategy merge. Rather, it will just ensure that the
         * afterEntity polygons match. There is currently no reason to merge unequal polygons.
         * Additionally, this step also ensures that the beforeViews, if present, had equivalent
         * geometry.
         */
        final MergedMemberBean<Polygon> mergedPolygonBean = mergeMember("polygon", beforeEntityLeft,
                afterEntityLeft, beforeEntityRight, afterEntityRight,
                atlasEntity -> ((Area) atlasEntity).asPolygon(), null, null);

        final CompleteArea mergedAfterArea = new CompleteArea(left.getIdentifier(),
                mergedPolygonBean.getMergedAfterMember(), mergedTagsBean.getMergedAfterMember(),
                mergedParentRelationsBean.getMergedAfterMember());
        mergedAfterArea.withBoundsExtendedBy(afterEntityLeft.bounds());
        mergedAfterArea.withBoundsExtendedBy(afterEntityRight.bounds());

        final CompleteArea mergedBeforeArea;
        /*
         * Here we just arbitrarily use the left side entity. We have already asserted that both
         * left and right explicitly provided or explicitly excluded a beforeView. At this point, we
         * have also ensured that both beforeViews, if present, were consistent (i.e. they contained
         * the same initial geometry). Therefore it is safe to arbitrarily choose one from which to
         * "shallowFrom" clone a new CompleteEntity.
         */
        if (beforeEntityLeft != null)
        {
            mergedBeforeArea = CompleteArea.shallowFrom((Area) beforeEntityLeft)
                    .withTags(mergedTagsBean.getMergedBeforeMember())
                    .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());
        }
        else
        {
            mergedBeforeArea = null;
        }
        return new FeatureChange(ChangeType.ADD, mergedAfterArea, mergedBeforeArea);
    }

    private static FeatureChange mergeLineItems(final FeatureChange left, final FeatureChange right,
            final MergedMemberBean<Map<String, String>> mergedTagsBean,
            final MergedMemberBean<Set<Long>> mergedParentRelationsBean)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity beforeEntityRight = right.getBeforeView();
        final AtlasEntity afterEntityRight = right.getAfterView();

        if (afterEntityLeft == null)
        {
            throw new CoreException("afterEntityLeft was null, this should never happen!");
        }
        if (afterEntityRight == null)
        {
            throw new CoreException("afterEntityRight was null, this should never happen!");
        }

        /*
         * The polyline merger will not do a strategy merge. Rather, it will just ensure that the
         * afterEntity polylines match. There is currently no reason to merge unequal polylines.
         * Additionally, this step also ensures that the beforeViews, if present, had equivalent
         * geometry.
         */
        final MergedMemberBean<PolyLine> mergedPolyLineBean = mergeMember("polyLine",
                beforeEntityLeft, afterEntityLeft, beforeEntityRight, afterEntityRight,
                atlasEntity -> ((LineItem) atlasEntity).asPolyLine(), null, null);

        if (afterEntityLeft instanceof Edge)
        {
            final MergedMemberBean<Long> mergedStartNodeIdentifierBean = mergeMember("startNode",
                    beforeEntityLeft, afterEntityLeft, beforeEntityRight, afterEntityRight,
                    edge -> ((Edge) edge).start() == null ? null
                            : ((Edge) edge).start().getIdentifier(),
                    null, null);
            final MergedMemberBean<Long> mergedEndNodeIdentifierBean = mergeMember("endNode",
                    beforeEntityLeft, afterEntityLeft, beforeEntityRight, afterEntityRight,
                    edge -> ((Edge) edge).end() == null ? null
                            : ((Edge) edge).end().getIdentifier(),
                    null, null);

            final CompleteEdge mergedAfterEdge = new CompleteEdge(left.getIdentifier(),
                    mergedPolyLineBean.getMergedAfterMember(),
                    mergedTagsBean.getMergedAfterMember(),
                    mergedStartNodeIdentifierBean.getMergedAfterMember(),
                    mergedEndNodeIdentifierBean.getMergedAfterMember(),
                    mergedParentRelationsBean.getMergedAfterMember());
            mergedAfterEdge.withBoundsExtendedBy(afterEntityLeft.bounds());
            mergedAfterEdge.withBoundsExtendedBy(afterEntityRight.bounds());

            final CompleteEdge mergedBeforeEdge;
            /*
             * Here we just arbitrarily use the left side entity. We have already asserted that both
             * left and right explicitly provided or explicitly excluded a beforeView. At this
             * point, we have also ensured that both beforeViews, if present, were consistent (i.e.
             * they contained the same initial geometry). Therefore it is safe to arbitrarily choose
             * one from which to "shallowFrom" clone a new CompleteEntity.
             */
            if (beforeEntityLeft != null)
            {
                mergedBeforeEdge = CompleteEdge.shallowFrom((Edge) beforeEntityLeft)
                        .withStartNodeIdentifier(
                                mergedStartNodeIdentifierBean.getMergedBeforeMember())
                        .withEndNodeIdentifier(mergedEndNodeIdentifierBean.getMergedBeforeMember())
                        .withTags(mergedTagsBean.getMergedBeforeMember())
                        .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());
            }
            else
            {
                mergedBeforeEdge = null;
            }
            return new FeatureChange(ChangeType.ADD, mergedAfterEdge, mergedBeforeEdge);
        }
        else if (afterEntityLeft instanceof Line)
        {
            final CompleteLine mergedAfterLine = new CompleteLine(left.getIdentifier(),
                    mergedPolyLineBean.getMergedAfterMember(),
                    mergedTagsBean.getMergedAfterMember(),
                    mergedParentRelationsBean.getMergedAfterMember());
            mergedAfterLine.withBoundsExtendedBy(afterEntityLeft.bounds());
            mergedAfterLine.withBoundsExtendedBy(afterEntityRight.bounds());

            final CompleteLine mergedBeforeLine;
            /*
             * Here we just arbitrarily use the left side entity. We have already asserted that both
             * left and right explicitly provided or explicitly excluded a beforeView. At this
             * point, we have also ensured that both beforeViews, if present, were consistent (i.e.
             * they contained the same initial geometry). Therefore it is safe to arbitrarily choose
             * one from which to "shallowFrom" clone a new CompleteEntity.
             */
            if (beforeEntityLeft != null)
            {
                mergedBeforeLine = CompleteLine.shallowFrom((Line) beforeEntityLeft)
                        .withTags(mergedTagsBean.getMergedBeforeMember())
                        .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());
            }
            else
            {
                mergedBeforeLine = null;
            }
            return new FeatureChange(ChangeType.ADD, mergedAfterLine, mergedBeforeLine);
        }
        else
        {
            throw new CoreException("Unknown AtlasEntity subtype {}",
                    afterEntityLeft.getClass().getName());
        }
    }

    private static FeatureChange mergeLocationItems(final FeatureChange left,
            final FeatureChange right, final MergedMemberBean<Map<String, String>> mergedTagsBean,
            final MergedMemberBean<Set<Long>> mergedParentRelationsBean)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity beforeEntityRight = right.getBeforeView();
        final AtlasEntity afterEntityRight = right.getAfterView();

        if (afterEntityLeft == null)
        {
            throw new CoreException("afterEntityLeft was null, this should never happen!");
        }
        if (afterEntityRight == null)
        {
            throw new CoreException("afterEntityRight was null, this should never happen!");
        }

        /*
         * The location merger will not do a strategy merge. Rather, it will just ensure that the
         * afterEntity locations match. There is currently no reason to merge unequal locations.
         * Additionally, this step also ensures that the beforeViews, if present, had equivalent
         * geometry.
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
                    MemberMergeStrategies.simpleLongSortedSetMerger,
                    MemberMergeStrategies.diffBasedLongSortedSetMerger);

            final MergedMemberBean<SortedSet<Long>> mergedOutEdgeIdentifiersBean = mergeMember(
                    "outEdgeIdentifiers", beforeEntityLeft, afterEntityLeft, beforeEntityRight,
                    afterEntityRight,
                    atlasEntity -> ((Node) atlasEntity).outEdges() == null ? null
                            : ((Node) atlasEntity).outEdges().stream().map(Edge::getIdentifier)
                                    .collect(Collectors.toCollection(TreeSet::new)),
                    MemberMergeStrategies.simpleLongSortedSetMerger,
                    MemberMergeStrategies.diffBasedLongSortedSetMerger);

            final CompleteNode mergedAfterNode = new CompleteNode(left.getIdentifier(),
                    mergedLocationBean.getMergedAfterMember(),
                    mergedTagsBean.getMergedAfterMember(),
                    mergedInEdgeIdentifiersBean.getMergedAfterMember(),
                    mergedOutEdgeIdentifiersBean.getMergedAfterMember(),
                    mergedParentRelationsBean.getMergedAfterMember());
            mergedAfterNode.withBoundsExtendedBy(afterEntityLeft.bounds());
            mergedAfterNode.withBoundsExtendedBy(afterEntityRight.bounds());

            final CompleteNode mergedBeforeNode;
            /*
             * Here we just arbitrarily use the left side entity. We have already asserted that both
             * left and right explicitly provided or explicitly excluded a beforeView. At this
             * point, we have also ensured that both beforeViews, if present, were consistent (i.e.
             * they contained the same initial geometry). Therefore it is safe to arbitrarily choose
             * one from which to "shallowFrom" clone a new CompleteEntity.
             */
            if (beforeEntityLeft != null)
            {
                mergedBeforeNode = CompleteNode.shallowFrom((Node) beforeEntityLeft)
                        .withInEdgeIdentifiers(mergedInEdgeIdentifiersBean.getMergedBeforeMember())
                        .withOutEdgeIdentifiers(
                                mergedOutEdgeIdentifiersBean.getMergedBeforeMember())
                        .withTags(mergedTagsBean.getMergedBeforeMember())
                        .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());

            }
            else
            {
                mergedBeforeNode = null;
            }
            return new FeatureChange(ChangeType.ADD, mergedAfterNode, mergedBeforeNode);
        }
        else if (afterEntityLeft instanceof Point)
        {
            final CompletePoint mergedAfterPoint = new CompletePoint(left.getIdentifier(),
                    mergedLocationBean.getMergedAfterMember(),
                    mergedTagsBean.getMergedAfterMember(),
                    mergedParentRelationsBean.getMergedAfterMember());
            mergedAfterPoint.withBoundsExtendedBy(afterEntityLeft.bounds());
            mergedAfterPoint.withBoundsExtendedBy(afterEntityRight.bounds());

            final CompletePoint mergedBeforePoint;
            /*
             * Here we just arbitrarily use the left side entity. We have already asserted that both
             * left and right explicitly provided or explicitly excluded a beforeView. At this
             * point, we have also ensured that both beforeViews, if present, were consistent (i.e.
             * they contained the same initial geometry). Therefore it is safe to arbitrarily choose
             * one from which to "shallowFrom" clone a new CompleteEntity.
             */
            if (beforeEntityLeft != null)
            {
                mergedBeforePoint = CompletePoint.shallowFrom((Point) beforeEntityLeft)
                        .withTags(mergedTagsBean.getMergedBeforeMember())
                        .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());
            }
            else
            {
                mergedBeforePoint = null;
            }
            return new FeatureChange(ChangeType.ADD, mergedAfterPoint, mergedBeforePoint);
        }
        else
        {
            throw new CoreException("Unknown LocationItem subtype {}",
                    afterEntityLeft.getClass().getName());
        }
    }

    private static FeatureChange mergeRelations(final FeatureChange left, final FeatureChange right,
            final MergedMemberBean<Map<String, String>> mergedTagsBean,
            final MergedMemberBean<Set<Long>> mergedParentRelationsBean)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity beforeEntityRight = right.getBeforeView();
        final AtlasEntity afterEntityRight = right.getAfterView();

        if (afterEntityLeft == null)
        {
            throw new CoreException("afterEntityLeft was null, this should never happen!");
        }
        if (afterEntityRight == null)
        {
            throw new CoreException("afterEntityRight was null, this should never happen!");
        }

        final MergedMemberBean<RelationBean> mergedMembersBean = mergeMember("relationMembers",
                beforeEntityLeft, afterEntityLeft, beforeEntityRight, afterEntityRight,
                entity -> ((Relation) entity).members() == null ? null
                        : ((Relation) entity).members().asBean(),
                null, MemberMergeStrategies.diffBasedRelationBeanMerger);

        final MergedMemberBean<Set<Long>> mergedAllRelationsWithSameOsmIdentifierBean = mergeMember(
                "allRelationsWithSameOsmIdentifier", beforeEntityLeft, afterEntityLeft,
                beforeEntityRight, afterEntityRight,
                atlasEntity -> ((Relation) atlasEntity).allRelationsWithSameOsmIdentifier() == null
                        ? null
                        : ((Relation) atlasEntity).allRelationsWithSameOsmIdentifier().stream()
                                .map(Relation::getIdentifier).collect(Collectors.toSet()),
                MemberMergeStrategies.simpleLongSetMerger,
                MemberMergeStrategies.diffBasedLongSetMerger);

        final MergedMemberBean<RelationBean> mergedAllKnownMembersBean = mergeMember(
                "allKnownOsmMembers", beforeEntityLeft, afterEntityLeft, beforeEntityRight,
                afterEntityRight,
                entity -> ((Relation) entity).allKnownOsmMembers() == null ? null
                        : ((Relation) entity).allKnownOsmMembers().asBean(),
                null, MemberMergeStrategies.diffBasedRelationBeanMerger);

        final MergedMemberBean<Long> mergedOsmRelationIdentifier = mergeMember(
                "osmRelationIdentifier", beforeEntityLeft, afterEntityLeft, beforeEntityRight,
                afterEntityRight, entity -> ((Relation) entity).osmRelationIdentifier(), null,
                null);

        final Rectangle mergedBounds = Rectangle.forLocated(afterEntityLeft, afterEntityRight);

        final CompleteRelation mergedAfterRelation = new CompleteRelation(left.getIdentifier(),
                mergedTagsBean.getMergedAfterMember(), mergedBounds,
                mergedMembersBean.getMergedAfterMember(),
                mergedAllRelationsWithSameOsmIdentifierBean.getMergedAfterMember() != null
                        ? mergedAllRelationsWithSameOsmIdentifierBean.getMergedAfterMember()
                                .stream().collect(Collectors.toList())
                        : null,
                mergedAllKnownMembersBean.getMergedAfterMember(),
                mergedOsmRelationIdentifier.getMergedAfterMember(),
                mergedParentRelationsBean.getMergedAfterMember());
        mergedAfterRelation.withBoundsExtendedBy(afterEntityLeft.bounds());
        mergedAfterRelation.withBoundsExtendedBy(afterEntityRight.bounds());

        final CompleteRelation mergedBeforeRelation;
        /*
         * Here we just arbitrarily use the left side entity. We have already asserted that both
         * left and right explicitly provided or explicitly excluded a beforeView. At this point, we
         * have also ensured that both beforeViews, if present, were consistent (i.e. they contained
         * the same initial geometry). Therefore it is safe to arbitrarily choose one from which to
         * "shallowFrom" clone a new CompleteEntity.
         */
        if (beforeEntityLeft != null)
        {
            mergedBeforeRelation = CompleteRelation.shallowFrom((Relation) beforeEntityLeft)
                    .withMembers(mergedMembersBean.getMergedBeforeMember(),
                            beforeEntityLeft.bounds())
                    .withAllRelationsWithSameOsmIdentifier(
                            mergedAllRelationsWithSameOsmIdentifierBean
                                    .getMergedAfterMember() != null
                                            ? mergedAllRelationsWithSameOsmIdentifierBean
                                                    .getMergedAfterMember().stream()
                                                    .collect(Collectors.toList())
                                            : null)
                    .withAllKnownOsmMembers(mergedAllKnownMembersBean.getMergedBeforeMember())
                    .withOsmRelationIdentifier(mergedOsmRelationIdentifier.getMergedBeforeMember())
                    .withTags(mergedTagsBean.getMergedBeforeMember())
                    .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());
        }
        else
        {
            mergedBeforeRelation = null;
        }
        return new FeatureChange(ChangeType.ADD, mergedAfterRelation, mergedBeforeRelation);
    }

    private FeatureChangeMergingHelpers()
    {
    }
}
