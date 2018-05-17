package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * Expose bugs with the way {@link MultiAtlas} calculates parent relations of its constituent
 * entities. These tests should pass now that all bugs are fixed.
 *
 * @see <a href="https://github.com/osmlab/atlas/pull/126">The PR that demonstrates and corrects
 *      these bugs</a>
 * @author lcram
 */
public class MissingMultiEntityRelationTest
{
    @Test
    public void testAreasForMissingRelation()
    {

    }

    @Test
    public void testEdgesForMissingRelation()
    {
        final List<Location> shapePoints = new ArrayList<>();
        final PackedAtlasBuilder builderWithRelation = new PackedAtlasBuilder();
        builderWithRelation.addNode(1L, Location.forString("1,1"),
                Maps.hashMap("someNode", "inTwoAtlases"));
        builderWithRelation.addNode(2L, Location.forString("2,2"),
                Maps.hashMap("someNode", "inTwoAtlases"));
        shapePoints.add(Location.forString("1,1"));
        shapePoints.add(Location.forString("2,2"));
        builderWithRelation.addEdge(3L, new PolyLine(shapePoints),
                Maps.hashMap("someEdge", "inTwoAtlases"));
        final RelationBean bean = new RelationBean();
        bean.addItem(1L,
                "Node that appears in multiple atlases, but parent relation is only in one of the atlases",
                ItemType.NODE);
        bean.addItem(2L,
                "Node that appears in multiple atlases, but parent relation is only in one of the atlases",
                ItemType.NODE);
        bean.addItem(3L,
                "Edge that appears in multiple atlases, but parent relation is only in one of the atlases",
                ItemType.EDGE);
        builderWithRelation.addRelation(4, 4, bean, Maps.hashMap());

        shapePoints.clear();
        final PackedAtlasBuilder builderNoRelation = new PackedAtlasBuilder();
        builderNoRelation.addNode(1L, Location.forString("1,1"),
                Maps.hashMap("someNode", "inTwoAtlases"));
        builderNoRelation.addNode(2L, Location.forString("2,2"),
                Maps.hashMap("someNode", "inTwoAtlases"));
        shapePoints.add(Location.forString("1,1"));
        shapePoints.add(Location.forString("2,2"));
        builderNoRelation.addEdge(3L, new PolyLine(shapePoints),
                Maps.hashMap("someEdge", "inTwoAtlases"));

        final PackedAtlas atlasWithRelation = (PackedAtlas) builderWithRelation.get();
        final PackedAtlas atlasNoRelation = (PackedAtlas) builderNoRelation.get();

        // 1 MiB resources
        final ByteArrayResource resourceWithRelation = new ByteArrayResource(1024 * 1024 * 1);
        final ByteArrayResource resourceNoRelation = new ByteArrayResource(1024 * 1024 * 1);

        atlasWithRelation.save(resourceWithRelation);
        atlasNoRelation.save(resourceNoRelation);

        final Atlas multiAtlas = new AtlasResourceLoader().load(resourceNoRelation,
                resourceWithRelation);

        for (final AtlasEntity entity : multiAtlas.entities())
        {
            System.out.println(entity.toString());
            System.out.println("MEMBER OF: " + entity.relations().toString());
        }

        Assert.assertTrue(!multiAtlas.edge(1L).relations().isEmpty());
    }

    @Test
    public void testLinesForMissingRelation()
    {

    }

    @Test
    public void testNodesForMissingRelation()
    {
        final PackedAtlasBuilder builderWithRelation = new PackedAtlasBuilder();
        builderWithRelation.addNode(1L, Location.forString("1,1"),
                Maps.hashMap("someNode", "inTwoAtlases"));
        final RelationBean bean = new RelationBean();
        bean.addItem(1L,
                "Node that appears in multiple atlases, but parent relation is only in one of the atlases",
                ItemType.NODE);
        builderWithRelation.addRelation(2, 2, bean, Maps.hashMap());

        final PackedAtlasBuilder builderNoRelation = new PackedAtlasBuilder();
        builderNoRelation.addNode(1L, Location.forString("1,1"),
                Maps.hashMap("someNode", "inTwoAtlases"));

        final PackedAtlas atlasWithRelation = (PackedAtlas) builderWithRelation.get();
        final PackedAtlas atlasNoRelation = (PackedAtlas) builderNoRelation.get();

        // 1 MiB resources
        final ByteArrayResource resourceWithRelation = new ByteArrayResource(1024 * 1024 * 1);
        final ByteArrayResource resourceNoRelation = new ByteArrayResource(1024 * 1024 * 1);

        atlasWithRelation.save(resourceWithRelation);
        atlasNoRelation.save(resourceNoRelation);

        final Atlas multiAtlas = new AtlasResourceLoader().load(resourceNoRelation,
                resourceWithRelation);

        Assert.assertTrue(!multiAtlas.node(1L).relations().isEmpty());
    }

    @Test
    public void testPointsForMissingRelation()
    {

    }

    @Test
    public void testRelationsForMissingRelation()
    {

    }
}
