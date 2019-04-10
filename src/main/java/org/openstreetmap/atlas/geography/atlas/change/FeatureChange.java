package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.serializer.FeatureChangeGeoJsonSerializer;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLineItem;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLocationItem;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
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
import org.openstreetmap.atlas.geography.atlas.validators.FeatureChangeUsefulnessValidator;
import org.openstreetmap.atlas.streaming.resource.WritableResource;

/**
 * Single feature change, does not include any consistency checks.
 * <p>
 * To add a new, non existing feature: {@link ChangeType} is ADD, and the included reference needs
 * to contain all the information related to that new feature.
 * <p>
 * To modify an existing feature: {@link ChangeType} is ADD, and the included reference needs to
 * contain the only the changed information related to that changed feature.
 * <p>
 * To remove an existing feature: {@link ChangeType} is REMOVE. The reference entity need only
 * contain the identifier of the feature to remove.
 * <p>
 * For all {@link FeatureChange}s, you may indirectly include a reference to the before view of the
 * entity using the {@link FeatureChange#add(AtlasEntity, Atlas)} and
 * {@link FeatureChange#remove(AtlasEntity, Atlas)} methods. Providing the atlas context allows
 * {@link FeatureChange} to perform more sophisticated merge logic.
 *
 * @author matthieun
 * @author lcram
 * @author Yazad Khambata
 */
public class FeatureChange implements Located, Serializable
{
    private static final long serialVersionUID = 9172045162819925515L;

    private final String featureChangeId = UUID.randomUUID().toString();

    private final ChangeType changeType;
    private AtlasEntity beforeView;
    private final AtlasEntity afterView;

    /**
     * Create a new {@link ChangeType#ADD} {@link FeatureChange} with a given afterView. The
     * afterView should be a {@link CompleteEntity} that specifies how the newly added or modified
     * feature should look. For the modified case, the afterView {@link CompleteEntity} need only
     * contain the fields that were modified. For ADDs that are adding a brand new feature, it
     * should be fully populated.
     *
     * @param afterView
     *            the after view {@link CompleteEntity}
     * @return the created {@link FeatureChange}
     */
    public static FeatureChange add(final AtlasEntity afterView)
    {
        return new FeatureChange(ChangeType.ADD, afterView);
    }

    /**
     * Create a new {@link ChangeType#ADD} {@link FeatureChange} with a given after view. The
     * afterView should be a {@link CompleteEntity} that specifies how the newly added or modified
     * feature should look. For the modified case, the afterView {@link CompleteEntity} need only
     * contain the fields that were modified. For ADDs that are adding a brand new feature, it
     * should be fully populated. The atlasContext parameter creates a richer {@link FeatureChange}
     * that contains information on how the entity looked before the update. This allows for more
     * sophisticated merge logic.
     *
     * @param afterView
     *            the after view {@link CompleteEntity}
     * @param atlasContext
     *            the atlas context
     * @return the created {@link FeatureChange}
     */
    public static FeatureChange add(final AtlasEntity afterView, final Atlas atlasContext)
    {
        return new FeatureChange(ChangeType.ADD, afterView).withAtlasContext(atlasContext);
    }

    /**
     * Create a new {@link ChangeType#REMOVE} {@link FeatureChange} using a given reference. The
     * reference can be a shallow {@link CompleteEntity}, i.e. containing only the identifier of the
     * feature to be removed.
     *
     * @param reference
     *            the {@link CompleteEntity} to remove
     * @return the created {@link FeatureChange}
     */
    public static FeatureChange remove(final AtlasEntity reference)
    {
        return new FeatureChange(ChangeType.REMOVE, reference);
    }

    /**
     * Create a new {@link ChangeType#REMOVE} {@link FeatureChange} using a given reference. The
     * reference can be a shallow {@link CompleteEntity}, i.e. containing only the identifier of the
     * feature to be removed. The atlasContext parameter creates a richer {@link FeatureChange} that
     * contains information on how the entity looked before the update. This allows for more
     * sophisticated merge logic.
     *
     * @param reference
     *            the {@link CompleteEntity} to remove
     * @param atlasContext
     *            the atlas context
     * @return the created {@link FeatureChange}
     */
    public static FeatureChange remove(final AtlasEntity reference, final Atlas atlasContext)
    {
        return new FeatureChange(ChangeType.REMOVE, reference).withAtlasContext(atlasContext);
    }

