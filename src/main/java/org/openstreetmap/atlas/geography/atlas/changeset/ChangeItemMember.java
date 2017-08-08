package org.openstreetmap.atlas.geography.atlas.changeset;

import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * Represents a member of a {@link ChangeItem}, only valid if {@link ChangeItem} type is
 * {@code ItemType.RELATION}.
 *
 * @author Yiqing Jin
 */
public interface ChangeItemMember
{
    /**
     * @return id of the item member, which is a reference of the member's entity id.
     */
    long getIdentifier();

    /**
     * @return role of the member. refer to OSM WIKI for details about roles in different relation
     *         type.
     */
    String getRole();

    /**
     * @return ItemType of the member
     */
    ItemType getType();
}
