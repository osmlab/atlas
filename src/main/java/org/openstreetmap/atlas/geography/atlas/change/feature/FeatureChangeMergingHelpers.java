package org.openstreetmap.atlas.geography.atlas.change.feature;

import java.util.List;
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
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
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
            return mergeRelationsOLD(left, right, mergedTagsBean, mergedParentRelationsBean);
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

        /*
         * The polygon merger will not do a strategy merge. Rather, it will just ensure that the
         * afterEntity polygons match. There is currently no reason to merge unequal polygons.
         */
        final MergedMemberBean<Polygon> mergedPolygonBean = mergeMember("polygon", beforeEntityLeft,
                afterEntityLeft, beforeEntityRight, afterEntityRight,
                atlasEntity -> ((Area) atlasEntity).asPolygon(), null, null);

        final CompleteArea mergedAfterArea = new CompleteArea(left.getIdentifier(),
                mergedPolygonBean.getMergedAfterMember(), mergedTagsBean.getMergedAfterMember(),
                mergedParentRelationsBean.getMergedAfterMember());

        final CompleteArea mergedBeforeArea = CompleteArea.shallowFrom((Area) beforeEntityLeft)
                .withTags(mergedTagsBean.getMergedBeforeMember())
                .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());

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

        /*
         * The polyline merger will not do a strategy merge. Rather, it will just ensure that the
         * afterEntity polylines match. There is currently no reason to merge unequal polylines.
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

            final CompleteEdge mergedBeforeEdge = CompleteEdge.shallowFrom((Edge) beforeEntityLeft)
                    .withStartNodeIdentifier(mergedStartNodeIdentifierBean.getMergedBeforeMember())
                    .withEndNodeIdentifier(mergedEndNodeIdentifierBean.getMergedBeforeMember())
                    .withTags(mergedTagsBean.getMergedBeforeMember())
                    .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());

            return new FeatureChange(ChangeType.ADD, mergedAfterEdge, mergedBeforeEdge);
        }
        else if (afterEntityLeft instanceof Line)
        {
            final CompleteLine mergedAfterLine = new CompleteLine(left.getIdentifier(),
                    mergedPolyLineBean.getMergedAfterMember(),
                    mergedTagsBean.getMergedAfterMember(),
                    mergedParentRelationsBean.getMergedAfterMember());

            final CompleteLine mergedBeforeLine = CompleteLine.shallowFrom((Line) beforeEntityLeft)
                    .withTags(mergedTagsBean.getMergedBeforeMember())
                    .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());

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

        /*
         * The location merger will not do a strategy merge. Rather, it will just ensure that the
         * afterEntity locations match. There is currently no reason to merge unequal locations.
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

            final CompleteNode mergedBeforeNode = CompleteNode.shallowFrom((Node) beforeEntityLeft)
                    .withInEdgeIdentifiers(mergedInEdgeIdentifiersBean.getMergedBeforeMember())
                    .withOutEdgeIdentifiers(mergedOutEdgeIdentifiersBean.getMergedBeforeMember())
                    .withTags(mergedTagsBean.getMergedBeforeMember())
                    .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());

            return new FeatureChange(ChangeType.ADD, mergedAfterNode, mergedBeforeNode);
        }
        else if (afterEntityLeft instanceof Point)
        {
            final CompletePoint mergedAfterPoint = new CompletePoint(left.getIdentifier(),
                    mergedLocationBean.getMergedAfterMember(),
                    mergedTagsBean.getMergedAfterMember(),
                    mergedParentRelationsBean.getMergedAfterMember());

            final CompletePoint mergedBeforePoint = CompletePoint
                    .shallowFrom((Point) beforeEntityLeft)
                    .withTags(mergedTagsBean.getMergedBeforeMember())
                    .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());

            return new FeatureChange(ChangeType.ADD, mergedAfterPoint, mergedBeforePoint);
        }
        else
        {
            throw new CoreException("Unknown LocationItem subtype {}",
                    afterEntityLeft.getClass().getName());
        }
    }

    private static FeatureChange mergeRelationsOLD(final FeatureChange left,
            final FeatureChange right, final MergedMemberBean<Map<String, String>> mergedTagsBean,
            final MergedMemberBean<Set<Long>> mergedParentRelationsBean)
    {
        final AtlasEntity thisReference = left.getAfterView();
        final AtlasEntity thatReference = right.getAfterView();

        final RelationBean mergedMembers = mergeMemberOldStrategy("relationMembers", thisReference,
                thatReference,
                entity -> ((Relation) entity).members() == null ? null
                        : ((Relation) entity).members().asBean(),
                MemberMergeStrategies.simpleRelationBeanMerger);
        final Rectangle mergedBounds = Rectangle.forLocated(thisReference, thatReference);
        final Long mergedOsmRelationIdentifier = mergeMemberOldStrategy("osmRelationIdentifier",
                thisReference, thatReference, entity -> ((Relation) entity).getOsmIdentifier(),
                null);
        final Set<Long> mergedAllRelationsWithSameOsmIdentifierSet = mergeMemberOldStrategy(
                "allRelationsWithSameOsmIdentifier", thisReference, thatReference,
                atlasEntity -> ((Relation) atlasEntity).allRelationsWithSameOsmIdentifier() == null
                        ? null
                        : ((Relation) atlasEntity).allRelationsWithSameOsmIdentifier().stream()
                                .map(Relation::getIdentifier).collect(Collectors.toSet()),
                MemberMergeStrategies.simpleLongSetMerger);
        final List<Long> mergedAllRelationsWithSameOsmIdentifier = mergedAllRelationsWithSameOsmIdentifierSet == null
                ? null
                : mergedAllRelationsWithSameOsmIdentifierSet.stream().collect(Collectors.toList());
        final RelationBean mergedAllKnownMembers = mergeMemberOldStrategy("allKnownOsmMembers",
                thisReference, thatReference,
                entity -> ((Relation) entity).allKnownOsmMembers() == null ? null
                        : ((Relation) entity).allKnownOsmMembers().asBean(),
                MemberMergeStrategies.simpleRelationBeanMerger);

        return FeatureChange.add(new CompleteRelation(left.getIdentifier(),
                mergedTagsBean.getMergedAfterMember(), mergedBounds, mergedMembers,
                mergedAllRelationsWithSameOsmIdentifier, mergedAllKnownMembers,
                mergedOsmRelationIdentifier, mergedParentRelationsBean.getMergedAfterMember()));
    }

    private FeatureChangeMergingHelpers()
    {
    }
}