    /**
     * Create a new {@link FeatureChange} with a given type and after view.
     *
     * @param changeType
     *            the type, either ADD or REMOVE.
     * @param afterView
     *            the after view of the changed entity
     */
    FeatureChange(final ChangeType changeType, final AtlasEntity afterView)
    {
        this(changeType, afterView, null);
    }

    /**
     * Create a new {@link FeatureChange} with a given type, after view, and before view. This
     * constructor is provided for exact control over the before view of a change. It is kept
     * package private, and is used for testing purposes only.
     *
     * @param changeType
     *            the change type
     * @param afterView
     *            the updated entity
     * @param beforeView
     *            the before entity
     */
    FeatureChange(final ChangeType changeType, final AtlasEntity afterView,
            final AtlasEntity beforeView)
    {
        if (afterView == null)
        {
            throw new CoreException("After view cannot be null.");
        }
        if (!(afterView instanceof CompleteEntity))
        {
            throw new CoreException(
                    "FeatureChange afterView requires CompleteEntity, found reference of type {}",
                    afterView.getClass().getName());
        }
        if (beforeView != null && !(beforeView instanceof CompleteEntity))
        {
            throw new CoreException(
                    "FeatureChange beforeView requires CompleteEntity, found reference of type {}",
                    beforeView.getClass().getName());
        }
        if (changeType == null)
        {
            throw new CoreException("changeType cannot be null.");
        }

        this.changeType = changeType;
        this.afterView = afterView;
        this.beforeView = beforeView;

        if (this.afterView.bounds() == null)
        {
            throw new CoreException("afterView {} bounds was null for {}", this.afterView,
                    this.toString());
        }
        if (this.beforeView != null && this.beforeView.bounds() == null)
        {
            throw new CoreException("beforeView {} bounds was null for {}", this.beforeView,
                    this.toString());
        }

        this.validateNotShallow();
        if (this.beforeView != null)
        {
            new FeatureChangeUsefulnessValidator(this).validate();
        }
    }

    public String getFeatureChangeId()
    {
        return featureChangeId;
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
    FeatureChange withAtlasContext(final Atlas atlas)
    {
        computeBeforeViewUsingAtlasContext(atlas, this.changeType);
        new FeatureChangeUsefulnessValidator(this).validate();
        return this;
    }

    /**
     * Check if this {@link FeatureChange}'s afterView is full. A full afterView is a
     * {@link CompleteEntity} that has all its fields set to non-null values.
     *
     * @return if this {@link FeatureChange} has a full afterView
     */
    public boolean afterViewIsFull()
    {
        if (this.getAfterView().getTags() == null || this.getAfterView().relations() == null)
        {
            return false;
        }
        switch (this.getItemType())
        {
            case NODE:
                final Node nodeReference = (Node) this.getAfterView();
                if (nodeReference.inEdges() == null || nodeReference.outEdges() == null
                        || nodeReference.getLocation() == null)
                {
                    return false;
                }
                break;
            case EDGE:
                final Edge edgeReference = (Edge) this.getAfterView();
                if (edgeReference.start() == null || edgeReference.end() == null
                        || edgeReference.asPolyLine() == null)
                {
                    return false;
                }
                break;
            case AREA:
                final Area areaReference = (Area) this.getAfterView();
                if (areaReference.asPolygon() == null)
                {
                    return false;
                }
                break;
            case LINE:
                final Line lineReference = (Line) this.getAfterView();
                if (lineReference.asPolyLine() == null)
                {
                    return false;
                }
                break;
            case POINT:
                final Point pointReference = (Point) this.getAfterView();
                if (pointReference.getLocation() == null)
                {
                    return false;
                }
                break;
            case RELATION:
                final Relation relationReference = (Relation) this.getAfterView();
                if (relationReference.members() == null
                        || relationReference.allKnownOsmMembers() == null
                        || relationReference.allRelationsWithSameOsmIdentifier() == null)
                {
                    return false;
                }
                break;
            default:
                throw new CoreException("Unknown Item Type {}", this.getItemType());
        }
        return true;
    }

    @Override
    public Rectangle bounds()
    {
        final Rectangle updatedBounds = this.afterView.bounds();
        if (this.beforeView == null)
        {
            return updatedBounds;
        }
        return Rectangle.forLocated(this.beforeView.bounds(), updatedBounds);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof FeatureChange)
        {
            final FeatureChange that = (FeatureChange) other;
            return this.getChangeType() == that.getChangeType()
                    && this.getAfterView().equals(that.getAfterView());
        }
        return false;
    }

