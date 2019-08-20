package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.change.MemberMerger.MergedMemberBean;
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
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for the various {@link FeatureChange} merge helper functions.
 *
 * @author lcram
 */
public final class FeatureChangeMergingHelpers
{
    static final String IN_EDGE_IDENTIFIERS_FIELD = "inEdgeIdentifiers";
    static final String OUT_EDGE_IDENTIFIERS_FIELD = "outEdgeIdentifiers";
    private static final String AFTER_ENTITY_RIGHT_WAS_NULL = "afterEntityRight was null, this should never happen!";
    private static final String AFTER_ENTITY_LEFT_WAS_NULL = "afterEntityLeft was null, this should never happen!";
    private static final Logger logger = LoggerFactory.getLogger(FeatureChangeMergingHelpers.class);

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

        if (afterEntityLeft == null)
        {
            throw new CoreException(AFTER_ENTITY_LEFT_WAS_NULL);
        }
        if (afterEntityRight == null)
        {
            throw new CoreException(AFTER_ENTITY_RIGHT_WAS_NULL);
        }

        final MergedMemberBean<Map<String, String>> mergedTagsBean = new MemberMerger.Builder<Map<String, String>>()
                .withMemberName("tags").withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight).withMemberExtractor(Taggable::getTags)
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.simpleTagMerger)
                .withAfterViewConsistentBeforeViewMerger(MemberMergeStrategies.diffBasedTagMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryTagMerger)
                .withBeforeViewMerger(MemberMergeStrategies.autofailBinaryTagMerger).build()
                .mergeMember();

        final MergedMemberBean<Set<Long>> mergedParentRelationsBean = new MemberMerger.Builder<Set<Long>>()
                .withMemberName("parentRelations").withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(atlasEntity -> atlasEntity.relations() == null ? null
                        : atlasEntity.relations().stream().map(Relation::getIdentifier)
                                .collect(Collectors.toSet()))
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.simpleLongSetMerger)
                .withAfterViewConsistentBeforeViewMerger(
                        MemberMergeStrategies.diffBasedLongSetMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryLongSetMerger)
                .withBeforeViewMerger(MemberMergeStrategies.autofailBinaryLongSetMerger).build()
                .mergeMember();

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

    /**
     * Merge the meta-data of two {@link FeatureChange}s
     *
     * @param left
     *            The left {@link FeatureChange}
     * @param right
     *            The right {@link FeatureChange}
     * @return The concatenated meta-data map
     */
    public static Map<String, String> mergeMetaData(final FeatureChange left,
            final FeatureChange right)
    {
        final Map<String, String> result = new HashMap<>();
        final Map<String, String> leftMap = left.getMetaData();
        final Map<String, String> rightMap = right.getMetaData();
        for (final Map.Entry<String, String> leftEntry : leftMap.entrySet())
        {
            if (rightMap.containsKey(leftEntry.getKey()))
            {
                final String leftValue = leftEntry.getValue();
                final String rightValue = rightMap.get(leftEntry.getKey());
                final String mergedValue;
                if (leftValue.equals(rightValue))
                {
                    mergedValue = leftValue;
                }
                else
                {
                    final Set<String> values = new TreeSet<>();
                    values.add(leftValue);
                    values.add(rightValue);
                    mergedValue = new StringList(values).join(",");
                }
                result.put(leftEntry.getKey(), mergedValue);
            }
            else
            {
                result.put(leftEntry.getKey(), leftEntry.getValue());
            }
        }
        for (final Map.Entry<String, String> rightEntry : rightMap.entrySet())
        {
            if (!result.containsKey(rightEntry.getKey()))
            {
                result.put(rightEntry.getKey(), rightEntry.getValue());
            }
        }
        return result;
    }

    /**
     * Merge two {@link ChangeType#REMOVE} {@link FeatureChange}s into a single
     * {@link FeatureChange}. This method only needs to handle merging the beforeViews, since the
     * afterViews would be null. Additionally, there are only a few beforeView fields that even need
     * to be merged in the first place, namely {@link RelationBean}s and {@link Node} in/out edge
     * identifier sets.
     *
     * @param left
     *            the left {@link FeatureChange}
     * @param right
     *            the right {@link FeatureChange}
     * @return the merged {@link FeatureChange}s
     */
    public static FeatureChange mergeREMOVEFeatureChangePair(final FeatureChange left,
            final FeatureChange right)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity beforeEntityRight = right.getBeforeView();

        /*
         * For nodes, we need to merge the beforeViews of the in/out edge identifier sets.
         */
        if (beforeEntityLeft instanceof Node)
        {
            final Node beforeNodeLeft = (Node) beforeEntityLeft;
            final Node beforeNodeRight = (Node) beforeEntityRight;
            final CompleteNode mergedBeforeNode = CompleteNode.from(beforeNodeLeft);
            final SortedSet<Long> leftInEdgeIdentifiers = new TreeSet<>(beforeNodeLeft.inEdges()
                    .stream().map(Edge::getIdentifier).collect(Collectors.toSet()));
            final SortedSet<Long> rightInEdgeIdentifiers = new TreeSet<>(beforeNodeRight.inEdges()
                    .stream().map(Edge::getIdentifier).collect(Collectors.toSet()));
            final SortedSet<Long> leftOutEdgeIdentifiers = new TreeSet<>(beforeNodeLeft.outEdges()
                    .stream().map(Edge::getIdentifier).collect(Collectors.toSet()));
            final SortedSet<Long> rightOutEdgeIdentifiers = new TreeSet<>(beforeNodeRight.outEdges()
                    .stream().map(Edge::getIdentifier).collect(Collectors.toSet()));

            if (!leftInEdgeIdentifiers.equals(rightInEdgeIdentifiers))
            {
                mergedBeforeNode
                        .withInEdgeIdentifiers(MemberMergeStrategies.simpleLongSortedSetMerger
                                .apply(leftInEdgeIdentifiers, rightInEdgeIdentifiers));
            }
            if (!leftOutEdgeIdentifiers.equals(rightOutEdgeIdentifiers))
            {
                mergedBeforeNode
                        .withOutEdgeIdentifiers(MemberMergeStrategies.simpleLongSortedSetMerger
                                .apply(leftOutEdgeIdentifiers, rightOutEdgeIdentifiers));
            }
            return new FeatureChange(ChangeType.REMOVE, left.getAfterView(), mergedBeforeNode);
        }
        /*
         * For relations, we need to merge the beforeViews of the members RelationBean.
         */
        else if (beforeEntityLeft instanceof Relation)
        {
            final Relation beforeRelationLeft = (Relation) beforeEntityLeft;
            final Relation beforeRelationRight = (Relation) beforeEntityRight;
            final CompleteRelation mergedBeforeRelation = CompleteRelation.from(beforeRelationLeft);
            final RelationBean leftMembers = beforeRelationLeft.members().asBean();
            final RelationBean rightMembers = beforeRelationRight.members().asBean();

            if (!leftMembers.equalsIncludingExplicitlyExcluded(rightMembers))
            {
                mergedBeforeRelation.withMembers(RelationBean.mergeBeans(leftMembers, rightMembers),
                        Rectangle.forLocated(beforeRelationLeft, beforeRelationRight));
            }
            return new FeatureChange(ChangeType.REMOVE, left.getAfterView(), mergedBeforeRelation);
        }
        /*
         * For any other case, there is no need to merge anything. Just arbitrarily return the left
         * side.
         */
        else
        {
            return left;
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
            throw new CoreException(AFTER_ENTITY_LEFT_WAS_NULL);
        }
        if (afterEntityRight == null)
        {
            throw new CoreException(AFTER_ENTITY_RIGHT_WAS_NULL);
        }

        /*
         * The polygon merger ensure that the afterEntity polygons either: 1) exactly match or 2)
         * are reconcilable based on the beforeView. Additionally, this step also ensures that the
         * beforeViews, if present, had equivalent geometry.
         */
        final MergedMemberBean<Polygon> mergedPolygonBean = new MemberMerger.Builder<Polygon>()
                .withMemberName("polygon").withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(atlasEntity -> ((Area) atlasEntity).asPolygon())
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.autofailBinaryPolygonMerger)
                .withAfterViewConsistentBeforeViewMerger(
                        MemberMergeStrategies.diffBasedPolygonMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryPolygonMerger)
                .withBeforeViewMerger(MemberMergeStrategies.autofailBinaryPolygonMerger).build()
                .mergeMember();

        final CompleteArea mergedAfterArea = new CompleteArea(left.getIdentifier(),
                mergedPolygonBean.getMergedAfterMember(), mergedTagsBean.getMergedAfterMember(),
                mergedParentRelationsBean.getMergedAfterMember());
        mergedAfterArea.withBoundsExtendedBy(afterEntityLeft.bounds());
        mergedAfterArea.withBoundsExtendedBy(afterEntityRight.bounds());

        final CompleteArea mergedBeforeArea;
        /*
         * Here we just arbitrarily use the left side entity. We have already asserted that both
         * left and right explicitly provided or explicitly excluded a beforeView. At this point, we
         * have also ensured that both beforeViews, if present, were consistent (or we merged them
         * if necessary). Therefore it is safe to arbitrarily choose one from which to "shallowFrom"
         * clone a new CompleteEntity.
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

    private static FeatureChange mergeEdges(final FeatureChange left, final FeatureChange right,
            final MergedMemberBean<PolyLine> mergedPolyLineBean,
            final MergedMemberBean<Map<String, String>> mergedTagsBean,
            final MergedMemberBean<Set<Long>> mergedParentRelationsBean)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity beforeEntityRight = right.getBeforeView();
        final AtlasEntity afterEntityRight = right.getAfterView();

        if (afterEntityLeft == null)
        {
            throw new CoreException(AFTER_ENTITY_LEFT_WAS_NULL);
        }
        if (afterEntityRight == null)
        {
            throw new CoreException(AFTER_ENTITY_RIGHT_WAS_NULL);
        }

        final MergedMemberBean<Long> mergedStartNodeIdentifierBean = new MemberMerger.Builder<Long>()
                .withMemberName("startNode").withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(edge -> ((Edge) edge).start() == null ? null
                        : ((Edge) edge).start().getIdentifier())
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.autofailBinaryLongMerger)
                .withAfterViewConsistentBeforeViewMerger(MemberMergeStrategies.diffBasedLongMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryLongMerger)
                .withBeforeViewMerger(MemberMergeStrategies.autofailBinaryLongMerger).build()
                .mergeMember();

        final MergedMemberBean<Long> mergedEndNodeIdentifierBean = new MemberMerger.Builder<Long>()
                .withMemberName("endNode").withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(edge -> ((Edge) edge).end() == null ? null
                        : ((Edge) edge).end().getIdentifier())
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.autofailBinaryLongMerger)
                .withAfterViewConsistentBeforeViewMerger(MemberMergeStrategies.diffBasedLongMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryLongMerger)
                .withBeforeViewMerger(MemberMergeStrategies.autofailBinaryLongMerger).build()
                .mergeMember();

        final CompleteEdge mergedAfterEdge = new CompleteEdge(left.getIdentifier(),
                mergedPolyLineBean.getMergedAfterMember(), mergedTagsBean.getMergedAfterMember(),
                mergedStartNodeIdentifierBean.getMergedAfterMember(),
                mergedEndNodeIdentifierBean.getMergedAfterMember(),
                mergedParentRelationsBean.getMergedAfterMember());
        mergedAfterEdge.withBoundsExtendedBy(afterEntityLeft.bounds());
        mergedAfterEdge.withBoundsExtendedBy(afterEntityRight.bounds());

        final CompleteEdge mergedBeforeEdge;
        /*
         * Here we just arbitrarily use the left side entity. We have already asserted that both
         * left and right explicitly provided or explicitly excluded a beforeView. At this point, we
         * have also ensured that both beforeViews, if present, were consistent (or we merged them
         * if necessary). Therefore it is safe to arbitrarily choose one from which to "shallowFrom"
         * clone a new CompleteEntity.
         */
        if (beforeEntityLeft != null)
        {
            mergedBeforeEdge = CompleteEdge.shallowFrom((Edge) beforeEntityLeft)
                    .withStartNodeIdentifier(mergedStartNodeIdentifierBean.getMergedBeforeMember())
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
            throw new CoreException(AFTER_ENTITY_LEFT_WAS_NULL);
        }
        if (afterEntityRight == null)
        {
            throw new CoreException(AFTER_ENTITY_RIGHT_WAS_NULL);
        }

        /*
         * The polyline merger ensure that the afterEntity polylines either: 1) exactly match or 2)
         * are reconcilable based on the beforeView. Additionally, this step also ensures that the
         * beforeViews, if present, had equivalent geometry.
         */
        final MergedMemberBean<PolyLine> mergedPolyLineBean = new MemberMerger.Builder<PolyLine>()
                .withMemberName("polyLine").withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(atlasEntity -> ((LineItem) atlasEntity).asPolyLine())
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.autofailBinaryPolyLineMerger)
                .withAfterViewConsistentBeforeViewMerger(
                        MemberMergeStrategies.diffBasedPolyLineMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryPolyLineMerger)
                .withBeforeViewMerger(MemberMergeStrategies.autofailBinaryPolyLineMerger).build()
                .mergeMember();

        if (afterEntityLeft instanceof Edge)
        {
            return mergeEdges(left, right, mergedPolyLineBean, mergedTagsBean,
                    mergedParentRelationsBean);
        }
        else if (afterEntityLeft instanceof Line)
        {
            return mergeLines(left, right, mergedPolyLineBean, mergedTagsBean,
                    mergedParentRelationsBean);
        }
        else
        {
            throw new CoreException("Unknown AtlasEntity subtype {}",
                    afterEntityLeft.getClass().getName());
        }
    }

    private static FeatureChange mergeLines(final FeatureChange left, final FeatureChange right,
            final MergedMemberBean<PolyLine> mergedPolyLineBean,
            final MergedMemberBean<Map<String, String>> mergedTagsBean,
            final MergedMemberBean<Set<Long>> mergedParentRelationsBean)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity afterEntityRight = right.getAfterView();

        if (afterEntityLeft == null)
        {
            throw new CoreException(AFTER_ENTITY_LEFT_WAS_NULL);
        }
        if (afterEntityRight == null)
        {
            throw new CoreException(AFTER_ENTITY_RIGHT_WAS_NULL);
        }

        final CompleteLine mergedAfterLine = new CompleteLine(left.getIdentifier(),
                mergedPolyLineBean.getMergedAfterMember(), mergedTagsBean.getMergedAfterMember(),
                mergedParentRelationsBean.getMergedAfterMember());
        mergedAfterLine.withBoundsExtendedBy(afterEntityLeft.bounds());
        mergedAfterLine.withBoundsExtendedBy(afterEntityRight.bounds());

        final CompleteLine mergedBeforeLine;
        /*
         * Here we just arbitrarily use the left side entity. We have already asserted that both
         * left and right explicitly provided or explicitly excluded a beforeView. At this point, we
         * have also ensured that both beforeViews, if present, were consistent (or we merged them
         * if necessary). Therefore it is safe to arbitrarily choose one from which to "shallowFrom"
         * clone a new CompleteEntity.
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
            throw new CoreException(AFTER_ENTITY_LEFT_WAS_NULL);
        }
        if (afterEntityRight == null)
        {
            throw new CoreException(AFTER_ENTITY_RIGHT_WAS_NULL);
        }

        /*
         * The location merger ensure that the afterEntity locations either: 1) exactly match or 2)
         * are reconcilable based on the beforeView. Additionally, this step also ensures that the
         * beforeViews, if present, had equivalent geometry.
         */
        final MergedMemberBean<Location> mergedLocationBean = new MemberMerger.Builder<Location>()
                .withMemberName("location").withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(atlasEntity -> ((LocationItem) atlasEntity).getLocation())
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.autofailBinaryLocationMerger)
                .withAfterViewConsistentBeforeViewMerger(
                        MemberMergeStrategies.diffBasedLocationMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryLocationMerger)
                .withBeforeViewMerger(MemberMergeStrategies.autofailBinaryLocationMerger).build()
                .mergeMember();

        if (afterEntityLeft instanceof Node)
        {
            return mergeNodes(left, right, mergedLocationBean, mergedTagsBean,
                    mergedParentRelationsBean);
        }
        else if (afterEntityLeft instanceof Point)
        {
            return mergePoints(left, right, mergedLocationBean, mergedTagsBean,
                    mergedParentRelationsBean);
        }
        else
        {
            throw new CoreException("Unknown LocationItem subtype {}",
                    afterEntityLeft.getClass().getName());
        }
    }

    private static FeatureChange mergeNodes(final FeatureChange left, final FeatureChange right,
            final MergedMemberBean<Location> mergedLocationBean,
            final MergedMemberBean<Map<String, String>> mergedTagsBean,
            final MergedMemberBean<Set<Long>> mergedParentRelationsBean)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity beforeEntityRight = right.getBeforeView();
        final AtlasEntity afterEntityRight = right.getAfterView();

        if (afterEntityLeft == null)
        {
            throw new CoreException(AFTER_ENTITY_LEFT_WAS_NULL);
        }
        if (afterEntityRight == null)
        {
            throw new CoreException(AFTER_ENTITY_RIGHT_WAS_NULL);
        }

        final MergedMemberBean<SortedSet<Long>> mergedInEdgeIdentifiersBean = new MemberMerger.Builder<SortedSet<Long>>()
                .withMemberName(IN_EDGE_IDENTIFIERS_FIELD).withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(atlasEntity -> ((Node) atlasEntity).inEdges() == null ? null
                        : ((Node) atlasEntity).inEdges().stream().map(Edge::getIdentifier)
                                .collect(Collectors.toCollection(TreeSet::new)))
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.simpleLongSortedSetMerger)
                .withAfterViewConsistentBeforeViewMerger(
                        MemberMergeStrategies.diffBasedLongSortedSetMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryLongSortedSetMerger)
                .withBeforeViewMerger(MemberMergeStrategies.simpleLongSortedSetMerger)
                .useHackForMergingConflictingConnectedEdgeSetBeforeViews(
                        (CompleteNode) afterEntityLeft, (CompleteNode) afterEntityRight)
                .build().mergeMember();

        final MergedMemberBean<SortedSet<Long>> mergedOutEdgeIdentifiersBean = new MemberMerger.Builder<SortedSet<Long>>()
                .withMemberName(OUT_EDGE_IDENTIFIERS_FIELD).withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(atlasEntity -> ((Node) atlasEntity).outEdges() == null ? null
                        : ((Node) atlasEntity).outEdges().stream().map(Edge::getIdentifier)
                                .collect(Collectors.toCollection(TreeSet::new)))
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.simpleLongSortedSetMerger)
                .withAfterViewConsistentBeforeViewMerger(
                        MemberMergeStrategies.diffBasedLongSortedSetMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryLongSortedSetMerger)
                .withBeforeViewMerger(MemberMergeStrategies.simpleLongSortedSetMerger)
                .useHackForMergingConflictingConnectedEdgeSetBeforeViews(
                        (CompleteNode) afterEntityLeft, (CompleteNode) afterEntityRight)
                .build().mergeMember();

        final CompleteNode mergedAfterNode = new CompleteNode(left.getIdentifier(),
                mergedLocationBean.getMergedAfterMember(), mergedTagsBean.getMergedAfterMember(),
                mergedInEdgeIdentifiersBean.getMergedAfterMember(),
                mergedOutEdgeIdentifiersBean.getMergedAfterMember(),
                mergedParentRelationsBean.getMergedAfterMember());
        mergedAfterNode.withBoundsExtendedBy(afterEntityLeft.bounds());
        mergedAfterNode.withBoundsExtendedBy(afterEntityRight.bounds());
        /*
         * We need to merge the explicitlyExcluded sets from the left and right CompleteNodes. This
         * simple merge will always succeed, since the sets are key only.
         */
        mergedAfterNode.setExplicitlyExcludedInEdgeIdentifiers(
                MemberMergeStrategies.simpleLongSetMerger.apply(
                        ((CompleteNode) afterEntityLeft).explicitlyExcludedInEdgeIdentifiers(),
                        ((CompleteNode) afterEntityRight).explicitlyExcludedInEdgeIdentifiers()));

        mergedAfterNode.setExplicitlyExcludedOutEdgeIdentifiers(
                MemberMergeStrategies.simpleLongSetMerger.apply(
                        ((CompleteNode) afterEntityLeft).explicitlyExcludedOutEdgeIdentifiers(),
                        ((CompleteNode) afterEntityRight).explicitlyExcludedOutEdgeIdentifiers()));

        final CompleteNode mergedBeforeNode;
        /*
         * Here we just arbitrarily use the left side entity. We have already asserted that both
         * left and right explicitly provided or explicitly excluded a beforeView. At this point, we
         * have also ensured that both beforeViews, if present, were consistent (or we merged them
         * if necessary). Therefore it is safe to arbitrarily choose one from which to "shallowFrom"
         * clone a new CompleteEntity.
         */
        if (beforeEntityLeft != null)
        {
            mergedBeforeNode = CompleteNode.shallowFrom((Node) beforeEntityLeft)
                    .withInEdgeIdentifiers(mergedInEdgeIdentifiersBean.getMergedBeforeMember())
                    .withOutEdgeIdentifiers(mergedOutEdgeIdentifiersBean.getMergedBeforeMember())
                    .withTags(mergedTagsBean.getMergedBeforeMember())
                    .withRelationIdentifiers(mergedParentRelationsBean.getMergedBeforeMember());
        }
        else
        {
            mergedBeforeNode = null;
        }
        return new FeatureChange(ChangeType.ADD, mergedAfterNode, mergedBeforeNode);
    }

    private static FeatureChange mergePoints(final FeatureChange left, final FeatureChange right,
            final MergedMemberBean<Location> mergedLocationBean,
            final MergedMemberBean<Map<String, String>> mergedTagsBean,
            final MergedMemberBean<Set<Long>> mergedParentRelationsBean)
    {
        final AtlasEntity beforeEntityLeft = left.getBeforeView();
        final AtlasEntity afterEntityLeft = left.getAfterView();
        final AtlasEntity afterEntityRight = right.getAfterView();

        if (afterEntityLeft == null)
        {
            throw new CoreException(AFTER_ENTITY_LEFT_WAS_NULL);
        }
        if (afterEntityRight == null)
        {
            throw new CoreException(AFTER_ENTITY_RIGHT_WAS_NULL);
        }

        final CompletePoint mergedAfterPoint = new CompletePoint(left.getIdentifier(),
                mergedLocationBean.getMergedAfterMember(), mergedTagsBean.getMergedAfterMember(),
                mergedParentRelationsBean.getMergedAfterMember());
        mergedAfterPoint.withBoundsExtendedBy(afterEntityLeft.bounds());
        mergedAfterPoint.withBoundsExtendedBy(afterEntityRight.bounds());

        final CompletePoint mergedBeforePoint;
        /*
         * Here we just arbitrarily use the left side entity. We have already asserted that both
         * left and right explicitly provided or explicitly excluded a beforeView. At this point, we
         * have also ensured that both beforeViews, if present, were consistent (or we merged them
         * if necessary). Therefore it is safe to arbitrarily choose one from which to "shallowFrom"
         * clone a new CompleteEntity.
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
            throw new CoreException(AFTER_ENTITY_LEFT_WAS_NULL);
        }
        if (afterEntityRight == null)
        {
            throw new CoreException(AFTER_ENTITY_RIGHT_WAS_NULL);
        }

        final MergedMemberBean<RelationBean> mergedMembersBean = new MemberMerger.Builder<RelationBean>()
                .withMemberName("relationMembers").withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(entity -> ((Relation) entity).members() == null ? null
                        : ((Relation) entity).members().asBean())
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.simpleRelationBeanMerger)
                .withAfterViewConsistentBeforeViewMerger(
                        MemberMergeStrategies.diffBasedRelationBeanMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.conflictingBeforeViewRelationBeanMerger)
                .withBeforeViewMerger(MemberMergeStrategies.simpleRelationBeanMerger).build()
                .mergeMember();

        final MergedMemberBean<Set<Long>> mergedAllRelationsWithSameOsmIdentifierBean = new MemberMerger.Builder<Set<Long>>()
                .withMemberName("allRelationsWithSameOsmIdentifier")
                .withBeforeEntityLeft(beforeEntityLeft).withAfterEntityLeft(afterEntityLeft)
                .withBeforeEntityRight(beforeEntityRight).withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(atlasEntity -> ((Relation) atlasEntity)
                        .allRelationsWithSameOsmIdentifier() == null
                                ? null
                                : ((Relation) atlasEntity).allRelationsWithSameOsmIdentifier()
                                        .stream().map(Relation::getIdentifier)
                                        .collect(Collectors.toSet()))
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.simpleLongSetMerger)
                .withAfterViewConsistentBeforeViewMerger(
                        MemberMergeStrategies.diffBasedLongSetMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryLongSetMerger)
                .withBeforeViewMerger(MemberMergeStrategies.autofailBinaryLongSetMerger).build()
                .mergeMember();

        final MergedMemberBean<RelationBean> mergedAllKnownMembersBean = new MemberMerger.Builder<RelationBean>()
                .withMemberName("allKnownOsmMembers").withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(
                        entity -> ((Relation) entity).allKnownOsmMembers() == null ? null
                                : ((Relation) entity).allKnownOsmMembers().asBean())
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.simpleRelationBeanMerger)
                .withAfterViewConsistentBeforeViewMerger(
                        MemberMergeStrategies.diffBasedRelationBeanMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.conflictingBeforeViewRelationBeanMerger)
                .withBeforeViewMerger(MemberMergeStrategies.simpleRelationBeanMerger).build()
                .mergeMember();

        final MergedMemberBean<Long> mergedOsmRelationIdentifier = new MemberMerger.Builder<Long>()
                .withMemberName("osmRelationIdentifier").withBeforeEntityLeft(beforeEntityLeft)
                .withAfterEntityLeft(afterEntityLeft).withBeforeEntityRight(beforeEntityRight)
                .withAfterEntityRight(afterEntityRight)
                .withMemberExtractor(entity -> ((Relation) entity).osmRelationIdentifier())
                .withAfterViewNoBeforeMerger(MemberMergeStrategies.autofailBinaryLongMerger)
                .withAfterViewConsistentBeforeViewMerger(MemberMergeStrategies.diffBasedLongMerger)
                .withAfterViewConflictingBeforeViewMerger(
                        MemberMergeStrategies.autofailQuaternaryLongMerger)
                .withBeforeViewMerger(MemberMergeStrategies.autofailBinaryLongMerger).build()
                .mergeMember();

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
         * have also ensured that both beforeViews, if present, were consistent (or we merged them
         * if necessary). Therefore it is safe to arbitrarily choose one from which to "shallowFrom"
         * clone a new CompleteEntity.
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
                                                    .getMergedBeforeMember().stream()
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
