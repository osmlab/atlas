package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.exception.AtlasIntegrityException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AtlasBuilder} for a {@link PackedAtlas}. This is not thread safe!
 *
 * @author matthieun
 */
public final class PackedAtlasBuilder implements AtlasBuilder
{
    private static final Logger logger = LoggerFactory.getLogger(PackedAtlasBuilder.class);

    private static final int MAXIMUM_RELATION_MEMBER_DEPTH = 500;
    // In very rare cases, Way Slicing will return slightly non-deterministic cut locations in
    // different shards. This tolerance allows the PackedAtlasBuilder to identify very closeby nodes
    // and use them instead.
    private static final Distance NODE_SEARCH_DISTANCE = Distance.ONE_METER;
    private static final Distance NODE_TOLERANCE_DISTANCE = Distance.meters(0.1);

    private PackedAtlas atlas;
    private AtlasSize sizeEstimates = AtlasSize.DEFAULT;
    private boolean locked = false;
    private String name;

    private AtlasMetaData metaData = new AtlasMetaData();

    @Override
    public void addArea(final long identifier, final Polygon geometry,
            final Map<String, String> tags)
    {
        initialize();
        try
        {
            this.atlas.addArea(identifier, geometry, tags);
        }
        catch (final Exception e)
        {
            logger.error("Error adding Area ({}): {}", identifier, geometry.toWkt(), e);
        }
    }

    @Override
    public void addEdge(final long identifier, final PolyLine geometry,
            final Map<String, String> tags)
    {
        initialize();
        final Location start = geometry.first();
        final Location end = geometry.last();
        Long startNodeIdentifier = this.atlas.nodeIdentifierForLocation(start);
        Long endNodeIdentifier = this.atlas.nodeIdentifierForLocation(end);
        if (startNodeIdentifier == null)
        {
            startNodeIdentifier = this.atlas.nodeIdentifierForEnlargedLocation(start,
                    NODE_SEARCH_DISTANCE, NODE_TOLERANCE_DISTANCE);
            if (startNodeIdentifier == null)
            {
                throw new AtlasIntegrityException(
                        "Atlas does not contain Node for Location {} for edge {}", start,
                        identifier);
            }
            logger.warn(
                    "Atlas does not contain Node for Location {} for edge {}. "
                            + "Found very close node {} and using it instead.",
                    start, identifier, startNodeIdentifier);
        }
        if (endNodeIdentifier == null)
        {
            endNodeIdentifier = this.atlas.nodeIdentifierForEnlargedLocation(end,
                    NODE_SEARCH_DISTANCE, NODE_TOLERANCE_DISTANCE);
            if (endNodeIdentifier == null)
            {
                throw new AtlasIntegrityException(
                        "Atlas does not contain Node for Location {} for edge {}", end, identifier);
            }
            logger.warn(
                    "Atlas does not contain Node for Location {} for edge {}. "
                            + "Found very close node {} and using it instead.",
                    end, identifier, endNodeIdentifier);
        }
        try
        {
            this.atlas.addEdge(identifier, startNodeIdentifier, endNodeIdentifier, geometry, tags);
        }
        catch (final AtlasIntegrityException e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            logger.error("Error adding Edge ({}): {}", identifier, geometry.toWkt(), e);
        }
    }

    @Override
    public void addLine(final long identifier, final PolyLine geometry,
            final Map<String, String> tags)
    {
        initialize();
        try
        {
            this.atlas.addLine(identifier, geometry, tags);
        }
        catch (final AtlasIntegrityException e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            logger.error("Error adding Line ({}): {}", identifier, geometry.toWkt(), e);
        }
    }

    @Override
    public void addNode(final long identifier, final Location geometry,
            final Map<String, String> tags)
    {
        initialize();
        try
        {
            this.atlas.addNode(identifier, geometry, tags);
        }
        catch (final AtlasIntegrityException e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            logger.error("Error adding Node ({}): {}", identifier, geometry.toWkt(), e);
        }
    }

    @Override
    public void addPoint(final long identifier, final Location geometry,
            final Map<String, String> tags)
    {
        initialize();
        try
        {
            this.atlas.addPoint(identifier, geometry, tags);
        }
        catch (final AtlasIntegrityException e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            logger.error("Error adding Point ({}): {}", identifier, geometry.toWkt(), e);
        }
    }

