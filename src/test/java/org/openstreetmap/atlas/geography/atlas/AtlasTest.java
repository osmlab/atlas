package org.openstreetmap.atlas.geography.atlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;

/**
 * @author Yazad Khambata
 */
public class AtlasTest
{

    @Rule
    public final BareAtlasTestRule rule = new BareAtlasTestRule();

    @Test
    public void nodesByIds()
    {
        final Atlas atlas = rule.getAtlas();
        final Iterable<Node> nodes = atlas.nodes(0L, 1L);
        final long count = StreamSupport.stream(nodes.spliterator(), false).count();
        Assert.assertEquals(2, count);
    }

    @Test
    public void entitiesByIds()
    {
        final Atlas atlas = rule.getAtlas();
        final Map<ItemType, ? extends Iterable<? extends AtlasEntity>> itemTypeToEntities = Arrays
                .stream(ItemType.values())
                .map(itemType -> Pair.of(itemType, itemType.entitiesForIdentifiers(atlas, 0L, 1L)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        Assert.assertEquals(ItemType.values().length, itemTypeToEntities.size());
        itemTypeToEntities.entrySet().forEach(entry ->
        {
            final ItemType itemType = entry.getKey();
            final List<AtlasEntity> atlasEntities = new ArrayList<>((Collection) entry.getValue());
            Assert.assertNotNull(atlasEntities);
            Assert.assertFalse(atlasEntities.isEmpty());
            Assert.assertEquals("itemType: " + itemType, 2, atlasEntities.size());
            atlasEntities.forEach(atlasEntity ->
            {
                final Class<AtlasEntity> memberClass = itemType.getMemberClass();
                final Class<? extends AtlasEntity> atlasEntityClass = atlasEntity.getClass();
                Assert.assertTrue(
                        "itemType: " + itemType + "; memberClass: " + memberClass
                                + "; atlasEntityClass: " + atlasEntityClass,
                        memberClass.isAssignableFrom(atlasEntityClass));
            });

        });
    }
}
