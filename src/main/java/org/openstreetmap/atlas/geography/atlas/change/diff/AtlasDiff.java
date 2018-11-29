package org.openstreetmap.atlas.geography.atlas.change.diff;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedArea;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedLine;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedPoint;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedRelation;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.matching.PolyLineRoute;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate {@link Change} objects based on the differences between two {@link Atlas}es. The main
 * usage of this class is the {@link AtlasDiff#generateChange()} method.
 *
 * @author lcram
 */
public class AtlasDiff
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDiff.class);

    private final Atlas before;
    private final Atlas after;
    private boolean geometryMatching = false;
    private boolean saveAllGeometries = false;
    private boolean useBloatedEntities = true;

    /*
     * TODO have a set of detected changes, construct FeatureChanges at the end. Try to avoid
     * repeated switch(itemType) constructs everywhere.
     */
    // Entities that were removed from the after atlas
    private Set<AtlasEntity> removedEntities;
    // Entities that were added to the after atlas
    private Set<AtlasEntity> addedEntities;
    // Entities that were modified in the after atlas
    private Set<AtlasEntity> changedEntities;

    /**
     * Construct an {@link AtlasDiff} with a given before {@link Atlas} and after {@link Atlas}.
     * When generating a {@link Change} based on this diff, the {@link Change} will change the
     * before atlas into the after atlas.
     *
     * @param before
     *            the initial {@link Atlas}
     * @param after
     *            the changed {@link Atlas}
     */
    public AtlasDiff(final Atlas before, final Atlas after)
    {
        this.before = before;
        this.after = after;

        if (this.before == null)
        {
            throw new CoreException("Before atlas cannot be null.");
        }

        if (this.after == null)
        {
            throw new CoreException("After atlas cannot be null.");
        }
    }

    /**
     * Generate a {@link Change} such that the {@link ChangeAtlas} produced by combining this
     * {@link Change} with the before {@link Atlas} would be effectively equivalent to the after
     * {@link Atlas}.
     *
     * @return the change
     */
    public Change generateChange()
    {
        this.addedEntities = ConcurrentHashMap.newKeySet();
        this.removedEntities = ConcurrentHashMap.newKeySet();
        this.changedEntities = ConcurrentHashMap.newKeySet();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        /*
         * Check for entities that were removed in the after atlas. If we find any, add them to a
         * removedEntities set for later processing. We will use this set to create FeatureChanges.
         */
        Iterables.parallelStream(this.before)
                .forEach(beforeEntity -> getRemovedEntityIfNecessary(beforeEntity, this.before,
                        this.after, this.geometryMatching).ifPresent(this.removedEntities::add));

        /*
         * Check for entities that were added in the after atlas. If we find any, add them to a
         * addedEntities set for later processing. We will use this set to create FeatureChanges.
         */
        Iterables.parallelStream(this.after)
                .forEach(afterEntity -> getAddedEntityIfNecessary(afterEntity, this.before,
                        this.after, this.geometryMatching).ifPresent(this.addedEntities::add));

        /*
         * Check for entities that were changed in the after atlas. If we find any, add them to a
         * changedEntities set for later processing. We will use this set to create FeatureChanges.
         */

        // Aggregate the results stored in addedEntities and removedEntities, creating the
        // ChangeObject if there are necessary changes
        createFeatureChangesBasedOnChangedEntities(this.addedEntities, this.removedEntities,
                this.before, this.after, this.useBloatedEntities, this.saveAllGeometries)
                        .parallelStream().forEach(changeBuilder::add);

        return changeBuilder.get();
    }

    public Atlas getAfterAtlas()
    {
        return this.after;
    }

    public Atlas getBeforeAtlas()
    {
        return this.before;
    }

    /**
     * Saving all geometries means that we bloat features with their geometry even if they do not
     * change the geometry. This is useful for visualization.<br>
     * <br>
     * NOTE TO REVIEWER TODO this may not be necessary, depending on how we end up implementing the
     * GEOJson for the {@link Change} class.
     *
     * @param saveAllGeometries
     *            save all geometries
     * @return a configured {@link AtlasDiff}
     */
    public AtlasDiff saveAllGeometries(final boolean saveAllGeometries)
    {
        this.saveAllGeometries = saveAllGeometries;
        return this;
    }

    /**
     * This can be disabled to skip saving bloated features and instead save features from the
     * before or after atlas. However, a Diff saved in this way will not be serializable. However,
     * it will be faster, and may be better for simple printing use-case.<br>
     * <br>
     * TODO We will need to decide on the default setting, true vs. false. Right now, it is set to
     * true.
     *
     * @param useBloatedEntities
     *            use bloated entities instead of the original entities
     * @return a configured {@link AtlasDiff}
     */
    public AtlasDiff useBloatedEntities(final boolean useBloatedEntities)
    {
        this.useBloatedEntities = useBloatedEntities;
        return this;
    }

    /**
     * Use geometry matching when computing diffs. This means that if we detect an added or removed
     * {@link Edge} based on ID, we will additionally check its geometry before actually generating
     * a diff. Sometimes, the same OSM way can be way-sectioned differently. So while the IDs for a
     * sectioned OSM way will be different, the underlying geometry will be the same.<br>
     * <br>
     * NOTE TO REVIEWER TODO this may not be necessary due to new consistent ID atlas?
     *
     * @param matching
     *            use geometry matching
     * @return a configured {@link AtlasDiff}
     */
    public AtlasDiff withGeometryMatching(final boolean matching)
    {
        this.geometryMatching = matching;
        return this;
    }

    private Set<FeatureChange> createFeatureChangesBasedOnChangedEntities(
            final Set<AtlasEntity> addedEntities, final Set<AtlasEntity> removedEntities,
            final Atlas beforeAtlas, final Atlas afterAtlas, final boolean useBloatedEntities,
            final boolean saveAllGeometries)
    {
        final Set<FeatureChange> featureChanges = ConcurrentHashMap.newKeySet();

        addedEntities
                .parallelStream().map(addedEntity -> createFeatureChangeWithType(addedEntity,
                        afterAtlas, useBloatedEntities, ChangeType.ADD))
                .forEach(featureChanges::add);

        removedEntities
                .parallelStream().map(removedEntity -> createFeatureChangeWithType(removedEntity,
                        beforeAtlas, useBloatedEntities, ChangeType.REMOVE))
                .forEach(featureChanges::add);

        return featureChanges;
    }

    private FeatureChange createFeatureChangeWithType(final AtlasEntity entity, final Atlas atlas,
            final boolean useBloatedEntities, final ChangeType changeType)
    {
        FeatureChange featureChange;
        final Long addedEntityIdentifier = entity.getIdentifier();
        if (useBloatedEntities)
        {
            switch (entity.getType())
            {
                case NODE:
                    featureChange = new FeatureChange(changeType,
                            BloatedNode.fromNode(atlas.node(addedEntityIdentifier)));
                    break;
                case EDGE:
                    featureChange = new FeatureChange(changeType,
                            BloatedEdge.fromEdge(atlas.edge(addedEntityIdentifier)));
                    break;
                case POINT:
                    featureChange = new FeatureChange(changeType,
                            BloatedPoint.fromPoint(atlas.point(addedEntityIdentifier)));
                    break;
                case LINE:
                    featureChange = new FeatureChange(changeType,
                            BloatedLine.fromLine(atlas.line(addedEntityIdentifier)));
                    break;
                case AREA:
                    featureChange = new FeatureChange(changeType,
                            BloatedArea.fromArea(atlas.area(addedEntityIdentifier)));
                    break;
                case RELATION:
                    featureChange = new FeatureChange(changeType,
                            BloatedRelation.fromRelation(atlas.relation(addedEntityIdentifier)));
                    break;
                default:
                    throw new CoreException("Unknown item type {}", entity.getType());
            }
        }
        else
        {
            featureChange = new FeatureChange(changeType, entity);
        }
        return featureChange;
    }

    private boolean edgeHasGeometryMatchAmong(final Edge edge, final Iterable<Edge> otherEdges)
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
        return false;
    }

    private boolean edgeHasGeometryMatchInAtlas(final Edge edge, final Atlas atlas)
    {
        final Iterable<Edge> intersectingEdgesWithSameOSMIdentifier = atlas.edgesIntersecting(
                edge.bounds(),
                otherEdge -> edge.getOsmIdentifier() == otherEdge.getOsmIdentifier());
        return edgeHasGeometryMatchAmong(edge, intersectingEdgesWithSameOSMIdentifier);
    }

    private Optional<AtlasEntity> getAddedEntityIfNecessary(final AtlasEntity afterEntity,
            final Atlas beforeAtlas, final Atlas afterAtlas, final boolean geometryMatching)
    {
        /*
         * Look up the afterEntity ID in the before atlas. If the entity is missing, we know it was
         * added to the after atlas.
         */
        if (afterEntity.getType().entityForIdentifier(beforeAtlas,
                afterEntity.getIdentifier()) == null)
        {
            /*
             * We made it here because we could not find an exact identifier match for "afterEntity"
             * in the before atlas. However, it is possible that the edge geometry is there, just
             * with a different ID. So if the user set to use geometryMatching, let's check to see
             * if the edge geometry is there.
             */
            if (geometryMatching)
            {
                if (afterEntity instanceof Edge
                        && edgeHasGeometryMatchInAtlas((Edge) afterEntity, beforeAtlas))
                {
                    return Optional.empty();
                }
            }

            /*
             * Ok, we made it here, so that means the afterEntity was not in the before atlas. It
             * was added to the after atlas, so let's return it!
             */
            return Optional.ofNullable(afterEntity);
        }
        return Optional.empty();
    }

    private Optional<AtlasEntity> getRemovedEntityIfNecessary(final AtlasEntity beforeEntity,
            final Atlas beforeAtlas, final Atlas afterAtlas, final boolean geometryMatching)
    {
        /*
         * Look up the beforeEntity ID in the after atlas. If the entity is missing, we know it was
         * removed from the after atlas.
         */
        if (beforeEntity.getType().entityForIdentifier(afterAtlas,
                beforeEntity.getIdentifier()) == null)
        {
            /*
             * We made it here because we could not find an exact identifier match for
             * "beforeEntity" in the after atlas. However, it is possible that the edge geometry is
             * there, just with a different ID. So if the user set to use geometryMatching, let's
             * check to see if the edge geometry is there.
             */
            if (geometryMatching)
            {
                if (beforeEntity instanceof Edge
                        && edgeHasGeometryMatchInAtlas((Edge) beforeEntity, afterAtlas))
                {
                    return Optional.empty();
                }
            }

            /*
             * Ok, we made it here, so that means the beforeEntity was not in the after atlas. It
             * was removed from the after atlas, so let's return it!
             */
            return Optional.ofNullable(beforeEntity);
        }

        /*
         * The beforeEntity was in both the before and after atlases,
         */
        return Optional.empty();
    }
}
