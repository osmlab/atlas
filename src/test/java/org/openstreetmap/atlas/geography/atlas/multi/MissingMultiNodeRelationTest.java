package org.openstreetmap.atlas.geography.atlas.multi;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * Expose a bug with the way {@link MultiNode} calculates parent relations of its constituent sub
 * nodes. This test should pass now that the bug is fixed.
 *
 * @author lcram
 */
public class MissingMultiNodeRelationTest
{
    @Test
    public void testForMissingRelation()
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
}
