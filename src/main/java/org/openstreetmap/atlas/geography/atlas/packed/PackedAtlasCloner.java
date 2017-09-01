package org.openstreetmap.atlas.geography.atlas.packed;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;

/**
 * Atlas Cloner. Mostly useful to get a {@link MultiAtlas} and clone it into one single
 * {@link PackedAtlas}
 *
 * @author matthieun
 */
public class PackedAtlasCloner
{
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
        // It's crucial to add relations in lowest order to highest order, to avoid adding a
        // relation which may contain an un-added sub-relation.
        atlas.relationsLowerOrderFirst().forEach(relation -> addRelation(builder, relation));

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