    public AtlasEntity getAfterView()
    {
        return this.afterView;
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
        return getAfterView().getIdentifier();
    }

    public ItemType getItemType()
    {
        return getAfterView().getType();
    }

    /**
     * Get a tag based on a key, taking the changes into account.
     *
     * @param key
     *            - The tag key to look for.
     * @return - the changed value of the tag, if available.
     */
    public Optional<String> getTag(final String key)
    {
        return this.getAfterView().getTag(key);
    }

    /**
     * Get the changed tags.
     *
     * @return Map - the changed tags.
     */
    public Map<String, String> getTags()
    {
        return this.getAfterView().getTags();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.changeType, this.afterView);
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
        /*
         * FeatureChanges are mergeable under certain pre-conditions. If those pre-conditions are
         * satisfied, then we can proceed with attempting to merge the FeatureChanges.
         */
        // Pre-conditions:
        // 1) The left and right FeatureChanges must be operating on the same entity identifier and
        // ItemType. Additionally, the ChangeType (i.e. ADD or REMOVE) must match. If these
        // conditions do not hold, there is no logical way to merge the FeatureChanges.
        //
        // 2) Either both FeatureChanges must provide a beforeView, or neither should provide one.
        // Attempting to merge two FeatureChanges where one has a beforeView and one does not
        // will always fail. We enforce this assumption in order to make the ADD/REMOVE merge logic
        // simpler.
        /*
         * Once basic mergeability is established, the merge logic proceeds.
         */
        // Merging two REMOVE changes:
        // This case is easy. Since a REMOVE contains no additional information, we can simply
        // arbitrarily return the left side FeatureChange. The beforeViews are guaranteed to be
        // properly merged because either:
        //
        // 1) Both FeatureChanges had fully populated, equivalent beforeViews (which are computed
        // automatically when a REMOVE FeatureChange is created)
        //
        // OR
        //
        // 2) Neither FeatureChange had a beforeView, in which case no merge is required.
        //
        // Merging two ADD changes:
        // In this case, we need to perform additional checks to ensure that the FeatureChanges can
        // indeed properly merge. We also must ensure that the potentially differing beforeViews can
        // merge. For more information on this, see
        // FeatureChangeMergingHelpers#mergeADDFeatureChangePair.
        try
        {
            // Pre-condition 1)
            if (this.getIdentifier() != other.getIdentifier()
                    || this.getItemType() != other.getItemType()
                    || this.getChangeType() != other.getChangeType())
            {
                throw new CoreException(
                        "Cannot merge FeatureChanges with mismatching properties: [{}, {}, {}] vs [{}, {}, {}]",
                        this.getIdentifier(), this.getItemType(), this.getChangeType(),
                        other.getIdentifier(), other.getItemType(), other.getChangeType());
            }

            // Pre-condition 2)
            if (this.getBeforeView() == null && other.getBeforeView() != null
                    || this.getBeforeView() != null && other.getBeforeView() == null)
            {
                throw new CoreException("One of the FeatureChanges was missing a beforeView - "
                        + "cannot merge two FeatureChanges unless both either explicitly provide or explicitly exclude a beforeView, {} and {}",
                        this.toString(), other.toString());
            }

            // Actually merge the changes
            if (this.getChangeType() == ChangeType.REMOVE)
            {
                return this;
            }
            else if (this.getChangeType() == ChangeType.ADD)
            {
                return FeatureChangeMergingHelpers.mergeADDFeatureChangePair(this, other);
            }

            // If we get here, something very unexpected happened.
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
                + this.afterView.getType() + "," + this.afterView.getIdentifier() + "}, tags="
                + getTags() + ", bounds=" + bounds() + "]";
    }

