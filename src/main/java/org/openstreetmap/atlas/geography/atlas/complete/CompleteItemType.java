package org.openstreetmap.atlas.geography.atlas.complete;

import org.openstreetmap.atlas.geography.atlas.items.ItemType;

import java.util.Arrays;

/**
 * A mapping of {@link CompleteEntity}-ies to {@link ItemType}.
 *
 * @author Yazad Khambata
 */
public enum CompleteItemType {

    NODE(CompleteNode.class, ItemType.NODE),
    EDGE(CompleteEdge.class, ItemType.EDGE),
    AREA(CompleteArea.class, ItemType.AREA),
    LINE(CompleteLine.class, ItemType.LINE),
    POINT(CompletePoint.class, ItemType.POINT),
    RELATION(CompleteRelation.class, ItemType.RELATION);

    private final Class<? extends CompleteEntity> completeEntityClass;

    private ItemType itemType;

    CompleteItemType(final Class<? extends CompleteEntity> completeEntityClass, final ItemType itemType) {
        this.completeEntityClass = completeEntityClass;
        this.itemType = itemType;
    }

    public Class<? extends CompleteEntity> getCompleteEntityClass() {
        return completeEntityClass;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public static CompleteItemType from(final ItemType itemType) {
        return Arrays.stream(CompleteItemType.values())
                .filter(completeItemType -> completeItemType.itemType == itemType)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
