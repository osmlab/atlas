package org.openstreetmap.atlas.geography.atlas.delta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.delta.Diff.DiffReason;
import org.openstreetmap.atlas.geography.atlas.delta.Diff.DiffType;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.matching.PolyLineRoute;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Difference between two {@link Atlas}.
 *
 * @author matthieun
 */
public class AtlasDelta implements Serializable
{
    private static final long serialVersionUID = 1189641317938152158L;
    private static final Logger logger = LoggerFactory.getLogger(AtlasDelta.class);
    private static final int COUNTER_REPORT = 100_000;

    private final Atlas before;
    private final Atlas after;
    private final SortedSet<Diff> differences;
    private final boolean withGeometryMatching;
    private final transient CounterWithStatistic counter;

    public AtlasDelta(final Atlas before, final Atlas after)
    {
        this(before, after, false);
    }

    public AtlasDelta(final Atlas before, final Atlas after, final boolean withGeometryMatching)
    {
        this.before = before;
        this.after = after;
        this.differences = new TreeSet<>();
        this.counter = new CounterWithStatistic(logger, COUNTER_REPORT, "Processed");
        this.withGeometryMatching = withGeometryMatching;
    }

    public AtlasDelta generate()
    {
        // Check for removed identifiers
        logger.info("Looking for removed items.");
        for (final AtlasEntity entity : this.before)
        {
            this.counter.increment();
            if (entity.getType().entityForIdentifier(this.after, entity.getIdentifier()) == null
                    && (!(entity instanceof Edge) || !hasGoodMatch((Edge) entity, this.after)))
            {
                this.differences.add(new Diff(entity.getType(), DiffType.REMOVED,
                        DiffReason.REMOVED, this.before, this.after, entity.getIdentifier()));
            }
        }
        // Check for added identifiers
        logger.info("Looking for added items.");
        for (final AtlasEntity entity : this.after)
        {
            this.counter.increment();
            if (entity.getType().entityForIdentifier(this.before, entity.getIdentifier()) == null
                    && (!(entity instanceof Edge) || !hasGoodMatch((Edge) entity, this.before)))
            {
                this.differences.add(new Diff(entity.getType(), DiffType.ADDED, DiffReason.ADDED,
                        this.before, this.after, entity.getIdentifier()));
            }
        }
        logger.info("Looking for changed items.");
        for (final AtlasEntity baseEntity : this.before)
        {
            this.counter.increment();
            final long identifier = baseEntity.getIdentifier();
            final AtlasEntity alterEntity = baseEntity.getType().entityForIdentifier(this.after,
                    baseEntity.getIdentifier());
            // Look only at entities that are in both Atlas.
            if (alterEntity != null)
            {
                // Entity Tags & Entity's Relations first
                if (!baseEntity.getTags().equals(alterEntity.getTags()))
                {
                    this.differences.add(new Diff(baseEntity.getType(), DiffType.CHANGED,
                            DiffReason.TAGS, this.before, this.after, identifier));
                }
                else if (differentInRelation(baseEntity, alterEntity))
                {
                    this.differences.add(new Diff(baseEntity.getType(), DiffType.CHANGED,
                            DiffReason.RELATION_MEMBER, this.before, this.after, identifier));
                }
                else if (baseEntity instanceof Node)
                {
                    if (differentNodes((Node) baseEntity, (Node) alterEntity))
                    {
                        this.differences.add(new Diff(ItemType.NODE, DiffType.CHANGED,
                                DiffReason.GEOMETRY_OR_TOPOLOGY, this.before, this.after,
                                identifier));
                    }
                }
                else if (baseEntity instanceof Edge)
                {
                    if (differentEdges((Edge) baseEntity, (Edge) alterEntity))
                    {
                        this.differences.add(new Diff(ItemType.EDGE, DiffType.CHANGED,
                                DiffReason.GEOMETRY_OR_TOPOLOGY, this.before, this.after,
                                identifier));
                    }
                }
                else if (baseEntity instanceof Area)
                {
                    if (differentAreas((Area) baseEntity, (Area) alterEntity))
                    {
                        this.differences.add(new Diff(ItemType.AREA, DiffType.CHANGED,
                                DiffReason.GEOMETRY_OR_TOPOLOGY, this.before, this.after,
                                identifier));
                    }
                }
                else if (baseEntity instanceof Line)
                {
                    if (differentLines((Line) baseEntity, (Line) alterEntity))
                    {
                        this.differences.add(new Diff(ItemType.LINE, DiffType.CHANGED,
                                DiffReason.GEOMETRY_OR_TOPOLOGY, this.before, this.after,
                                identifier));
                    }
                }
                else if (baseEntity instanceof Point)
                {
                    if (differentPoints((Point) baseEntity, (Point) alterEntity))
                    {
                        this.differences.add(new Diff(ItemType.POINT, DiffType.CHANGED,
                                DiffReason.GEOMETRY_OR_TOPOLOGY, this.before, this.after,
                                identifier));
                    }
                }
                else if (baseEntity instanceof Relation)
                {
                    if (differentRelations((Relation) baseEntity, (Relation) alterEntity))
                    {
                        this.differences.add(new Diff(ItemType.RELATION, DiffType.CHANGED,
                                DiffReason.RELATION_TOPOLOGY, this.before, this.after, identifier));
                    }
                }
            }
        }
        this.counter.summary();
        return this;
    }

