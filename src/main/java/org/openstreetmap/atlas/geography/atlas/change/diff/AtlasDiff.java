package org.openstreetmap.atlas.geography.atlas.change.diff;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.BareAtlas;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate {@link Change} objects based on the differences between two {@link Atlas}es. The main
 * usage of this class is the {@link AtlasDiff#generateChange()} method.<br>
 * <br>
 *
 * @author lcram
 */
public class AtlasDiff
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDiff.class);

    private final Atlas before;
    private final Atlas after;
    private Change change;

    /*
     * By default, AtlasDiff will save geometries of all changed features, even if that feature's
     * geometry was not changed. This makes it really easy to visualize changed features using
     * GeoJSON (e.g. if a Node tag changes, we still want to be able to see it in the GeoJSON
     * serialized Change object). This behaviour can be disabled.
     */
    private boolean saveAllGeometries = true;

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
        this.change = null;

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
     * the after {@link Atlas}. If there were no changes (i.e. the atlases had no differences), then
     * return an empty {@link Optional}. Repeated calls to this method will return a cached
     * {@link Change} instead of performing a re-computation.<br>
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
     * @return an {@link Optional} wrapping the generated {@link Change}
     */
    public Optional<Change> generateChange()
    {
        if (this.change != null)
        {
            return Optional.of(this.change);
        }

        final Set<AtlasEntity> addedEntities = new HashSet<>();
        final Set<AtlasEntity> removedEntities = new HashSet<>();
        final Set<AtlasEntity> potentiallyModifiedEntities = new HashSet<>();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        /*
         * Check for entities that were removed in the after atlas. If we find any, add them to a
         * removedEntities set for later processing. We will use this set to create FeatureChanges.
         * We also check for entities that were potentially modified in the after atlas, i.e. any
         * entities present in both the before and after atlases. We add them to a
         * potentiallyModified set, and will check if any modifications occurred later.
         */
        Iterables.stream(this.before).forEach(beforeEntity ->
        {
            if (isEntityMissingFromGivenAtlas(beforeEntity, this.after))
            {
                removedEntities.add(beforeEntity);
            }
            else
            {
                potentiallyModifiedEntities.add(beforeEntity);
            }
        });

        /*
         * Check for entities that were added in the after atlas. If we find any, add them to a
         * addedEntities set for later processing. We will use this set to create FeatureChanges.
         */
        Iterables.stream(this.after)
                .filter(afterEntity -> isEntityMissingFromGivenAtlas(afterEntity, this.before))
                .forEach(addedEntities::add);

        /*
         * Aggregate the results stored in the sets, creating the FeatureChange objects if there are
         * necessary changes.
         */
        createFeatureChangesBasedOnEntitySets(addedEntities, removedEntities,
                potentiallyModifiedEntities, this.before, this.after, this.saveAllGeometries)
                        .stream().forEach(changeBuilder::add);

        if (changeBuilder.peekNumberOfChanges() == 0)
        {
            return Optional.empty();
        }
        this.change = changeBuilder.get();
        return Optional.of(this.change);
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
     * change the geometry. This is useful for visualization.
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

    private Set<FeatureChange> createFeatureChangesBasedOnEntitySets(
            final Set<AtlasEntity> addedEntities, final Set<AtlasEntity> removedEntities,
            final Set<AtlasEntity> potentiallyModifiedEntities, final Atlas beforeAtlas,
            final Atlas afterAtlas, final boolean saveAllGeometries)
    {
        final Set<FeatureChange> featureChanges = new HashSet<>();

        addedEntities.stream().map(addedEntity -> createSimpleFeatureChangeWithType(ChangeType.ADD,
                addedEntity, afterAtlas, saveAllGeometries)).forEach(featureChanges::add);

        removedEntities.stream()
                .map(removedEntity -> createSimpleFeatureChangeWithType(ChangeType.REMOVE,
                        removedEntity, beforeAtlas, saveAllGeometries))
                .forEach(featureChanges::add);

        potentiallyModifiedEntities.stream()
                .map(modifiedEntity -> createModifyFeatureChanges(modifiedEntity, beforeAtlas,
                        afterAtlas, saveAllGeometries))
                .forEach(modifyFeatureChangeSet -> modifyFeatureChangeSet
                        .forEach(featureChanges::add));

        return featureChanges;
    }

    /**
     * Create a set of modify {@link FeatureChange}s ie. ADDs that change an existing feature. The
     * set will contain an individual {@link FeatureChange} for certain types of differences. Eg. if
     * a Point's location AND tags changed, then the set will contain two feature changes, one for
     * the location and one for the tags. However, if a Node's location and connected edges change,
     * this will come in as one feature change.<br>
     *
     * @param entity
     * @param beforeAtlas
     * @param afterAtlas
     * @param useGeometryMatching
     * @param useBloatedEntities
     * @param saveAllGeometries
     * @return a {@link Set} containing the possibly constructed {@link FeatureChange}s
     */
    private Set<FeatureChange> createModifyFeatureChanges(final AtlasEntity entity,
            final Atlas beforeAtlas, final Atlas afterAtlas, final boolean saveAllGeometries)
    {
        final Set<FeatureChange> featureChanges = new HashSet<>();

        final AtlasEntity beforeEntity = entity.getType().entityForIdentifier(this.before,
                entity.getIdentifier());
        final AtlasEntity afterEntity = entity.getType().entityForIdentifier(this.after,
                entity.getIdentifier());

        if (beforeEntity == null || afterEntity == null)
        {
            throw new CoreException("Unexpected null entity. This should never happen.");
        }

        /*
         * Determine if the entities are actually different in any relevant way. If so, then we can
         * decide how to create the feature change.
         */

        /*
         * Detect changed tags.
         */
        AtlasDiffHelper.getTagChangeIfNecessary(beforeEntity, afterEntity, saveAllGeometries)
                .ifPresent(featureChanges::add);

        /*
         * Detect if the entity changed its parent relation membership.
         */
        AtlasDiffHelper.getParentRelationMembershipChangeIfNecessary(beforeEntity, afterEntity,
                saveAllGeometries).ifPresent(featureChanges::add);

        /*
         * Detect if the entities were Nodes and some Node properties changed. We check for Node
         * locations, as well as inEdges and outEdges.
         */
        if (entity instanceof Node)
        {
            AtlasDiffHelper.getNodeChangeIfNecessary((Node) beforeEntity, (Node) afterEntity,
                    saveAllGeometries).ifPresent(featureChanges::add);
        }
        /*
         * Detect if the entities were Edges and some Edge properties changed. We check for Edge
         * polylines, as well as the start and end Node.
         */
        else if (entity instanceof Edge)
        {
            AtlasDiffHelper.getEdgeChangeIfNecessary((Edge) beforeEntity, (Edge) afterEntity,
                    saveAllGeometries).ifPresent(featureChanges::add);
        }
        /*
         * Detect if the entities were Points and some Point properties changed. We just check the
         * location.
         */
        else if (entity instanceof Point)
        {
            AtlasDiffHelper.getPointChangeIfNecessary((Point) beforeEntity, (Point) afterEntity)
                    .ifPresent(featureChanges::add);
        }
        /*
         * Detect if the entities were Lines and some Line properties changed. We just check the
         * polyline.
         */
        else if (entity instanceof Line)
        {
            AtlasDiffHelper.getLineChangeIfNecessary((Line) beforeEntity, (Line) afterEntity)
                    .ifPresent(featureChanges::add);
        }
        /*
         * Detect if the entities were Areas and some Area properties changed. We just check the
         * polygon.
         */
        else if (entity instanceof Area)
        {
            AtlasDiffHelper.getAreaChangeIfNecessary((Area) beforeEntity, (Area) afterEntity)
                    .ifPresent(featureChanges::add);
        }
        /*
         * Detect if the entities were Relations and some Relation properties changed. We check the
         * member lists for changes (changes include a removed or added member or a member role
         * change). Note that the relation diff checking is not deep, i.e. it only looks at the
         * relation data itself (member ID, role, and type). For example, if a contained Area's
         * geometry or tags change, this will not generate a relation change for relations
         * containing that Area.
         */
        else if (entity instanceof Relation)
        {
            AtlasDiffHelper
                    .getRelationChangeIfNecessary((Relation) beforeEntity, (Relation) afterEntity)
                    .ifPresent(featureChanges::add);
        }

        return featureChanges;
    }

    /**
     * Create a simple {@link FeatureChange} ie. a REMOVE, or an ADD that is adding a new feature
     * (as opposed to modifying an existing feature). The feature change will be created from the
     * given entity in the given atlas.
     *
     * @param changeType
     * @param entity
     * @param atlas
     * @param useBloatedEntities
     * @param saveAllGeometries
     * @return the feature change
     */
    private FeatureChange createSimpleFeatureChangeWithType(final ChangeType changeType,
            final AtlasEntity entity, final Atlas atlas, final boolean saveAllGeometries)
    {
        final FeatureChange featureChange;
        switch (entity.getType())
        {
            case NODE:
                featureChange = AtlasDiffHelper.simpleBloatedNodeChange(changeType, atlas, entity,
                        saveAllGeometries);
                break;
            case EDGE:
                featureChange = AtlasDiffHelper.simpleBloatedEdgeChange(changeType, atlas, entity,
                        saveAllGeometries);
                break;
            case POINT:
                featureChange = AtlasDiffHelper.simpleBloatedPointChange(changeType, atlas, entity,
                        saveAllGeometries);
                break;
            case LINE:
                featureChange = AtlasDiffHelper.simpleBloatedLineChange(changeType, atlas, entity,
                        saveAllGeometries);
                break;
            case AREA:
                featureChange = AtlasDiffHelper.simpleBloatedAreaChange(changeType, atlas, entity,
                        saveAllGeometries);
                break;
            case RELATION:
                featureChange = AtlasDiffHelper.simpleBloatedRelationChange(changeType, atlas,
                        entity);
                break;
            default:
                throw new CoreException("Unknown item type {}", entity.getType());
        }
        return featureChange;
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
            final Atlas atlasToCheck)
    {
        /*
         * Look up the given entity's ID in the atlasToCheck. If the returned entity is null, we
         * know it was NOT PRESENT in the atlasToCheck.
         */
        return entity.getType().entityForIdentifier(atlasToCheck, entity.getIdentifier()) == null;
    }
}