    @Override
    public void addRelation(final long identifier, final long osmIdentifier,
            final RelationBean structure, final Map<String, String> tags)
    {
        if (structure.isEmpty())
        {
            throw new CoreException("Cannot add relation {} with an empty member list.",
                    identifier);
        }
        initialize();
        try
        {
            this.atlas.addRelation(identifier, osmIdentifier, structure.getMemberIdentifiers(),
                    structure.getMemberTypes(), structure.getMemberRoles(), tags);
        }
        catch (final AtlasIntegrityException e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            logger.error("Error adding Relation ({}): {}", identifier, structure.toString(), e);
        }
    }

    @Override
    public Atlas get()
    {
        initialize();
        this.locked = true;
        if (this.atlas.isEmpty())
        {
            logger.warn("An Atlas is Located, and therefore cannot be empty.");
            return null;
        }
        if (Iterables.size(this.atlas) == this.atlas.numberOfRelations())
        {
            logger.warn(
                    "An Atlas is Located, and therefore cannot be made of only relations (which cannot be loacted as there are no other features).");
            return null;
        }
        verifyNegativeEdgesHaveMasterEdge();
        this.atlas.relations().forEach(relation ->
        {
            try
            {
                // Make sure that the relations are not looping to each other, or just bounds-less.
                validateRelation(relation, relation.getIdentifier(), 0);
            }
            catch (final Exception e)
            {
                throw new CoreException("Relation {} is corrupted. Invalidating Atlas!",
                        relation.getIdentifier(), e);
            }
        });
        // Update the meta data so the Atlas sizes are correct.
        final AtlasSize updatedAtlasSize = new AtlasSize(this.atlas.numberOfEdges(),
                this.atlas.numberOfNodes(), this.atlas.numberOfAreas(), this.atlas.numberOfLines(),
                this.atlas.numberOfPoints(), this.atlas.numberOfRelations());
        this.atlas.setMetaData(this.metaData.copyWithNewSize(updatedAtlasSize));
        return this.atlas;
    }

    public PackedAtlas peek()
    {
        initialize();
        return this.atlas;
    }

    @Override
    public void setMetaData(final AtlasMetaData metaData)
    {
        this.metaData = metaData;
    }

    @Override
    public void setSizeEstimates(final AtlasSize estimates)
    {
        this.sizeEstimates = estimates;
    }

    public PackedAtlasBuilder withMetaData(final AtlasMetaData metaData)
    {
        setMetaData(metaData);
        return this;
    }

    public PackedAtlasBuilder withName(final String name)
    {
        this.name = name;
        return this;
    }

    public PackedAtlasBuilder withSizeEstimates(final AtlasSize estimates)
    {
        setSizeEstimates(estimates);
        return this;
    }

    private void initialize()
    {
        if (this.locked)
        {
            throw new CoreException("Cannot keep adding items to a locked graph.");
        }
        if (this.atlas == null)
        {
            this.atlas = new PackedAtlas(this.sizeEstimates);
            this.atlas.setName(this.name);
        }
    }

    /**
     * Recursive call to make sure that the relations are really bounded and do not loop on each
     * other.
     *
     * @param relation
     */
    private void validateRelation(final Relation relation, final long parentIdentifier,
            final int depth)
    {
        if (depth > MAXIMUM_RELATION_MEMBER_DEPTH)
        {
            throw new CoreException(
                    "Relation {} referencing each other more than {} levels deep, without hitting any bounded feature.",
                    parentIdentifier, MAXIMUM_RELATION_MEMBER_DEPTH);
        }
        for (final RelationMember member : relation.members())
        {
            if (member.getEntity() instanceof AtlasItem)
            {
                return;
            }
            else
            {
                validateRelation((Relation) member.getEntity(), parentIdentifier, depth + 1);
            }
        }
    }

    private void verifyNegativeEdgesHaveMasterEdge()
    {
        this.atlas.edges().forEach(edge ->
        {
            final long edgeIdentifier = edge.getIdentifier();
            if (edgeIdentifier < 0 && this.atlas.edge(-edgeIdentifier) == null)
            {
                throw new AtlasIntegrityException(
                        "Cannot build an Atlas with a negative edge without its positive counterpart: {}",
                        edgeIdentifier);
            }
        });
    }
}