    /**
     * Compute the beforeView using a given afterView and Atlas context. The beforeView is always a
     * CompleteEntity. For ChangeType.ADD, the beforeView will only contain references to fields
     * that were updated in the afterView. For ChangeType.REMOVE, the beforeView will be fully
     * populated. This will facilitate better debug printouts.
     *
     * @param atlas
     *            the atlas context
     * @param changeType
     *            the change type
     */
    private void computeBeforeViewUsingAtlasContext(final Atlas atlas, final ChangeType changeType)
    {
        if (atlas == null)
        {
            throw new CoreException("Atlas context cannot be null for {}", this.toString());
        }

        final AtlasEntity beforeViewUpdatesOnly;
        final AtlasEntity beforeViewFromAtlas = atlas.entity(this.afterView.getIdentifier(),
                this.afterView.getType());

        /*
         * Check that the beforeViewFromAtlas is non-null. In case of REMOVE, this must be the case.
         * In case of ADD, it is possible the beforeViewFromAtlas is null when adding a brand new
         * feature.
         */
        if (beforeViewFromAtlas == null && changeType != ChangeType.ADD)
        {
            throw new CoreException(
                    "Could not find {} with ID {} in atlas context, ChangeType was {}",
                    this.afterView.getType(), this.afterView.getIdentifier(), changeType);
        }
        /*
         * For the REMOVE case, we fully populate the beforeView and return.
         */
        if (changeType == ChangeType.REMOVE)
        {
            this.beforeView = CompleteEntity.from(beforeViewFromAtlas);
            return;
        }

        /*
         * Otherwise, we continue with the ADD case.
         */
        if (changeType != ChangeType.ADD)
        {
            throw new CoreException("Unknown ChangeType {}", changeType);
        }

        /*
         * If the beforeViewFromAtlas is null, then this is a brand new ADD. We just set the
         * beforeView to null and return.
         */
        if (beforeViewFromAtlas == null)
        {
            this.beforeView = null;
            return;
        }

        /*
         * Make type specific updates first.
         */
        if (this.afterView instanceof Area)
        {
            /*
             * Area specific updates. The only Area-specific field is the polygon.
             */
            final Area afterAreaView = (Area) this.afterView;
            final Area beforeAreaViewFromAtlas = (Area) beforeViewFromAtlas;
            beforeViewUpdatesOnly = CompleteArea.shallowFrom(beforeAreaViewFromAtlas);
            if (afterAreaView.asPolygon() != null)
            {
                ((CompleteArea) beforeViewUpdatesOnly)
                        .withPolygon(beforeAreaViewFromAtlas.asPolygon());
            }
        }
        else if (this.afterView instanceof LineItem)
        {
            /*
             * LineItem specific updates. The LineItem-specific fields are the polyline, and the
             * start/end nodes in case of an Edge LineItem.
             */
            final LineItem afterLineItemView = (LineItem) this.afterView;
            final LineItem beforeLineItemViewFromAtlas = (LineItem) beforeViewFromAtlas;
            beforeViewUpdatesOnly = CompleteEntity.shallowFrom(beforeLineItemViewFromAtlas);
            if (afterLineItemView.asPolyLine() != null)
            {
                ((CompleteLineItem) beforeViewUpdatesOnly)
                        .withPolyLine(beforeLineItemViewFromAtlas.asPolyLine());
            }
            if (this.afterView instanceof Edge)
            {
                final Edge afterEdgeView = (Edge) afterLineItemView;
                final Edge beforeEdgeViewFromAtlas = (Edge) beforeViewFromAtlas;
                if (afterEdgeView.start() != null)
                {
                    ((CompleteEdge) beforeViewUpdatesOnly).withStartNodeIdentifier(
                            beforeEdgeViewFromAtlas.start().getIdentifier());
                }
                if (afterEdgeView.end() != null)
                {
                    ((CompleteEdge) beforeViewUpdatesOnly)
                            .withEndNodeIdentifier(beforeEdgeViewFromAtlas.end().getIdentifier());
                }
            }
        }
        else if (this.afterView instanceof LocationItem)
        {
            /*
             * LocationItem specific updates. The LocationItem-specific fields are the location, and
             * the in/out edge sets in case of a Node LocationItem.
             */
            final LocationItem afterLocationItemView = (LocationItem) this.afterView;
            final LocationItem beforeLocationItemViewFromAtlas = (LocationItem) beforeViewFromAtlas;
            beforeViewUpdatesOnly = CompleteEntity.shallowFrom(beforeLocationItemViewFromAtlas);
            if (afterLocationItemView.getLocation() != null)
            {
                ((CompleteLocationItem) beforeViewUpdatesOnly)
                        .withLocation(beforeLocationItemViewFromAtlas.getLocation());
            }
            if (this.afterView instanceof Node)
            {
                final Node afterNodeView = (Node) afterLocationItemView;
                final Node beforeNodeViewFromAtlas = (Node) beforeViewFromAtlas;
                if (afterNodeView.inEdges() != null)
                {
                    ((CompleteNode) beforeViewUpdatesOnly)
                            .withInEdges(beforeNodeViewFromAtlas.inEdges());
                }
                if (afterNodeView.outEdges() != null)
                {
                    ((CompleteNode) beforeViewUpdatesOnly)
                            .withOutEdges(beforeNodeViewFromAtlas.outEdges());
                }
            }
        }
        else if (this.afterView instanceof Relation)
        {
            /*
             * Relation specific updates. There are quite a few Relation specific fields: members,
             * allRelationsWithSameOsmIdentifier, allKnownOsmMembers, and osmRelationIdentifier.
             */
            final Relation afterRelationView = (Relation) this.afterView;
            final Relation beforeRelationViewFromAtlas = (Relation) beforeViewFromAtlas;
            beforeViewUpdatesOnly = CompleteRelation.shallowFrom(afterRelationView);
            if (afterRelationView.members() != null)
            {
                ((CompleteRelation) beforeViewUpdatesOnly)
                        .withMembers(beforeRelationViewFromAtlas.members());
            }
            if (afterRelationView.allRelationsWithSameOsmIdentifier() != null)
            {
                ((CompleteRelation) beforeViewUpdatesOnly).withAllRelationsWithSameOsmIdentifier(
                        beforeRelationViewFromAtlas.allRelationsWithSameOsmIdentifier().stream()
                                .map(Relation::getIdentifier).collect(Collectors.toList()));
            }
            if (afterRelationView.allKnownOsmMembers() != null)
            {
                ((CompleteRelation) beforeViewUpdatesOnly).withAllKnownOsmMembers(
                        beforeRelationViewFromAtlas.allKnownOsmMembers().asBean());
            }
            if (afterRelationView.osmRelationIdentifier() != null)
            {
                ((CompleteRelation) beforeViewUpdatesOnly).withOsmRelationIdentifier(
                        beforeRelationViewFromAtlas.osmRelationIdentifier());
            }
        }
        else
        {
            throw new CoreException("Unknown entity type {}", this.afterView.getType());
        }

        /*
         * Add before view of the tags if the updatedView updated the tags.
         */
        final Map<String, String> updatedViewTags = this.afterView.getTags();
        if (updatedViewTags != null)
        {
            ((CompleteEntity) beforeViewUpdatesOnly).withTags(beforeViewFromAtlas.getTags());
        }

        /*
         * Add before view of relations if updatedView updated relations.
         */
        final Set<Relation> updatedViewRelations = this.afterView.relations();
        if (updatedViewRelations != null)
        {
            ((CompleteEntity) beforeViewUpdatesOnly).withRelations(beforeViewFromAtlas.relations());
        }

        this.beforeView = beforeViewUpdatesOnly;
    }

    /**
     * Check that this {@link FeatureChange} is not shallow. A shallow {@link FeatureChange} is one
     * whose CompleteEntity only contains an identifier.
     */
    private void validateNotShallow()
    {
        if (this.changeType == ChangeType.ADD && ((CompleteEntity) this.afterView).isShallow())
        {
            throw new CoreException("{} was shallow (i.e. it contained only an identifier)", this);
        }
    }
}