    public Atlas getAfter()
    {
        return this.after;
    }

    public Atlas getBefore()
    {
        return this.before;
    }

    public SortedSet<Diff> getDifferences()
    {
        return this.differences;
    }

    /**
     * Similar to the regular {@link AtlasDelta#toString}, but attempts to make the string more
     * friendly to diff viewing.
     *
     * @return the diff string
     */
    public String toDiffViewFriendlyString()
    {
        return Diff.toDiffViewFriendlyString(this.differences);
    }

    public String toGeoJson()
    {
        return Diff.toGeoJson(this.differences);
    }

    public String toGeoJson(final Predicate<Diff> filter)
    {
        return Diff.toGeoJson(this.differences, filter);
    }

    public String toRelationsGeoJson()
    {
        return Diff.toRelationsGeoJson(this.differences);
    }

    public String toRelationsGeoJson(final Predicate<Diff> filter)
    {
        return Diff.toRelationsGeoJson(this.differences, filter);
    }

    @Override
    public String toString()
    {
        return Diff.toString(this.differences);
    }

    private boolean differentAreas(final Area baseArea, final Area alterArea)
    {
        try
        {
            if (!baseArea.asPolygon().equals(alterArea.asPolygon()))
            {
                return true;
            }
            return false;
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to compare areas {} and {}", baseArea, alterArea, e);
        }
    }

    private boolean differentEdges(final Edge baseEdge, final Edge alterEdge)
    {
        try
        {
            boolean result = false;
            if (!baseEdge.asPolyLine().equals(alterEdge.asPolyLine()))
            {
                result = true;
            }
            if (!result && baseEdge.start().getIdentifier() != alterEdge.start().getIdentifier())
            {
                result = true;
            }
            if (!result && baseEdge.end().getIdentifier() != alterEdge.end().getIdentifier())
            {
                result = true;
            }
            if (result)
            {
                // Make sure that there is not way to find a match with the other polylines
                result = !hasGoodMatch(baseEdge, alterEdge.getAtlas());
            }
            return result;
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to compare edges {} and {}", baseEdge, alterEdge, e);
        }
    }

    private boolean differentEdgeSet(final SortedSet<Edge> baseEdges,
            final SortedSet<Edge> alterEdges)
    {
        final boolean differentEdgeSetBasic = differentEdgeSetBasic(baseEdges, alterEdges);
        final boolean differentEdgeSetWithMatch = differentEdgeSetWithMatch(baseEdges, alterEdges);
        return differentEdgeSetBasic && differentEdgeSetWithMatch;
    }

    private boolean differentEdgeSetBasic(final SortedSet<Edge> baseEdges,
            final SortedSet<Edge> alterEdges)
    {
        if (baseEdges.size() != alterEdges.size())
        {
            return true;
        }
        final Iterator<Edge> baseInEdgeIterator = baseEdges.iterator();
        final Iterator<Edge> alterInEdgeIterator = alterEdges.iterator();
        for (int i = 0; i < baseEdges.size(); i++)
        {
            final Edge baseInEdge = baseInEdgeIterator.next();
            final Edge alterInEdge = alterInEdgeIterator.next();
            if (baseInEdge.getIdentifier() != alterInEdge.getIdentifier())
            {
                return true;
            }
        }
        return false;
    }

    private boolean differentEdgeSetWithMatch(final Set<Edge> baseEdges, final Set<Edge> alterEdges)
    {
        if (baseEdges.isEmpty() && alterEdges.isEmpty())
        {
            return false;
        }
        boolean baseToAlterResult = baseEdges.isEmpty();
        for (final Edge edge : baseEdges)
        {
            if (alterEdges.isEmpty() || !hasPerfectMatch(edge, alterEdges))
            {
                baseToAlterResult = true;
                break;
            }
        }
        boolean alterToBaseResult = alterEdges.isEmpty();
        for (final Edge edge : alterEdges)
        {
            if (baseEdges.isEmpty() || !hasPerfectMatch(edge, baseEdges))
            {
                alterToBaseResult = true;
                break;
            }
        }
        return baseToAlterResult && alterToBaseResult;
    }

