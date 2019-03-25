package org.openstreetmap.atlas.geography.atlas.change;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
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

    public static FeatureChange remove(final AtlasEntity reference, final Atlas atlasContext)
    {
        return new FeatureChange(ChangeType.REMOVE, reference).withAtlasContext(atlasContext);
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
        /*
         * FeatureChanges are mergeable under certain pre-conditions. If those pre-conditions are
         * satisfied, then two FeatureChange objects can be composed into a single object.
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
         * Once mergeability is established, the merge logic proceeds.
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
                        + "cannot merge two FeatureChanges unless both either explicitly provide or explicitly exclude a beforeView");
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
        computeBeforeViewUsingAtlasContext(atlas, this.changeType);
        return this;
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
         * For the REMOVE case, we fully populate the beforeView and return.
         */
        if (changeType == ChangeType.REMOVE)
        {
            this.beforeView = CompleteEntity.from(beforeViewFromAtlas);
            return;
        }
        if (changeType != ChangeType.ADD)
        {
            throw new CoreException("Unknown ChangeType {}", changeType);
        }

        /*
         * Otherwise, we continue with the ADD case.
         */

        /*
         * Make type specific updates first.
         */
        switch (this.afterView.getType())
        {
            /*
             * Area specific updates. The only Area-specific field is the polygon.
             */
            case AREA:
                final Area afterAreaView = (Area) this.afterView;
                final Area beforeAreaViewFromAtlas = (Area) beforeViewFromAtlas;
                beforeViewUpdatesOnly = CompleteArea.shallowFrom(beforeAreaViewFromAtlas);
                if (afterAreaView.asPolygon() != null)
                {
                    ((CompleteArea) beforeViewUpdatesOnly)
                            .withPolygon(beforeAreaViewFromAtlas.asPolygon());
                }
                break;
            /*
             * Edge specific updates. The Edge-specific fields are the polyline and the start/end
             * nodes.
             */
            case EDGE:
                final Edge afterEdgeView = (Edge) this.afterView;
                final Edge beforeEdgeViewFromAtlas = (Edge) beforeViewFromAtlas;
                beforeViewUpdatesOnly = CompleteEdge.shallowFrom(afterEdgeView);
                if (afterEdgeView.asPolyLine() != null)
                {
                    ((CompleteEdge) beforeViewUpdatesOnly)
                            .withPolyLine(beforeEdgeViewFromAtlas.asPolyLine());
                }
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
                break;
            /*
             * Line specific updates. The only Line-specific field is the polyline.
             */
            case LINE:
                final Line afterLineView = (Line) this.afterView;
                final Line beforeLineViewFromAtlas = (Line) beforeViewFromAtlas;
                beforeViewUpdatesOnly = CompleteLine.shallowFrom(afterLineView);
                if (afterLineView.asPolyLine() != null)
                {
                    ((CompleteLine) beforeViewUpdatesOnly)
                            .withPolyLine(beforeLineViewFromAtlas.asPolyLine());
                }
                break;
            /*
             * Node specific updates. The Node-specific fields are the location and the in/out edge
             * sets.
             */
            case NODE:
                final Node afterNodeView = (Node) this.afterView;
                final Node beforeNodeViewFromAtlas = (Node) beforeViewFromAtlas;
                beforeViewUpdatesOnly = CompleteNode.shallowFrom(afterNodeView);
                if (afterNodeView.getLocation() != null)
                {
                    ((CompleteNode) beforeViewUpdatesOnly)
                            .withLocation(beforeNodeViewFromAtlas.getLocation());
                }
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
                break;
            /*
             * Point specific updates. The only Point-specific field is the location.
             */
            case POINT:
                final Point afterPointView = (Point) this.afterView;
                final Point beforePointViewFromAtlas = (Point) beforeViewFromAtlas;
                beforeViewUpdatesOnly = CompletePoint.shallowFrom(afterPointView);
                if (afterPointView.getLocation() != null)
                {
                    ((CompletePoint) beforeViewUpdatesOnly)
                            .withLocation(beforePointViewFromAtlas.getLocation());
                }
                break;
            /*
             * Relation specific updates. There are quite a few Relation specific fields: members,
             * allRelationsWithSameOsmIdentifier, allKnownOsmMembers, and osmRelationIdentifier.
             */
            case RELATION:
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
                    ((CompleteRelation) beforeViewUpdatesOnly)
                            .withAllRelationsWithSameOsmIdentifier(beforeRelationViewFromAtlas
                                    .allRelationsWithSameOsmIdentifier().stream()
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
        final String errorMessage = "{} does not contain anything useful.";
        if (this.changeType == ChangeType.ADD && this.beforeView != null
                && ((CompleteEntity) this.afterView).isSuperShallow())
        {
            switch (this.beforeView.getType())
            {
                case AREA:
                    final Area beforeArea = (Area) this.beforeView;
                    final Area afterArea = (Area) this.afterView;
                    if (beforeArea.asPolygon().equals(afterArea.asPolygon()))
                    {
                        throw new CoreException(errorMessage, this);
                    }
                    break;
                case EDGE:
                    final Edge beforeEdge = (Edge) this.beforeView;
                    final Edge afterEdge = (Edge) this.afterView;
                    if (beforeEdge.asPolyLine().equals(afterEdge.asPolyLine()))
                    {
                        throw new CoreException(errorMessage, this);
                    }
                    break;
                case LINE:
                    final Line beforeLine = (Line) this.beforeView;
                    final Line afterLine = (Line) this.afterView;
                    if (beforeLine.asPolyLine().equals(afterLine.asPolyLine()))
                    {
                        throw new CoreException(errorMessage, this);
                    }
                    break;
                case NODE:
                    final Node beforeNode = (Node) this.beforeView;
                    final Node afterNode = (Node) this.afterView;
                    if (beforeNode.getLocation().equals(afterNode.getLocation()))
                    {
                        throw new CoreException(errorMessage, this);
                    }
                    break;
                case POINT:
                    final Point beforePoint = (Point) this.beforeView;
                    final Point afterPoint = (Point) this.afterView;
                    if (beforePoint.getLocation().equals(afterPoint.getLocation()))
                    {
                        throw new CoreException(errorMessage, this);
                    }
                    break;
                case RELATION:
                    throw new CoreException(errorMessage, this);
                default:
                    throw new CoreException("Unrecognized ItemType {}", this.beforeView.getType());
            }
        }
    }
}
