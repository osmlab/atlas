package org.openstreetmap.atlas.geography.atlas.change.feature;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
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
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.streaming.resource.WritableResource;

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
    private AtlasEntity beforeView;
    private final AtlasEntity afterView;

    public static FeatureChange add(final AtlasEntity afterView)
    {
        return new FeatureChange(ChangeType.ADD, afterView);
    }

    public static FeatureChange add(final AtlasEntity afterView, final Atlas atlasContext)
    {
        return new FeatureChange(ChangeType.ADD, afterView).withAtlasContext(atlasContext);
    }

    public static FeatureChange remove(final AtlasEntity reference)
    {
        return new FeatureChange(ChangeType.REMOVE, reference);
    }

    /**
     * Create a new {@link FeatureChange} with a given type, after view, and before view. This
     * constructor is provided for exact control over the before view of a change. However, most
     * users who wish to specify a before view should generally prefer to use the following pattern
     * instead:<br>
     * <br>
     * <code>
     * new FeatureChange(ChangeType.ADD, updatedCompleteEntity).withAtlasContext(atlas);
     * </code> <br>
     * <br>
     * This is much less error prone, as {@link FeatureChange} will automatically calculate the
     * properly populated beforeView it needs to perform proper merging. See
     * {@link FeatureChange#withAtlasContext(Atlas)} for more information.
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
            throw new CoreException("reference cannot be null.");
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
        this.validateUsefulFeatureChange();
    }

    /**
     * Create a new {@link FeatureChange} with a given type and after view.
     *
     * @param changeType
     *            the type, either ADD or REMOVE.
     * @param afterView
     *            the after view of the changed entity
     */
    public FeatureChange(final ChangeType changeType, final AtlasEntity afterView)
    {
        this(changeType, afterView, null);
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
        return ItemType.forEntity(getAfterView());
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
        try
        {
            if (this.getIdentifier() != other.getIdentifier()
                    || this.getItemType() != other.getItemType()
                    || this.getChangeType() != other.getChangeType())
            {
                throw new CoreException(
                        "Cannot merge FeatureChanges with mismatching properties: [{}, {}, {}] vs [{}, {}, {}]",
                        this.getIdentifier(), this.getItemType(), this.getChangeType(),
                        other.getIdentifier(), other.getItemType(), other.getChangeType());
            }
            /*
             * If one of the FeatureChanges has a beforeView, the other must as well. Otherwise we
             * cannot merge. It is the responsibility of the caller to ensure that either both
             * FeatureChanges have a beforeView, or both do not.
             */
            if (this.getBeforeView() == null && other.getBeforeView() != null
                    || this.getBeforeView() != null && other.getBeforeView() == null)
            {
                throw new CoreException("One of the FeatureChanges was missing a beforeView - "
                        + "cannot merge two FeatureChanges unless both either explicitly provide or explicitly exclude a beforeView");
            }
            if (this.getChangeType() == ChangeType.REMOVE)
            {
                return this;
            }
            if (this.getChangeType() == ChangeType.ADD)
            {
                return FeatureChangeMergingHelpers.mergeADDFeatureChangePair(this, other);
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
                + this.afterView.getType() + "," + this.afterView.getIdentifier() + "}, tags="
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
    private void computeBeforeViewUsingAtlasContext(final Atlas atlas)
    {
        if (atlas == null)
        {
            throw new CoreException("Atlas context cannot be null");
        }

        final AtlasEntity beforeViewUpdatesOnly;
        final AtlasEntity beforeViewFromAtlas = atlas.entity(this.afterView.getIdentifier(),
                this.afterView.getType());
        if (beforeViewFromAtlas == null)
        {
            throw new CoreException("Could not find {} with ID {} in atlas context",
                    this.afterView.getType(), this.afterView.getIdentifier());
        }

        /*
         * Make type specific updates first.
         */
        switch (this.afterView.getType())
        {
            /*
             * Area specific updates. The only Area-specific field is the polygon.
             */
            case AREA:
                final Area updatedAreaView = (Area) this.afterView;
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
                final Edge updatedEdgeView = (Edge) this.afterView;
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
                final Line updatedLineView = (Line) this.afterView;
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
                final Node updatedNodeView = (Node) this.afterView;
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
                final Point updatedPointView = (Point) this.afterView;
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
                final Relation updatedRelationView = (Relation) this.afterView;
                final Relation beforeRelationView = (Relation) beforeViewFromAtlas;
                beforeViewUpdatesOnly = CompleteRelation.shallowFrom(updatedRelationView);
                if (updatedRelationView.members() != null)
                {
                    ((CompleteRelation) beforeViewUpdatesOnly)
                            .withMembers(beforeRelationView.members());
                }
                break;
            default:
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

    private void validateUsefulFeatureChange()
    {
        if (this.changeType == ChangeType.ADD && this.afterView instanceof CompleteEntity
                && ((CompleteEntity) this.afterView).isSuperShallow())
        {
            throw new CoreException("{} does not contain anything useful.", this);
        }
    }
}