    private boolean differentInRelation(final AtlasEntity baseEntity, final AtlasEntity alterEntity)
    {
        try
        {
            final Set<Relation> baseRelations = baseEntity.relations();
            final Set<Relation> alterRelations = alterEntity.relations();
            if (baseRelations.size() != alterRelations.size())
            {
                return true;
            }
            for (final Relation baseRelation : baseRelations)
            {
                Relation alterRelation = null;
                for (final Relation alterRelationCandidate : alterRelations)
                {
                    if (alterRelationCandidate.getIdentifier() == baseRelation.getIdentifier())
                    {
                        alterRelation = alterRelationCandidate;
                        break;
                    }
                }
                if (alterRelation == null)
                {
                    // The two relation sets are different
                    return true;
                }

                // Index of the member in the Relation's member list
                int baseIndex = -1;
                int alterIndex = -1;
                final RelationMemberList baseMembers = baseRelation.members();
                final RelationMemberList alterMembers = alterRelation.members();
                for (int j = 0; j < baseMembers.size(); j++)
                {
                    final RelationMember baseMember = baseMembers.get(j);
                    if (baseMember.getEntity().getIdentifier() == baseEntity.getIdentifier())
                    {
                        baseIndex = j;
                    }
                }
                for (int j = 0; j < alterMembers.size(); j++)
                {
                    final RelationMember alterMember = alterMembers.get(j);
                    if (alterMember.getEntity().getIdentifier() == baseEntity.getIdentifier())
                    {
                        alterIndex = j;
                    }
                }
                if (baseIndex < 0 || alterIndex < 0)
                {
                    throw new CoreException("Corrupted Atlas dataset.");
                }
                if (baseIndex != alterIndex)
                {
                    // Order changed
                    return true;
                }
                if (!baseMembers.get(baseIndex).getRole()
                        .equals(alterMembers.get(alterIndex).getRole()))
                {
                    // Role changed
                    return true;
                }
                if (baseMembers.get(baseIndex).getEntity().getType() != alterMembers.get(alterIndex)
                        .getEntity().getType())
                {
                    // Type changed
                    return true;
                }
            }
            return false;
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to compare relations for {} and {}", baseEntity,
                    alterEntity, e);
        }
    }

    private boolean differentLines(final Line baseLine, final Line alterLine)
    {
        try
        {
            if (!baseLine.asPolyLine().equals(alterLine.asPolyLine()))
            {
                return true;
            }
            return false;
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to compare line geometries for {} and {}", baseLine,
                    alterLine, e);
        }
    }

    private boolean differentNodes(final Node baseNode, final Node alterNode)
    {
        try
        {
            if (!baseNode.getLocation().equals(alterNode.getLocation()))
            {
                return true;
            }
            if (differentEdgeSet(baseNode.inEdges(), alterNode.inEdges()))
            {
                return true;
            }
            if (differentEdgeSet(baseNode.outEdges(), alterNode.outEdges()))
            {
                return true;
            }
            return false;
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to compare nodes {} and {}", baseNode, alterNode, e);
        }
    }

    private boolean differentPoints(final Point basePoint, final Point alterPoint)
    {
        try
        {
            if (!basePoint.getLocation().equals(alterPoint.getLocation()))
            {
                return true;
            }
            return false;
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to compare points {} and {}", basePoint, alterPoint, e);
        }
    }

    private boolean differentRelationMemberListsWithMatch(final RelationMemberList baseMembers,
            final RelationMemberList alterMembers)
    {
        final SortedSet<Edge> baseEdges = Iterables.stream(baseMembers)
                .map(member -> member.getEntity()).filter(entity -> entity instanceof Edge)
                .map(entity -> (Edge) entity).collectToSortedSet();
        final SortedSet<Edge> alterEdges = Iterables.stream(alterMembers)
                .map(member -> member.getEntity()).filter(entity -> entity instanceof Edge)
                .map(entity -> (Edge) entity).collectToSortedSet();
        return differentEdgeSet(baseEdges, alterEdges);
    }

    private boolean differentRelations(final Relation baseRelation, final Relation alterRelation)
    {
        try
        {
            final RelationMemberList baseMembers = baseRelation.members();
            final RelationMemberList alterMembers = alterRelation.members();
            return !baseMembers.equals(alterMembers)
                    && !differentRelationMemberListsWithMatch(baseMembers, alterMembers);
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to compare relations {} and {}", baseRelation,
                    alterRelation, e);
        }
    }

    private boolean hasGoodMatch(final Edge edge, final Atlas other)
    {
        if (this.withGeometryMatching)
        {
            final Rectangle bounds = edge.bounds();
            return hasPerfectMatch(edge, other.edgesIntersecting(bounds,
                    otherEdge -> edge.getOsmIdentifier() == otherEdge.getOsmIdentifier()));
        }
        return false;
    }

    private boolean hasPerfectMatch(final Edge edge, final Iterable<Edge> otherEdges)
    {
        if (this.withGeometryMatching)
        {
            final PolyLine source = edge.asPolyLine();
            final List<PolyLine> candidates = Iterables.stream(otherEdges).map(Edge::asPolyLine)
                    .collectToList();
            final Optional<PolyLineRoute> match = source.costDistanceToOneWay(candidates)
                    .match(Distance.ZERO);
            if (match.isPresent() && match.get().getCost().isLessThanOrEqualTo(Distance.ZERO))
            {
                // The edge was probably split by way sectioning without changing itself.
                logger.trace("Edge {} from {} has no equal member but found a match with no cost.",
                        edge, edge.getAtlas().getName());
                return true;
            }
        }
        return false;
    }
}
