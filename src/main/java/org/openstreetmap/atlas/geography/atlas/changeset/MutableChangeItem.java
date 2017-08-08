package org.openstreetmap.atlas.geography.atlas.changeset;

import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * This interface is designed to be used by data enhancing and data merging programs which may
 * require frequently changing values on the fly.
 *
 * @author Yiqing Jin
 */
public interface MutableChangeItem extends ChangeItem
{
    void addAllMembers(Iterable<ChangeItemMember> members) throws CoreException;

    void addMember(ChangeItemMember member) throws CoreException;

    boolean removeMember(long identifier, String role, ItemType type) throws CoreException;

    void setAction(ChangeAction action);

    void setGeometry(Iterable<Location> geometry) throws CoreException;

    void setIdentifier(long identifier);

    /**
     * @param score
     *            the score of the item, actual meaning varies base on source type and conflation
     *            process
     */
    void setScore(double score);

    void setSourceName(String sourceName);

    void setTags(Map<String, String> tags);

    void setType(ItemType type);
}
