package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.change.serializer.FeatureChangeGeoJsonSerializer;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * Single feature change, does not include any consistency checks.
 * <p>
 * To add a new, non existing feature: {@link ChangeType} is ADD, and the included reference needs
 * to contain all the information related to that new feature.
 * <p>
 * To modify an existing feature: {@link ChangeType} is ADD, and the included reference needs to
 * contain the only the changed information related to that changed feature.
 * <p>
 * To remove an existing feature: {@link ChangeType} is REMOVE. The included reference's only
 * feature that needs to match the existing feature is the identifier.
 *
 * @author matthieun
 * @author lcram
 */
public class FeatureChange implements Located, Serializable
{
    private static final long serialVersionUID = 9172045162819925515L;
    private static final BinaryOperator<Map<String, String>> tagMerger = Maps::withMaps;
    private static final BinaryOperator<Set<Long>> directReferenceMerger = Sets::withSets;
    private static final BinaryOperator<SortedSet<Long>> directReferenceMergerSorted = Sets::withSortedSets;
    private static final BinaryOperator<Set<Long>> directReferenceMergerLoose = (left,
            right) -> Sets.withSets(false, left, right);
    private static final BinaryOperator<RelationBean> relationBeanMerger = RelationBean::merge;

    private final ChangeType changeType;
    private final AtlasEntity updatedView;
    private final AtlasEntity beforeView;

    /**
     * Create a new ADD {@link FeatureChange} with a given complete entity. TODO should we leave
     * this, or remove it?
     *
     * @param updatedView
     *            the updated reference
     * @return a new ADD {@link FeatureChange}
     * @deprecated please use {@link FeatureChange#add2} instead
     */
    @Deprecated
    public static FeatureChange add(final AtlasEntity updatedView)
    {
        return FeatureChange.add2(updatedView, null);
    }

