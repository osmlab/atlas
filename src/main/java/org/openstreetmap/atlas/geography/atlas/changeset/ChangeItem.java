package org.openstreetmap.atlas.geography.atlas.changeset;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.tags.Taggable;

/**
 * A change item represent a result generated from data enhancement process.
 *
 * @author Yiqing Jin
 * @deprecated - see new API under org.openstreetmap.atlas.geography.atlas.change package.
 */
@Deprecated
public interface ChangeItem extends Taggable, Serializable
{
    /**
     * @return action type of this change item
     */
    ChangeAction getAction();

    /**
     * this needs further thought. We should use a Atlas data type but right now there is no generic
     * type for all geometry types.
     *
     * @return the geometry of the change item, if has it.
     */
    Iterable<Location> getGeometry();

    /**
     * identifier of change item. It's required to be unique within one change set. For newly
     * created item please use negative value
     *
     * @return the unique identifier of change item
     */
    long getIdentifier();

    /**
     * @return item member list if exists. It should only have value if ItemType is RELATION
     */
    Iterable<ChangeItemMember> getMembers();

    /**
     * create a new RelationBean object from item members
     *
     * @return a relation bean object to use for AtlasBuilder.
     */
    Optional<RelationBean> getRelationBean();

    /**
     * @return the score of the item, actual meaning varies base on source type and conflation
     *         process, value should always be between 0-1 with 0 for lowest rank and 1 for highest
     *         rank. If score is not needed it should always return 1.
     */
    default double getScore()
    {
        return 1;
    }

    /**
     * @return the source name from which the change set is generated.
     */
    String getSourceName();

    /**
     * @return tag map of this item
     */
    @Override
    Map<String, String> getTags();

    /**
     * @return item type
     */
    ItemType getType();
}
