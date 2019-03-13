package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChangeMergers.MergedMemberBean;
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

/**
 * Single feature change, does not include any consistency checks.
 * <p>
 * To add a new, non existing feature: {@link ChangeType} is ADD, and the included reference needs
 * to contain all the information related to that new feature.
 * <p>
 * To modify an existing feature: {@link ChangeType} is ADD, and the included reference needs to
 * contain the only the changed information related to that changed feature. You must also include a
 * reference to the before view of the entity.
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

    private final ChangeType changeType;
    private final AtlasEntity updatedView;
    private AtlasEntity beforeView;

    public static FeatureChange add(final AtlasEntity updatedView)
    {
        return new FeatureChange(ChangeType.ADD, updatedView);
    }

    public static FeatureChange add(final AtlasEntity updatedView, final Atlas atlasContext)
    {
        return new FeatureChange(ChangeType.ADD, updatedView).withAtlasContext(atlasContext);
    }

    public static FeatureChange remove(final AtlasEntity reference)
    {
        return new FeatureChange(ChangeType.REMOVE, reference);
    }

    /**
     * This constructor is package private, and should be used for unit testing purposes only. Users
     * who wish to specify a before view should be using
     * {@link FeatureChange#withAtlasContext(Atlas)}.
     *
     * @param changeType
     *            the change type
     * @param updatedView
     *            the updated entity
     * @param beforeView
     *            the before entity
     */
    FeatureChange(final ChangeType changeType, final AtlasEntity updatedView,
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

    public FeatureChange(final ChangeType changeType, final AtlasEntity updatedView)
    {
        this(changeType, updatedView, null);
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
                    && this.getUpdatedView().equals(that.getUpdatedView());
        }
        return false;
    }

    public AtlasEntity getBeforeView()
    {
        return this.beforeView;
    }

    public ChangeType getChangeType()
    {
        return this.changeType;
    }

    public long getIdentifier()
    {
        return getUpdatedView().getIdentifier();
    }

    public ItemType getItemType()
    {
        return ItemType.forEntity(getUpdatedView());
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
        return this.getUpdatedView().getTag(key);
    }

    /**
     * Get the changed tags.
     *
     * @return Map - the changed tags.
     */
    public Map<String, String> getTags()
    {
        return this.getUpdatedView().getTags();
    }

    public AtlasEntity getUpdatedView()
    {
        return this.updatedView;
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
        // TODO we need to handle merging the before view properly
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
                final AtlasEntity thisReference = this.getUpdatedView();
                final AtlasEntity thatReference = other.getUpdatedView();
                final Map<String, String> mergedTags = FeatureChangeMergers
                        .mergeMember_OldStrategy("tags", thisReference, thatReference,
                                Taggable::getTags, FeatureChangeMergers.tagMerger);
                final Set<Long> mergedParentRelations = FeatureChangeMergers
                        .mergeMember_OldStrategy("parentRelations", thisReference,
                                thatReference,
                                atlasEntity -> atlasEntity.relations() == null ? null
                                        : atlasEntity.relations().stream()
                                                .map(Relation::getIdentifier)
                                                .collect(Collectors.toSet()),
                                FeatureChangeMergers.directReferenceMergerLoose);

                if (thisReference instanceof LocationItem)
                {
                    return mergeLocationItems_NewlyProposed(other, mergedTags, mergedParentRelations);
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
        catch (final Exception exception)
        {
            throw new CoreException("Cannot merge two feature changes {} and {}.", this, other,
                    exception);
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

    /**
     * Specify the Atlas on which this {@link FeatureChange} is based. {@link FeatureChange} objects
     * with a contextual Atlas are able to calculate their before view, and so are able to leverage
     * richer and more robust merging mechanics.
     *
     * @param atlas
     *            the contextual atlas
     * @return the updated {@link FeatureChange}
     */
    public FeatureChange withAtlasContext(final Atlas atlas)
    {
        // TODO before view for REMOVE type?
        if (this.changeType == ChangeType.ADD)
        {
            computeBeforeViewUsingAtlasContext(atlas);
        }
        return this;
    }

    /**
     * Compute the beforeView using a given afterView and Atlas context. The beforeView is a
     * CompleteEntity, and will contain only those fields which have been updated in the afterView.
     * E.g. Suppose the afterView is a CompleteNode with ID 1, and the only updated field is the tag
     * Map. Then the beforeView will be a CompleteNode with ID 1, where the only populated field is
     * the tag Map. However, the afterView's tag Map will be fetched from the Atlas context. Having
     * a beforeView for each {@link FeatureChange} allows for more robust merging strategies.
     *
     * @param atlas
     *            the atlas context
     */
    private void computeBeforeViewUsingAtlasContext(final Atlas atlas) // NOSONAR
    {
        if (atlas == null)
        {
            throw new CoreException("Atlas context cannot be null");
        }

        AtlasEntity beforeViewUpdatesOnly;
        final AtlasEntity beforeViewFromAtlas = atlas.entity(this.updatedView.getIdentifier(),
                this.updatedView.getType());
        if (beforeViewFromAtlas == null)
        {
            throw new CoreException("Could not find {} with ID {} in atlas context",
                    this.updatedView.getType(), this.updatedView.getIdentifier());
        }

        /*
         * Make type specific updates first.
         */
        switch (this.updatedView.getType())
        {
            /*
             * Area specific updates. The only Area-specific field is the polygon.
             */
            case AREA:
                final Area updatedAreaView = (Area) this.updatedView;
                final Area beforeAreaView = (Area) beforeViewFromAtlas;
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
                final Edge updatedEdgeView = (Edge) this.updatedView;
                final Edge beforeEdgeView = (Edge) beforeViewFromAtlas;
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
                final Line updatedLineView = (Line) this.updatedView;
                final Line beforeLineView = (Line) beforeViewFromAtlas;
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
                final Node updatedNodeView = (Node) this.updatedView;
                final Node beforeNodeView = (Node) beforeViewFromAtlas;
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
                final Point updatedPointView = (Point) this.updatedView;
                final Point beforePointView = (Point) beforeViewFromAtlas;
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
                final Relation updatedRelationView = (Relation) this.updatedView;
                final Relation beforeRelationView = (Relation) beforeViewFromAtlas;
                beforeViewUpdatesOnly = CompleteRelation.shallowFrom(updatedRelationView);
                if (updatedRelationView.members() != null)
                {
                    ((CompleteRelation) beforeViewUpdatesOnly)
                            .withMembers(beforeRelationView.members());
                }
                break;
            default:
                throw new CoreException("Unknown entity type {}", this.updatedView.getType());
        }

        /*
         * Add before view of the tags if the updatedView updated the tags.
         */
        final Map<String, String> updatedViewTags = this.updatedView.getTags();
        if (updatedViewTags != null)
        {
            ((CompleteEntity) beforeViewUpdatesOnly).withTags(beforeViewFromAtlas.getTags());
        }

        /*
         * Add before view of relations if updatedView updated relations.
         */
        final Set<Relation> updatedViewRelations = this.updatedView.relations();
        if (updatedViewRelations != null)
        {
            ((CompleteEntity) beforeViewUpdatesOnly).withRelations(beforeViewFromAtlas.relations());
        }

        this.beforeView = beforeViewUpdatesOnly;
    }

    private FeatureChange mergeAreas(final FeatureChange other,
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getUpdatedView();
        final AtlasEntity thatReference = other.getUpdatedView();
        final Polygon mergedPolygon = FeatureChangeMergers.mergeMember_OldStrategy("polygon",
                thisReference, thatReference, atlasEntity -> ((Area) atlasEntity).asPolygon(),
                null);

        final CompleteArea result = new CompleteArea(getIdentifier(), mergedPolygon, mergedTags,
                mergedParentRelations);
        if (result.bounds() == null)
        {
            throw new CoreException("TODO CompleteArea result bounds null, investigate");
        }
        return FeatureChange.add(result);
    }

    private FeatureChange mergeLineItems(final FeatureChange other, // NOSONAR
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getUpdatedView();
        final AtlasEntity thatReference = other.getUpdatedView();
        final PolyLine mergedPolyLine = FeatureChangeMergers.mergeMember_OldStrategy(
                "polyLine", thisReference, thatReference,
                atlasEntity -> ((LineItem) atlasEntity).asPolyLine(), null);
        if (thisReference instanceof Edge)
        {
            final Long mergedStartNodeIdentifier = FeatureChangeMergers
                    .mergeMember_OldStrategy("startNode", thisReference, thatReference,
                            edge -> ((Edge) edge).start() == null ? null
                                    : ((Edge) edge).start().getIdentifier(),
                            null);
            final Long mergedEndNodeIdentifier = FeatureChangeMergers.mergeMember_OldStrategy(
                    "endNode", thisReference, thatReference, edge -> ((Edge) edge).end() == null
                            ? null : ((Edge) edge).end().getIdentifier(),
                    null);
            final CompleteEdge result = new CompleteEdge(getIdentifier(), mergedPolyLine,
                    mergedTags, mergedStartNodeIdentifier, mergedEndNodeIdentifier,
                    mergedParentRelations);
            if (result.bounds() == null)
            {
                throw new CoreException("TODO CompleteEdge result bounds null, investigate");
            }
            return FeatureChange.add(result);
        }
        else
        {
            // Line
            final CompleteLine result = new CompleteLine(getIdentifier(), mergedPolyLine,
                    mergedTags, mergedParentRelations);
            if (result.bounds() == null)
            {
                throw new CoreException("TODO CompleteLine result bounds null, investigate");
            }
            return FeatureChange.add(result);
        }
    }

    private FeatureChange mergeLocationItems(final FeatureChange other, // NOSONAR
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getUpdatedView();
        final AtlasEntity thatReference = other.getUpdatedView();
        final Location mergedLocation = FeatureChangeMergers.mergeMember_OldStrategy(
                "location", thisReference, thatReference,
                atlasEntity -> ((LocationItem) atlasEntity).getLocation(), null);
        if (thisReference instanceof Node)
        {
            final SortedSet<Long> mergedInEdgeIdentifiers = FeatureChangeMergers
                    .mergeMember_OldStrategy("inEdgeIdentifiers", thisReference, thatReference,
                            atlasEntity -> ((Node) atlasEntity).inEdges() == null ? null
                                    : ((Node) atlasEntity).inEdges().stream()
                                            .map(Edge::getIdentifier)
                                            .collect(Collectors.toCollection(TreeSet::new)),
                            FeatureChangeMergers.directReferenceMergerSorted);
            final SortedSet<Long> mergedOutEdgeIdentifiers = FeatureChangeMergers
                    .mergeMember_OldStrategy("outEdgeIdentifiers", thisReference,
                            thatReference,
                            atlasEntity -> ((Node) atlasEntity).outEdges() == null ? null
                                    : ((Node) atlasEntity).outEdges().stream()
                                            .map(Edge::getIdentifier)
                                            .collect(Collectors.toCollection(TreeSet::new)),
                            FeatureChangeMergers.directReferenceMergerSorted);
            final CompleteNode result = new CompleteNode(getIdentifier(), mergedLocation,
                    mergedTags, mergedInEdgeIdentifiers, mergedOutEdgeIdentifiers,
                    mergedParentRelations);
            if (result.bounds() == null)
            {
                throw new CoreException("TODO CompleteNode result bounds null, investigate");
            }
            return FeatureChange.add(result);
        }
        else
        {
            // Point
            final CompletePoint result = new CompletePoint(getIdentifier(), mergedLocation,
                    mergedTags, mergedParentRelations);
            if (result.bounds() == null)
            {
                throw new CoreException("TODO CompletePoint result bounds null, investigate");
            }
            return FeatureChange.add(result);
        }
    }

    private FeatureChange mergeLocationItems_NewlyProposed(final FeatureChange other, // NOSONAR
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity beforeEntityLeft = this.getBeforeView();
        final AtlasEntity afterEntityLeft = this.getUpdatedView();
        final AtlasEntity beforeEntityRight = other.getBeforeView();
        final AtlasEntity afterEntityRight = other.getUpdatedView();

        final MergedMemberBean<Location> mergedLocationBean = FeatureChangeMergers.mergeMember(
                "location", beforeEntityLeft, afterEntityLeft, beforeEntityRight, afterEntityRight,
                atlasEntity -> ((LocationItem) atlasEntity).getLocation(), null, null);

        if (afterEntityLeft instanceof Node)
        {
            final MergedMemberBean<SortedSet<Long>> mergedInEdgeIdentifiersBean = FeatureChangeMergers
                    .mergeMember("inEdgeIdentifiers", beforeEntityLeft, afterEntityLeft,
                            beforeEntityRight, afterEntityRight,
                            atlasEntity -> ((Node) atlasEntity).inEdges() == null ? null
                                    : ((Node) atlasEntity).inEdges().stream()
                                            .map(Edge::getIdentifier)
                                            .collect(Collectors.toCollection(TreeSet::new)),
                            FeatureChangeMergers.directReferenceMergerSorted,
                            FeatureChangeMergers.diffBasedIntegerSortedSetMerger);
            final MergedMemberBean<SortedSet<Long>> mergedOutEdgeIdentifiersBean = FeatureChangeMergers
                    .mergeMember("outEdgeIdentifiers", beforeEntityLeft, afterEntityLeft,
                            beforeEntityRight, afterEntityRight,
                            atlasEntity -> ((Node) atlasEntity).outEdges() == null ? null
                                    : ((Node) atlasEntity).outEdges().stream()
                                            .map(Edge::getIdentifier)
                                            .collect(Collectors.toCollection(TreeSet::new)),
                            FeatureChangeMergers.directReferenceMergerSorted,
                            FeatureChangeMergers.diffBasedIntegerSortedSetMerger);
            final CompleteNode result = new CompleteNode(getIdentifier(),
                    mergedLocationBean.getMergedAfterMember(), mergedTags,
                    mergedInEdgeIdentifiersBean.getMergedAfterMember(),
                    mergedOutEdgeIdentifiersBean.getMergedAfterMember(), mergedParentRelations);
            if (result.bounds() == null)
            {
                throw new CoreException("TODO CompleteNode result bounds null, investigate");
            }
            final CompleteNode beforeNode = CompleteNode.shallowFrom((Node) afterEntityLeft)
                    .withLocation(mergedLocationBean.getMergedBeforeMember())
                    .withInEdgeIdentifiers(mergedInEdgeIdentifiersBean.getMergedBeforeMember())
                    .withOutEdgeIdentifiers(mergedOutEdgeIdentifiersBean.getMergedBeforeMember());
            return new FeatureChange(ChangeType.ADD, result, beforeNode);
        }
        else
        {
            // Point
            final CompletePoint result = new CompletePoint(getIdentifier(),
                    mergedLocationBean.getMergedAfterMember(), mergedTags, mergedParentRelations);
            if (result.bounds() == null)
            {
                throw new CoreException("TODO CompletePoint result bounds null, investigate");
            }
            final CompletePoint beforePoint = CompletePoint.shallowFrom((Point) afterEntityLeft)
                    .withLocation(mergedLocationBean.getMergedBeforeMember());
            return new FeatureChange(ChangeType.ADD, result, beforePoint);
        }
    }

    private FeatureChange mergeRelations(final FeatureChange other,
            final Map<String, String> mergedTags, final Set<Long> mergedParentRelations)
    {
        final AtlasEntity thisReference = this.getUpdatedView();
        final AtlasEntity thatReference = other.getUpdatedView();

        final RelationBean mergedMembers = FeatureChangeMergers.mergeMember_OldStrategy(
                "relationMembers", thisReference, thatReference,
                entity -> ((Relation) entity).members() == null ? null
                        : ((Relation) entity).members().asBean(),
                FeatureChangeMergers.relationBeanMerger);
        final Rectangle mergedBounds = Rectangle.forLocated(thisReference, thatReference);
        final Long mergedOsmRelationIdentifier = FeatureChangeMergers.mergeMember_OldStrategy(
                "osmRelationIdentifier", thisReference, thatReference,
                entity -> ((Relation) entity).getOsmIdentifier(), null);
        final Set<Long> mergedAllRelationsWithSameOsmIdentifierSet = FeatureChangeMergers
                .mergeMember_OldStrategy("allRelationsWithSameOsmIdentifier", thisReference,
                        thatReference,
                        atlasEntity -> ((Relation) atlasEntity)
                                .allRelationsWithSameOsmIdentifier() == null
                                        ? null
                                        : ((Relation) atlasEntity)
                                                .allRelationsWithSameOsmIdentifier().stream()
                                                .map(Relation::getIdentifier)
                                                .collect(Collectors.toSet()),
                        FeatureChangeMergers.directReferenceMerger);
        final List<Long> mergedAllRelationsWithSameOsmIdentifier = mergedAllRelationsWithSameOsmIdentifierSet == null
                ? null
                : mergedAllRelationsWithSameOsmIdentifierSet.stream().collect(Collectors.toList());
        final RelationBean mergedAllKnownMembers = FeatureChangeMergers
                .mergeMember_OldStrategy("allKnownOsmMembers", thisReference, thatReference,
                        entity -> ((Relation) entity).allKnownOsmMembers() == null ? null
                                : ((Relation) entity).allKnownOsmMembers().asBean(),
                        FeatureChangeMergers.relationBeanMerger);

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