    public static FeatureChange add2(final AtlasEntity updatedView, final AtlasEntity beforeView)
    {
        final AtlasEntity beforeViewUpdatesOnly;

        /*
         * This is the case where we are adding a brand new feature. There is no beforeView. Callers
         * indicate this by passing "null" for the before view.
         */
        if (beforeView == null)
        {
            return new FeatureChange(ChangeType.ADD, updatedView, null);
        }

        /*
         * If the user indicated an update-like ADD (by specifying a non-null beforeView) but the
         * identifiers don't match, we have a problem.
         */
        if (updatedView.getIdentifier() != beforeView.getIdentifier())
        {
            throw new CoreException(
                    "Update-like ADD attempted but updatedView ID ({}) did not match beforeView ID ({})",
                    updatedView.getIdentifier(), beforeView.getIdentifier());
        }

        /*
         * Same check as above, but for ItemType. Here, we are guaranteed that at least the IDs
         * match due to the above check.
         */
        if (updatedView.getType() != beforeView.getType())
        {
            throw new CoreException(
                    "Update-like ADD attempted on ID ({}) but updatedView ItemType ({}) did not match beforeView ItemType ({})",
                    beforeView.getIdentifier(), updatedView.getType(), beforeView.getType());
        }

        /*
         * Make type specific updates first.
         */
        switch (updatedView.getType())
        {
            /*
             * Area specific updates. The only Area-specific field is the polygon.
             */
            case AREA:
                final Area updatedAreaView = (Area) updatedView;
                final Area beforeAreaView = (Area) beforeView;
                beforeViewUpdatesOnly = CompleteArea.shallowFrom(beforeAreaView);
                if (updatedAreaView.asPolygon() != null)
                {
                    ((CompleteArea) beforeViewUpdatesOnly).withPolygon(beforeAreaView.asPolygon());
                }
                break;
            /*
             * Edge specific updates. The Edge-specific fields are the polyline and the start/end
             * nodes.
             */
            case EDGE:
                final Edge updatedEdgeView = (Edge) updatedView;
                final Edge beforeEdgeView = (Edge) beforeView;
                beforeViewUpdatesOnly = CompleteEdge.shallowFrom(updatedEdgeView);
                if (updatedEdgeView.asPolyLine() != null)
                {
                    ((CompleteEdge) beforeViewUpdatesOnly)
                            .withPolyLine(beforeEdgeView.asPolyLine());
                }
                if (updatedEdgeView.start() != null)
                {
                    ((CompleteEdge) beforeViewUpdatesOnly)
                            .withStartNodeIdentifier(beforeEdgeView.start().getIdentifier());
                }
                if (updatedEdgeView.end() != null)
                {
                    ((CompleteEdge) beforeViewUpdatesOnly)
                            .withEndNodeIdentifier(beforeEdgeView.end().getIdentifier());
                }
                break;
            /*
             * Line specific updates. The only Line-specific field is the polyline.
             */
            case LINE:
                final Line updatedLineView = (Line) updatedView;
                final Line beforeLineView = (Line) beforeView;
                beforeViewUpdatesOnly = CompleteLine.shallowFrom(updatedLineView);
                if (updatedLineView.asPolyLine() != null)
                {
                    ((CompleteLine) beforeViewUpdatesOnly)
                            .withPolyLine(beforeLineView.asPolyLine());
                }
                break;
            /*
             * Node specific updates. The Node-specific fields are the location and the in/out edge
             * sets.
             */
            case NODE:
                final Node updatedNodeView = (Node) updatedView;
                final Node beforeNodeView = (Node) beforeView;
                beforeViewUpdatesOnly = CompleteNode.shallowFrom(updatedNodeView);
                if (updatedNodeView.getLocation() != null)
                {
                    ((CompleteNode) beforeViewUpdatesOnly)
                            .withLocation(beforeNodeView.getLocation());
                }
                if (updatedNodeView.inEdges() != null)
                {
                    ((CompleteNode) beforeViewUpdatesOnly).withInEdges(beforeNodeView.inEdges());
                }
                if (updatedNodeView.outEdges() != null)
                {
                    ((CompleteNode) beforeViewUpdatesOnly).withOutEdges(beforeNodeView.outEdges());
                }
                break;
            /*
             * Point specific updates. The only Point-specific field is the location.
             */
            case POINT:
                final Point updatedPointView = (Point) updatedView;
                final Point beforePointView = (Point) beforeView;
                beforeViewUpdatesOnly = CompletePoint.shallowFrom(updatedPointView);
                if (updatedPointView.getLocation() != null)
                {
                    ((CompletePoint) beforeViewUpdatesOnly)
                            .withLocation(beforePointView.getLocation());
                }
                break;
            /*
             * Relation specific updates. The only Relation-specific field is the member list.
             */
            /*
             * TODO do we need to handle the allKnownOsmMembers case? I am not convinced we even
             * need this field in Relation. I don't see it called anywhere in the codebase (except
             * in a few tests).
             */
            case RELATION:
                final Relation updatedRelationView = (Relation) updatedView;
                final Relation beforeRelationView = (Relation) beforeView;
                beforeViewUpdatesOnly = CompleteRelation.shallowFrom(updatedRelationView);
                if (updatedRelationView.members() != null)
                {
                    ((CompleteRelation) beforeViewUpdatesOnly)
                            .withMembers(beforeRelationView.members());
                }
                break;
            default:
                throw new CoreException("Unknown entity type {}", updatedView.getType());
        }

        /*
         * Add before view of the tags if the updatedView updated the tags.
         */
        final Map<String, String> updatedViewTags = updatedView.getTags();
        if (updatedViewTags != null)
        {
            ((CompleteEntity) beforeViewUpdatesOnly).withTags(beforeView.getTags());
        }

        /*
         * Add before view of relations if updatedView updated relations.
         */
        final Set<Relation> updatedViewRelations = updatedView.relations();
        if (updatedViewRelations != null)
        {
            ((CompleteEntity) beforeViewUpdatesOnly).withRelations(beforeView.relations());
        }

        return new FeatureChange(ChangeType.ADD, updatedView, beforeViewUpdatesOnly);
    }

    public static FeatureChange remove(final AtlasEntity reference)
    {
        return new FeatureChange(ChangeType.REMOVE, reference, null);
    }

    public FeatureChange(final ChangeType changeType, final AtlasEntity updatedView)
    {
        this(changeType, updatedView, null);
    }

