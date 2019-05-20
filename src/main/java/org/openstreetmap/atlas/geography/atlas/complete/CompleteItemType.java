package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.Arrays;

import org.apache.commons.lang3.Validate;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * A mapping of {@link CompleteEntity}-ies to {@link ItemType}.
 *
 * @author Yazad Khambata
 */
public enum CompleteItemType
{

    NODE(CompleteNode.class, ItemType.NODE),
    EDGE(CompleteEdge.class, ItemType.EDGE),
    AREA(CompleteArea.class, ItemType.AREA),
    LINE(CompleteLine.class, ItemType.LINE),
    POINT(CompletePoint.class, ItemType.POINT),
    RELATION(CompleteRelation.class, ItemType.RELATION);

    private final Class<? extends CompleteEntity> completeEntityClass;

    private ItemType itemType;

    CompleteItemType(final Class<? extends CompleteEntity> completeEntityClass,
            final ItemType itemType)
    {
        this.completeEntityClass = completeEntityClass;
        this.itemType = itemType;
    }

    public static CompleteItemType from(final ItemType itemType)
    {
        return Arrays.stream(CompleteItemType.values())
                .filter(completeItemType -> completeItemType.itemType == itemType).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public Class<? extends CompleteEntity> getCompleteEntityClass()
    {
        return completeEntityClass;
    }

    public ItemType getItemType()
    {
        return itemType;
    }

    public <C extends CompleteEntity> C completeEntityFrom(final AtlasEntity reference)
    {
        validate(reference);
        return (C) CompleteEntity.from(reference);
    }

    public <C extends CompleteEntity> C completeEntityShallowFrom(final AtlasEntity reference)
    {
        validate(reference);
        return (C) CompleteEntity.shallowFrom(reference);
    }

    public static <C extends CompleteEntity> C shallowFrom(final AtlasEntity reference)
    {
        final ItemType itemType = reference.getType();
        final CompleteItemType completeItemType = CompleteItemType.from(itemType);
        final C c = completeItemType.completeEntityShallowFrom(reference);
        return c;
    }

    private void validate(final AtlasEntity reference)
    {
        Validate.isTrue(getItemType().getMemberClass().isAssignableFrom(reference.getClass()),
                "reference: " + reference + "; cannot be converted to completed entity " + this
                        + ".");
    }
}
