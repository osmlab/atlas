package org.openstreetmap.atlas.geography.atlas.change.diff;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    /*
     * TODO this can be enabled to skip saving bloated features and instead save features from the
     * before or after atlas. However, a Diff saved in this way will not be serializable. However,
     * it will be faster, and may be better for simple printing use-case.
     */
    private final boolean skipBloatedFeatures = false;

    /*
     * TODO have a set of detected changes, construct FeatureChanges at the end. Try to avoid
     * repeated switch(itemType) constructs everywhere.
     */
    // Entities that were removed from the after atlas
    private Set<AtlasEntity> removedEntities;
    // Entities that were added or modified to the after atlas
    private Set<AtlasEntity> addedEntities;

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
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        // Check for entities that were removed in the after atlas
        for (final AtlasEntity beforeEntity : this.before)
        {
            createRemoveFeatureChangeIfNecessary(beforeEntity, this.before, this.after,
                    this.geometryMatching).ifPresent(changeBuilder::add);
        }

        // Check for entities that were added in the after atlas
        for (final AtlasEntity afterEntity : this.after)
        {
            createAddFeatureChangeIfNecessary(afterEntity, this.before, this.after,
                    this.geometryMatching).ifPresent(changeBuilder::add);
        }

        // Check for entities that were changed in the after atlas
        for (final AtlasEntity beforeEntity : this.before)
        {
            createModifyFeatureChangeIfNecessary(beforeEntity, this.before, this.after,
                    this.geometryMatching).ifPresent(changeBuilder::add);
        }

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

    /**
     * Create an ADD {@link FeatureChange} (corresponding to a new feature) for a given
     * {@link AtlasEntity} if necessary. This method only creates an add feature change if the after
     * entity is not found in the before atlas. If geometryMatching is specified, then this method
     * will also look for edges that have different IDs but identical geometry. Those will be
     * counted as matches, and no change will be generated.
     *
     * @param afterEntity
     *            The entity in question from the after atlas
     * @param beforeAtlas
     *            The before atlas
     * @param afterAtlas
     *            The after atlas
     * @param geometryMatching
     *            Use geometry matching
     */
    private Optional<FeatureChange> createAddFeatureChangeIfNecessary(final AtlasEntity afterEntity,
            final Atlas beforeAtlas, final Atlas afterAtlas, final boolean geometryMatching)
    {
        final Long afterEntityIdentifier = afterEntity.getIdentifier();

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
            // NOTE TO REVIEWER TODO this may not be necessary due to new consistent ID atlases?
            if (geometryMatching)
            {
                if (afterEntity instanceof Edge
                        && edgeHasGeometryMatchInAtlas((Edge) afterEntity, beforeAtlas))
                {
                    return Optional.empty();
                }
            }

            FeatureChange featureChange;
            switch (afterEntity.getType())
            {
                case NODE:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedNode.fromNode(afterAtlas.node(afterEntityIdentifier)));
                    break;
                case EDGE:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedEdge.fromEdge(afterAtlas.edge(afterEntityIdentifier)));
                    break;
                case POINT:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedPoint.fromPoint(afterAtlas.point(afterEntityIdentifier)));
                    break;
                case LINE:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedLine.fromLine(afterAtlas.line(afterEntityIdentifier)));
                    break;
                case AREA:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedArea.fromArea(afterAtlas.area(afterEntityIdentifier)));
                    break;
                case RELATION:
                    featureChange = new FeatureChange(ChangeType.ADD, BloatedRelation
                            .fromRelation(afterAtlas.relation(afterEntityIdentifier)));
                    break;
                default:
                    throw new CoreException("Unknown item type {}", afterEntity.getType());
            }
            return Optional.ofNullable(featureChange);
        }
        return Optional.empty();
    }

    /**
     * Create an ADD {@link FeatureChange} (corresponding to a modified feature) for a given
     * {@link AtlasEntity} if necessary. This method only creates an ADD feature change if the
     * before entity is found in the after atlas AND was modified in the after atlas.
     *
     * @param beforeEntity
     *            The entity in question from the before atlas
     * @param beforeAtlas
     *            The before atlas
     * @param afterAtlas
     *            The after atlas
     * @param geometryMatching
     *            Use geometry matching
     */
    private Optional<FeatureChange> createModifyFeatureChangeIfNecessary(
            final AtlasEntity beforeEntity, final Atlas before, final Atlas after,
            final boolean geometryMatching)
    {
        final AtlasEntity afterEntity = beforeEntity.getType().entityForIdentifier(this.after,
                beforeEntity.getIdentifier());

        // Only continue if the entity is in the before atlas AND the after atlas.
        if (afterEntity != null)
        {
            final Long afterEntityIdentifier = afterEntity.getIdentifier();
            FeatureChange featureChange = null;
            // Entity Tags & Entity's Relations first
            // Check the entities for changed tags
            if (!beforeEntity.getTags().equals(afterEntity.getTags()))
            {
                // this.differences.add(new Diff(baseEntity.getType(), DiffType.CHANGED,
                // DiffReason.TAGS, this.before, this.after, identifier));
                switch (afterEntity.getType())
                {
                    case NODE:
                        featureChange = new FeatureChange(ChangeType.ADD,
                                BloatedNode.fromNode(after.node(afterEntityIdentifier)));
                        break;
                    case EDGE:
                        featureChange = new FeatureChange(ChangeType.ADD,
                                BloatedEdge.fromEdge(after.edge(afterEntityIdentifier)));
                        break;
                    case POINT:
                        featureChange = new FeatureChange(ChangeType.ADD,
                                BloatedPoint.fromPoint(after.point(afterEntityIdentifier)));
                        break;
                    case LINE:
                        featureChange = new FeatureChange(ChangeType.ADD,
                                BloatedLine.fromLine(after.line(afterEntityIdentifier)));
                        break;
                    case AREA:
                        featureChange = new FeatureChange(ChangeType.ADD,
                                BloatedArea.fromArea(after.area(afterEntityIdentifier)));
                        break;
                    case RELATION:
                        featureChange = new FeatureChange(ChangeType.ADD, BloatedRelation
                                .fromRelation(after.relation(afterEntityIdentifier)));
                        break;
                    default:
                        throw new CoreException("Unknown item type {}", afterEntity.getType());
                }
            }
            // check the entities for changed relations
            // else if (differentInRelation(baseEntity, alterEntity))
            // {
            // this.differences.add(new Diff(baseEntity.getType(), DiffType.CHANGED,
            // DiffReason.RELATION_MEMBER, this.before, this.after, identifier));
            // }
            return Optional.ofNullable(featureChange);
        }
        return Optional.empty();
    }

    /**
     * Create a REMOVE {@link FeatureChange} for a given {@link AtlasEntity} if necessary. This
     * method only creates a remove feature change if the before entity is not found in the after
     * atlas. If geometryMatching is specified, then this method will also look for edges that have
     * different IDs but identical geometry. Those will be counted as matches, and no change will be
     * generated.
     *
     * @param beforeEntity
     *            The entity in question from the before atlas
     * @param beforeAtlas
     *            The before atlas
     * @param afterAtlas
     *            The after atlas
     * @param geometryMatching
     *            Use geometry matching
     */
    private Optional<FeatureChange> createRemoveFeatureChangeIfNecessary(
            final AtlasEntity beforeEntity, final Atlas beforeAtlas, final Atlas afterAtlas,
            final boolean geometryMatching)
    {
        final Long beforeEntityIdentifier = beforeEntity.getIdentifier();

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
            // NOTE TO REVIEWER TODO this may not be necessary due to new consistent ID atlases?
            if (geometryMatching)
            {
                if (beforeEntity instanceof Edge
                        && edgeHasGeometryMatchInAtlas((Edge) beforeEntity, afterAtlas))
                {
                    return Optional.empty();
                }
            }

            FeatureChange featureChange;
            switch (beforeEntity.getType())
            {
                case NODE:
                    featureChange = new FeatureChange(ChangeType.REMOVE,
                            BloatedNode.shallowFromNode(beforeAtlas.node(beforeEntityIdentifier)));
                    break;
                case EDGE:
                    featureChange = new FeatureChange(ChangeType.REMOVE,
                            BloatedEdge.shallowFromEdge(beforeAtlas.edge(beforeEntityIdentifier)));
                    break;
                case POINT:
                    featureChange = new FeatureChange(ChangeType.REMOVE, BloatedPoint
                            .shallowFromPoint(beforeAtlas.point(beforeEntityIdentifier)));
                    break;
                case LINE:
                    featureChange = new FeatureChange(ChangeType.REMOVE,
                            BloatedLine.shallowFromLine(beforeAtlas.line(beforeEntityIdentifier)));
                    break;
                case AREA:
                    featureChange = new FeatureChange(ChangeType.REMOVE,
                            BloatedArea.shallowFromArea(beforeAtlas.area(beforeEntityIdentifier)));
                    break;
                case RELATION:
                    featureChange = new FeatureChange(ChangeType.REMOVE, BloatedRelation
                            .shallowFromRelation(beforeAtlas.relation(beforeEntityIdentifier)));
                    break;
                default:
                    throw new CoreException("Unknown item type {}", beforeEntity.getType());
            }
            return Optional.ofNullable(featureChange);
        }
        return Optional.empty();
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
}
