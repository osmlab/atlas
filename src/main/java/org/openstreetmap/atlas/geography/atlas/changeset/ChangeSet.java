package org.openstreetmap.atlas.geography.atlas.changeset;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * A ChangeSet is a set of {@link ChangeItem} generated from side files for a specific Atlas data
 * set. A side file is a data file generated from other sources or validation program to improve the
 * data quality and/or coverage.
 *
 * @author Yiqing Jin
 * @deprecated - see new API under org.openstreetmap.atlas.geography.atlas.change package.
 */
@Deprecated
@SuppressWarnings("squid:S1874")
public interface ChangeSet extends Set<ChangeItem>, Serializable
{
    /**
     * @param identifier
     *            the identifier of changeItem
     * @param type
     *            item type
     * @return true if item exists and false if not
     */
    boolean contains(long identifier, ItemType type);

    /**
     * @param identifier
     *            the identifier of changeItem
     * @param type
     *            item type
     * @param action
     *            the action of change item
     * @return true if item exists and false if not
     */
    boolean contains(long identifier, ItemType type, ChangeAction action);

    /**
     * Ideally there should only be one ChangeItem for the combination of identifier and ItemType.
     * the method will ONLY return ONE item that matches. If the implementation allows multiple
     * ChangeAction in one change set, please use {@link #get(long, ItemType, ChangeAction)}
     *
     * @param identifier
     *            the identifier of change item
     * @param type
     *            the type of item
     * @return the {@link ChangeItem} that matches the input.
     * @see #get(long, ItemType, ChangeAction)
     */
    Optional<ChangeItem> get(long identifier, ItemType type);

    /**
     * @param identifier
     *            the identifier of change item
     * @param type
     *            the type of item
     * @param action
     *            the action of change item
     * @return the {@link ChangeItem} that matches the input
     */
    Optional<ChangeItem> get(long identifier, ItemType type, ChangeAction action);

    /**
     * human readable description of the change set
     *
     * @return description of change set
     */
    String getDescription();

    /**
     * @return the source name from which the change set is generated. In case of multiple sources
     *         names are joined by comma(,)
     */
    Iterable<String> getSourceNames();

    /**
     * Change set version should be same as atlas version against which it's generated.
     *
     * @return version of change set, same as atlas version.
     */
    String getVersion();

    /**
     * @param action
     *            action to filter
     * @return a iterator for all {@link ChangeItem}s that matches the action
     */
    Iterator<ChangeItem> iterator(ChangeAction action);

    /**
     * @param type
     *            type to filer
     * @return a iterator for all {@link ChangeItem}s that matches the type
     */
    Iterator<ChangeItem> iterator(ItemType type);

    /**
     * @param type
     *            type to filter
     * @param action
     *            action to filter
     * @return a iterator for all {@link ChangeItem}s that matches both the type and the action
     */
    Iterator<ChangeItem> iterator(ItemType type, ChangeAction action);

    /**
     * @param description
     *            description of the change set
     */
    void setDescription(String description);

    /**
     * set version of the ChangeSet, the version should be same as atlas version against which it's
     * generated.
     *
     * @param version
     *            version of the change set
     */
    void setVersion(String version);

    /**
     * @param action
     *            {@link ChangeAction} the return sub set should have.
     * @return a set of all items with given action
     */
    Set<ChangeItem> subSet(ChangeAction action);

    /**
     * @param type
     *            the type of item
     * @return a set of all items with given type
     */
    Set<ChangeItem> subSet(ItemType type);

    /**
     * get all items with given type and action.
     *
     * @param type
     *            the type of item
     * @param action
     *            item action
     * @return a set of items with given type and action
     */
    Set<ChangeItem> subSet(ItemType type, ChangeAction action);
}
