package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Atlas Cloner. Mostly useful to get a {@link MultiAtlas} and clone it into one single
 * {@link PackedAtlas}
 *
 * @author matthieun
 */
public class PackedAtlasCloner
{
    private static final Logger logger = LoggerFactory.getLogger(PackedAtlasCloner.class);
    private static final int MAXIMUM_RELATION_LOOPS = 500;

    private String shardName = null;

    public PackedAtlasCloner()
    {
    }

    public PackedAtlasCloner(final String shardName)
    {
        this.shardName = shardName;
    }

    /**
     * Clone an {@link Atlas}
     *
     * @param atlas
     *            The source {@link Atlas}
     * @return A cloned {@link PackedAtlas}
     */
    public PackedAtlas cloneFrom(final Atlas atlas)
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.setSizeEstimates(atlas.metaData().getSize());
        final AtlasMetaData metaData = atlas.metaData();
        if (this.shardName != null)
        {
            metaData.copyWithNewShardName(this.shardName);
        }
        builder.setMetaData(metaData);
        atlas.nodes().forEach(
                node -> builder.addNode(node.getIdentifier(), node.getLocation(), node.getTags()));
        atlas.edges().forEach(
                edge -> builder.addEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags()));
        atlas.areas().forEach(
                area -> builder.addArea(area.getIdentifier(), area.asPolygon(), area.getTags()));
        atlas.lines().forEach(
                line -> builder.addLine(line.getIdentifier(), line.asPolyLine(), line.getTags()));
        atlas.points().forEach(point -> builder.addPoint(point.getIdentifier(), point.getLocation(),
                point.getTags()));
        // Add all the relations that do not have members that are relations.
        Set<Long> stagedRelationIdentifiers = new HashSet<>();
        for (final Relation relation : atlas.relations())
        {
            boolean skip = false;
            for (final RelationMember member : relation.members())
            {
                if (member.getEntity() instanceof Relation)
                {
                    stagedRelationIdentifiers.add(relation.getIdentifier());
                    skip = true;
                    break;
                }
            }
            if (!skip)
            {
                addRelation(builder, relation);
            }
        }
        // Add all the other relations
        int iterations = 0;
        while (++iterations < MAXIMUM_RELATION_LOOPS && !stagedRelationIdentifiers.isEmpty())
        {
            logger.trace("Copying relations level {} deep.", iterations);
            final Set<Long> stagedRelationIdentifiersCopy = new HashSet<>();
            for (final Long relationIdentifier : stagedRelationIdentifiers)
            {
                final Relation relation = atlas.relation(relationIdentifier);
                boolean skip = false;
                for (final RelationMember member : relation.members())
                {
                    final AtlasEntity entity = member.getEntity();
                    if (entity instanceof Relation
                            && builder.peek().relation(entity.getIdentifier()) == null)
                    {
                        skip = true;
                        break;
                    }
                }
                if (!skip)
                {
                    addRelation(builder, relation);
                }
                else
                {
                    stagedRelationIdentifiersCopy.add(relationIdentifier);
                }
            }
            stagedRelationIdentifiers = stagedRelationIdentifiersCopy;
        }
        if (iterations >= MAXIMUM_RELATION_LOOPS)
        {
            throw new CoreException(
                    "There might be a loop in relations! It took more than {} loops to copy the relation of {}.",
                    MAXIMUM_RELATION_LOOPS, atlas.getName());
        }
        return (PackedAtlas) builder.get();
    }

    private void addRelation(final PackedAtlasBuilder builder, final Relation relation)
    {
        final RelationBean bean = new RelationBean();
        relation.members().forEach(member -> bean.addItem(member.getEntity().getIdentifier(),
                member.getRole(), member.getEntity().getType()));
        builder.addRelation(relation.getIdentifier(), relation.osmRelationIdentifier(), bean,
                relation.getTags());
    }
}
