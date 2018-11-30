package org.openstreetmap.atlas.geography.atlas.change.diff;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.BareAtlas;
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
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.matching.PolyLineRoute;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate {@link Change} objects based on the differences between two {@link Atlas}es. The main
 * usage of this class is the {@link AtlasDiff#generateChange()} method.<br>
 * TODO this class could utilize parallel streams. However, all stream parallelization uses the same
 * common fork-join pool (https://stackoverflow.com/a/21172732). This could prove an issue in
 * parallel compute environments, since one parallel stream will lock all others. Also, there is
 * significant overhead with setting up parallel streams. Benchmarking is needed here.
 *
 * @author lcram
 */
public class AtlasDiff
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDiff.class);

    private final Atlas before;
    private final Atlas after;
    private boolean useGeometryMatching = false;
    private boolean saveAllGeometries = false;
    private boolean useBloatedEntities = true;

    // Entities that were removed from the after atlas
    private Set<AtlasEntity> removedEntities;

    // Entities that were added to the after atlas
    private Set<AtlasEntity> addedEntities;

    // Entities that were modified in the after atlas
    private Set<AtlasEntity> potentiallyModifiedEntities;

    /**
     * Construct an {@link AtlasDiff} with a given before {@link Atlas} and after {@link Atlas}. The
     * resulting {@link Change} will effectively transform the before atlas into the after atlas.
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
     * Generate a {@link Change} that represents a transformation from the before {@link Atlas} to
     * the after {@link Atlas}.<br>
     * <br>
     * Now suppose we create a {@link ChangeAtlas} based on the before {@link Atlas} and the
     * generated {@link Change}. All of the following will be true:<br>
     * <br>
     * 1) Computing the {@link AtlasDiff} of the {@link ChangeAtlas} with the after {@link Atlas}
     * would yield an empty {@link Change}.<br>
     * <br>
     * 2) The {@link ChangeAtlas} will be equivalent to the after {@link Atlas},
     * feature-for-feature.<br>
     * <br>
     * 3) The {@link PackedAtlas}es created by cloning both the {@link ChangeAtlas} and the after
     * {@link Atlas} will be equivalent under {@link BareAtlas#equals(Object)}, but may not
     * necessarily be byte-for-byte equivalent.<br>
     * <br>
     *
     * @return the generated {@link Change}
     */
    public Change generateChange()
    {
        this.addedEntities = ConcurrentHashMap.newKeySet();
        this.removedEntities = ConcurrentHashMap.newKeySet();
        this.potentiallyModifiedEntities = ConcurrentHashMap.newKeySet();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        /*
         * Check for entities that were removed in the after atlas. If we find any, add them to a
         * removedEntities set for later processing. We will use this set to create FeatureChanges.
         */
        Iterables.stream(this.before)
                .filter(beforeEntity -> isEntityMissingFromGivenAtlas(beforeEntity, this.after,
                        this.useGeometryMatching))
                .forEach(this.removedEntities::add);

        /*
         * Check for entities that were added in the after atlas. If we find any, add them to a
         * addedEntities set for later processing. We will use this set to create FeatureChanges.
         */
        Iterables.stream(this.after)
                .filter(afterEntity -> isEntityMissingFromGivenAtlas(afterEntity, this.before,
                        this.useGeometryMatching))
                .forEach(this.addedEntities::add);

        /*
         * Check for entities that were potentially modified in the after atlas (here, a potentially
         * modified entity is simple any entity what was present in both the before and after
         * atlas). If we find any, add them to a set for later processing. We will use this set to
         * create FeatureChanges. TODO does the useGeometryMatching setting still make sense here?
         */
        Iterables.stream(this.before)
                .filter(beforeEntity -> !isEntityMissingFromGivenAtlas(beforeEntity, this.after,
                        this.useGeometryMatching))
                .forEach(this.potentiallyModifiedEntities::add);

        /*
         * Aggregate the results stored in the sets, creating the FeatureChange objects if there are
         * necessary changes.
         */
        createFeatureChangesBasedOnChangedEntities(this.addedEntities, this.removedEntities,
                this.potentiallyModifiedEntities, this.before, this.after, this.useBloatedEntities,
                this.saveAllGeometries).stream().forEach(changeBuilder::add);

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
     * change the geometry. This is useful for visualization. If
     * {@link AtlasDiff#useBloatedEntities(boolean)} is set to false, then this setting is
     * effectively ignored - since all geometries will already be present.
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
     * before or after atlas. A {@link Change} saved in this way will not be serializable. However,
     * computing the change will be faster, so this may be better for the simple printing
     * use-case.<br>
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
     * sectioned OSM way will be different, the underlying geometry will be the same. In that case,
     * {@link AtlasDiff#useGeometryMatching(boolean)} will cause {@link AtlasDiff} to ignore the
     * inconsistent way sectioning.
     *
     * @param useGeometryMatching
     *            use geometry matching
     * @return a configured {@link AtlasDiff}
     */
    public AtlasDiff useGeometryMatching(final boolean useGeometryMatching)
    {
        this.useGeometryMatching = useGeometryMatching;
        return this;
    }

    private Set<FeatureChange> createFeatureChangesBasedOnChangedEntities(
            final Set<AtlasEntity> addedEntities, final Set<AtlasEntity> removedEntities,
            final Set<AtlasEntity> modifiedEntities, final Atlas beforeAtlas,
            final Atlas afterAtlas, final boolean useBloatedEntities,
            final boolean saveAllGeometries)
    {
        final Set<FeatureChange> featureChanges = ConcurrentHashMap.newKeySet();

        addedEntities.stream()
                .map(addedEntity -> createFeatureChangeWithType(addedEntity, afterAtlas,
                        useBloatedEntities, saveAllGeometries, ChangeType.ADD))
                .forEach(featureChanges::add);

        removedEntities.stream()
                .map(removedEntity -> createFeatureChangeWithType(removedEntity, beforeAtlas,
                        useBloatedEntities, saveAllGeometries, ChangeType.REMOVE))
                .forEach(featureChanges::add);

        // TODO handle modified entities

        return featureChanges;
    }

    private FeatureChange createFeatureChangeWithType(final AtlasEntity entity, final Atlas atlas,
            final boolean useBloatedEntities, final boolean saveAllGeometries,
            final ChangeType changeType)
    {
        FeatureChange featureChange;
        final Long addedEntityIdentifier = entity.getIdentifier();
        if (useBloatedEntities)
        {
            switch (entity.getType())
            {
                /*
                 * TODO these cases all use the deep Bloated copy. In the case of changeType=REMOVE,
                 * we want to use a shallow copy. And we also need to be able to control this more
                 * granularly, in the case of an ADD that is a modify. Additionally, we are
                 * currently ignoring both the useBloatedEntities and the saveAllGeometries flags.
                 */
                case NODE:
                    featureChange = new FeatureChange(changeType,
                            BloatedNode.from(atlas.node(addedEntityIdentifier)));
                    break;
                case EDGE:
                    featureChange = new FeatureChange(changeType,
                            BloatedEdge.from(atlas.edge(addedEntityIdentifier)));
                    break;
                case POINT:
                    featureChange = new FeatureChange(changeType,
                            BloatedPoint.from(atlas.point(addedEntityIdentifier)));
                    break;
                case LINE:
                    featureChange = new FeatureChange(changeType,
                            BloatedLine.from(atlas.line(addedEntityIdentifier)));
                    break;
                case AREA:
                    featureChange = new FeatureChange(changeType,
                            BloatedArea.from(atlas.area(addedEntityIdentifier)));
                    break;
                case RELATION:
                    featureChange = new FeatureChange(changeType,
                            BloatedRelation.from(atlas.relation(addedEntityIdentifier)));
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

    /**
     * Check if a given entity is missing from a given atlas. Optionally, we can match using the
     * underlying geometry if the itemType/identifier check fails.
     *
     * @param entity
     *            the entity to check for
     * @param atlasToCheck
     *            the atlas to check
     * @param useGeometryMatching
     *            use geometry matching
     * @return if the entity was missing from the atlas
     */
    private boolean isEntityMissingFromGivenAtlas(final AtlasEntity entity,
            final Atlas atlasToCheck, final boolean useGeometryMatching)
    {
        /*
         * Look up the given entity's ID in the atlasToCheck. If the returned entity is null, we
         * know it was NOT PRESENT in the atlasToCheck.
         */
        if (entity.getType().entityForIdentifier(atlasToCheck, entity.getIdentifier()) == null)
        {
            if (useGeometryMatching)
            {
                /*
                 * We made it here because we could not find an exact identifier match for "entity"
                 * in the atlasToCheck. However, it is possible that the edge geometry is there,
                 * just with a different ID. So if the user set to useGeometryMatching, let's check
                 * to see if the edge geometry is there.
                 */
                if (entity instanceof Edge
                        && edgeHasGeometryMatchInAtlas((Edge) entity, atlasToCheck))
                {
                    return false;
                }
            }

            /*
             * Ok, we made it here, so that means the entity was not in the atlasToCheck.
             */
            return true;
        }

        /*
         * The entity was in the atlasToCheck!
         */
        return false;
    }
}
