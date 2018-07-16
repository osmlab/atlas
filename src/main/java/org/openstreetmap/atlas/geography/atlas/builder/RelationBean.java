package org.openstreetmap.atlas.geography.atlas.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * @author matthieun
 */
public class RelationBean implements Serializable
{
    private static final long serialVersionUID = 8511830231633569713L;

    private final List<Long> memberIdentifiers;
    private final List<String> memberRoles;
    private final List<ItemType> memberTypes;

    public RelationBean()
    {
        this.memberIdentifiers = new ArrayList<>();
        this.memberRoles = new ArrayList<>();
        this.memberTypes = new ArrayList<>();
    }

    public void addItem(final Long identifier, final String role, final ItemType itemType)
    {
        this.memberIdentifiers.add(identifier);
        this.memberRoles.add(role);
        this.memberTypes.add(itemType);
    }

    public List<Long> getMemberIdentifiers()
    {
        return this.memberIdentifiers;
    }

    public List<String> getMemberRoles()
    {
        return this.memberRoles;
    }

    public List<ItemType> getMemberTypes()
    {
        return this.memberTypes;
    }

    /**
     * @return True if this bean has no members
     */
    public boolean isEmpty()
    {
        return this.memberIdentifiers.isEmpty();
    }

    /**
     * @return The number of members in this {@link RelationBean}
     */
    public int size()
    {
        return this.memberIdentifiers.size();
    }

    @Override
    public String toString()
    {
        return "RelationBean [memberIdentifiers=" + this.memberIdentifiers + ", memberRoles="
                + this.memberRoles + ", memberTypes=" + this.memberTypes + "]";
    }
}
