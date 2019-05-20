package org.openstreetmap.atlas.geography.atlas.complete;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Yazad Khambata
 */
public class CompleteItemTypeTest {
    @Rule
    public CompleteItemTypeTestRule rule = new CompleteItemTypeTestRule();

    @Test
    public void shallowFrom() {
        final Atlas atlas = rule.getAtlas();
        final List<CompleteEntity> completeEntities = toCompleteEntities(atlas);
        validate(completeEntities);
    }

    private List<CompleteEntity> toCompleteEntities(final Atlas atlas) {
        return Arrays.stream(ItemType.values())
                    .map(itemType -> itemType.entityForIdentifier(atlas, 1L))
                    .map(atlasEntity -> {
                        final CompleteEntity completeEntity = CompleteItemType.shallowFrom(atlasEntity);
                        return completeEntity;
                    })
                    .collect(Collectors.toList());
    }

    private void validate(final List<CompleteEntity> completeEntities) {
        Assert.assertNotNull(completeEntities);
        Assert.assertFalse(completeEntities.isEmpty());
        Assert.assertTrue(completeEntities.size() == ItemType.values().length);
        final Set<ItemType> itemTypes = completeEntities.stream()
                .filter(completeEntity -> completeEntity != null)
                .map(CompleteEntity::getType)
                .collect(Collectors.toSet());
        Assert.assertEquals(itemTypes, Arrays.stream(ItemType.values()).collect(Collectors.toSet()));
    }
}