    public FeatureChange(final ChangeType changeType, final AtlasEntity updatedView,
            final AtlasEntity beforeView)
    {
        if (updatedView == null)
        {
            throw new CoreException("reference cannot be null.");
        }
        if (!(updatedView instanceof CompleteEntity))
        {
            throw new CoreException(
                    "FeatureChange requires CompleteEntity, found reference of type {}",
                    updatedView.getClass().getName());
        }
        if (changeType == null)
        {
            throw new CoreException("changeType cannot be null.");
        }
        this.changeType = changeType;
        this.updatedView = updatedView;
        this.beforeView = beforeView;
        this.validateUsefulFeatureChange();
    }

    @Override
    public Rectangle bounds()
    {
        return this.updatedView.bounds();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof FeatureChange)
        {
            final FeatureChange that = (FeatureChange) other;
            return this.getChangeType() == that.getChangeType()
                    && this.getReference().equals(that.getReference());
        }
        return false;
    }

    public ChangeType getChangeType()
    {
        return this.changeType;
    }

    public long getIdentifier()
    {
        return getReference().getIdentifier();
    }

    public ItemType getItemType()
    {
        return ItemType.forEntity(getReference());
    }

    public AtlasEntity getReference()
    {
        return this.updatedView;
    }

    /**
     * Get a tag based on key post changes.
     *
     * @param key
     *            - The tag key to look for.
     * @return - the changed value of the tag, if available.
     */
    public Optional<String> getTag(final String key)
    {
        return this.getReference().getTag(key);
    }

    /**
     * Get the changed tags.
     *
     * @return Map - the changed tags.
     */
    public Map<String, String> getTags()
    {
        return this.getReference().getTags();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.changeType, this.updatedView);
    }

    /**
     * Merge two feature changes together. If it cannot succeed, this method will throw a
     * {@link CoreException} explaining why.
     *
     * @param other
     *            The other to merge into this one.
     * @return The merged {@link FeatureChange}
     */
    public FeatureChange merge(final FeatureChange other)
    {
        try
        {
            if (this.getIdentifier() != other.getIdentifier()
                    || this.getItemType() != other.getItemType()
                    || this.getChangeType() != other.getChangeType())
            {
                throw new CoreException(
                        "Cannot merge two feature changes that are not of the same type.");
            }
            if (this.getChangeType() == ChangeType.REMOVE)
            {
                return this;
            }
            if (this.getChangeType() == ChangeType.ADD)
            {
                final AtlasEntity thisReference = this.getReference();
                final AtlasEntity thatReference = other.getReference();
                final Map<String, String> mergedTags = mergedMember("tags", thisReference,
                        thatReference, Taggable::getTags, Optional.of(tagMerger));
                final Set<Long> mergedParentRelations = mergedMember("parentRelations",
                        thisReference, thatReference,
                        atlasEntity -> atlasEntity.relations() == null ? null
                                : atlasEntity.relations().stream().map(Relation::getIdentifier)
                                        .collect(Collectors.toSet()),
                        Optional.of(directReferenceMergerLoose));
                if (thisReference instanceof LocationItem)
                {
                    return mergeLocationItems(other, mergedTags, mergedParentRelations);
                }
                else if (thisReference instanceof LineItem)
                {
                    return mergeLineItems(other, mergedTags, mergedParentRelations);
                }
                else if (thisReference instanceof Area)
                {
                    return mergeAreas(other, mergedTags, mergedParentRelations);
                }
                else
                {
                    // Relation
                    return mergeRelations(other, mergedTags, mergedParentRelations);
                }

            }
            throw new CoreException("Unable to merge {} and {}", this, other);
        }
        catch (final Exception e)
        {
            throw new CoreException("Cannot merge two feature changes {} and {}.", this, other, e);
        }
    }

    /**
     * Save a GeoJSON representation of that feature change.
     *
     * @param resource
     *            The {@link WritableResource} to save the GeoJSON to.
     */
    public void save(final WritableResource resource)
    {
        new FeatureChangeGeoJsonSerializer().accept(this, resource);
    }

    public String toGeoJson()
    {
        return new FeatureChangeGeoJsonSerializer().convert(this);
    }

    @Override
    public String toString()
    {
        return "FeatureChange [changeType=" + this.changeType + ", reference={"
                + this.updatedView.getType() + "," + this.updatedView.getIdentifier() + "}, tags="
                + getTags() + ", bounds=" + bounds() + "]";
    }

    private FeatureChange mergeAreas(final FeatureChange other,
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getReference();
        final AtlasEntity thatReference = other.getReference();
        final Polygon mergedPolygon = mergedMember("polygon", thisReference, thatReference,
                atlasEntity -> ((Area) atlasEntity).asPolygon(), Optional.empty());

        CompleteArea result = new CompleteArea(getIdentifier(), mergedPolygon, mergedTags,
                mergedParentRelations);
        if (result.bounds() == null)
        {
            if (bounds() != null)
            {
                result = result.withAggregateBoundsExtendedUsing(bounds());
            }
            else if (other.bounds() != null)
            {
                result = result.withAggregateBoundsExtendedUsing(other.bounds());
            }
        }
        return FeatureChange.add(result);
    }

    private <M> M mergedMember(final String memberName, final AtlasEntity left,
            final AtlasEntity right, final Function<AtlasEntity, M> memberExtractor,
            final Optional<BinaryOperator<M>> memberMergerOption)
    {
        final M result;
        final M leftMember = memberExtractor.apply(left);
        final M rightMember = memberExtractor.apply(right);
        if (leftMember != null && rightMember != null)
        {
            // Both are not null, merge evaluated
            if (leftMember.equals(rightMember))
            {
                // They are equal, arbitrarily pick one.
                result = leftMember;
            }
            else if (memberMergerOption.isPresent())
            {
                // They are unequal, but we can attempt a merge
                try
                {
                    result = memberMergerOption.get().apply(leftMember, rightMember);
                }
                catch (final CoreException e)
                {
                    throw new CoreException("Attempted merge failed for {}: {} and {}", memberName,
                            leftMember, rightMember, e);
                }
            }
            else
            {
                // They are unequal and we do not have a tool to merge them.
                throw new CoreException("Conflicting members, no merge option for {}: {} and {}",
                        memberName, leftMember, rightMember);
            }
        }
        else
        {
            // One is not null, or both are.
            if (leftMember != null)
            {
                result = leftMember;
            }
            else
            {
                result = rightMember;
            }
        }
        return result;
    }

    private FeatureChange mergeLineItems(final FeatureChange other, // NOSONAR
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getReference();
        final AtlasEntity thatReference = other.getReference();
        final PolyLine mergedPolyLine = mergedMember("polyLine", thisReference, thatReference,
                atlasEntity -> ((LineItem) atlasEntity).asPolyLine(), Optional.empty());
        if (thisReference instanceof Edge)
        {
            final Long mergedStartNodeIdentifier = mergedMember("startNode", thisReference,
                    thatReference, edge -> ((Edge) edge).start() == null ? null
                            : ((Edge) edge).start().getIdentifier(),
                    Optional.empty());
            final Long mergedEndNodeIdentifier = mergedMember("endNode", thisReference,
                    thatReference, edge -> ((Edge) edge).end() == null ? null
                            : ((Edge) edge).end().getIdentifier(),
                    Optional.empty());
            CompleteEdge result = new CompleteEdge(getIdentifier(), mergedPolyLine, mergedTags,
                    mergedStartNodeIdentifier, mergedEndNodeIdentifier, mergedParentRelations);
            if (result.bounds() == null)
            {
                if (bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(bounds());
                }
                else if (other.bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(other.bounds());
                }
            }
            return FeatureChange.add(result);
        }
        else
        {
            // Line
            CompleteLine result = new CompleteLine(getIdentifier(), mergedPolyLine, mergedTags,
                    mergedParentRelations);
            if (result.bounds() == null)
            {
                if (bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(bounds());
                }
                else if (other.bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(other.bounds());
                }
            }
            return FeatureChange.add(result);
        }
    }

    private FeatureChange mergeLocationItems(final FeatureChange other, // NOSONAR
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getReference();
        final AtlasEntity thatReference = other.getReference();
        final Location mergedLocation = mergedMember("location", thisReference, thatReference,
                atlasEntity -> ((LocationItem) atlasEntity).getLocation(), Optional.empty());
        if (thisReference instanceof Node)
        {
            final SortedSet<Long> mergedInEdgeIdentifiers = mergedMember("inEdgeIdentifiers",
                    thisReference, thatReference,
                    atlasEntity -> ((Node) atlasEntity).inEdges() == null ? null
                            : ((Node) atlasEntity).inEdges().stream().map(Edge::getIdentifier)
                                    .collect(Collectors.toCollection(TreeSet::new)),
                    Optional.of(directReferenceMergerSorted));
            final SortedSet<Long> mergedOutEdgeIdentifiers = mergedMember("outEdgeIdentifiers",
                    thisReference, thatReference,
                    atlasEntity -> ((Node) atlasEntity).outEdges() == null ? null
                            : ((Node) atlasEntity).outEdges().stream().map(Edge::getIdentifier)
                                    .collect(Collectors.toCollection(TreeSet::new)),
                    Optional.of(directReferenceMergerSorted));
            CompleteNode result = new CompleteNode(getIdentifier(), mergedLocation, mergedTags,
                    mergedInEdgeIdentifiers, mergedOutEdgeIdentifiers, mergedParentRelations);
            if (result.bounds() == null)
            {
                if (bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(bounds());
                }
                else if (other.bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(other.bounds());
                }
            }
            return FeatureChange.add(result);
        }
        else
        {
            // Point
            CompletePoint result = new CompletePoint(getIdentifier(), mergedLocation, mergedTags,
                    mergedParentRelations);
            if (result.bounds() == null)
            {
                if (bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(bounds());
                }
                else if (other.bounds() != null)
                {
                    result = result.withAggregateBoundsExtendedUsing(other.bounds());
                }
            }
            return FeatureChange.add(result);
        }
    }

    private FeatureChange mergeRelations(final FeatureChange other,
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getReference();
        final AtlasEntity thatReference = other.getReference();

        final RelationBean mergedMembers = mergedMember("relationMembers", thisReference,
                thatReference, entity -> ((Relation) entity).members() == null ? null
                        : ((Relation) entity).members().asBean(),
                Optional.of(relationBeanMerger));
        final Rectangle mergedBounds = Rectangle.forLocated(thisReference, thatReference);
        final Long mergedOsmRelationIdentifier = mergedMember("osmRelationIdentifier",
                thisReference, thatReference, entity -> ((Relation) entity).getOsmIdentifier(),
                Optional.empty());
        final Set<Long> mergedAllRelationsWithSameOsmIdentifierSet = mergedMember(
                "allRelationsWithSameOsmIdentifier", thisReference, thatReference,
                atlasEntity -> ((Relation) atlasEntity).allRelationsWithSameOsmIdentifier() == null
                        ? null
                        : ((Relation) atlasEntity).allRelationsWithSameOsmIdentifier().stream()
                                .map(Relation::getIdentifier).collect(Collectors.toSet()),
                Optional.of(directReferenceMerger));
        final List<Long> mergedAllRelationsWithSameOsmIdentifier = mergedAllRelationsWithSameOsmIdentifierSet == null
                ? null
                : mergedAllRelationsWithSameOsmIdentifierSet.stream().collect(Collectors.toList());
        final RelationBean mergedAllKnownMembers = mergedMember("allKnownOsmMembers", thisReference,
                thatReference,
                entity -> ((Relation) entity).allKnownOsmMembers() == null ? null
                        : ((Relation) entity).allKnownOsmMembers().asBean(),
                Optional.of(relationBeanMerger));

        return FeatureChange.add(new CompleteRelation(getIdentifier(), mergedTags, mergedBounds,
                mergedMembers, mergedAllRelationsWithSameOsmIdentifier, mergedAllKnownMembers,
                mergedOsmRelationIdentifier, mergedParentRelations));
    }

    private void validateUsefulFeatureChange()
    {
        if (this.changeType == ChangeType.ADD && this.updatedView instanceof CompleteEntity
                && ((CompleteEntity) this.updatedView).isSuperShallow())
        {
            throw new CoreException("{} does not contain anything useful.", this);
        }
    }
}
