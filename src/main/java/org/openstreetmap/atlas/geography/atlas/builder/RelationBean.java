package org.openstreetmap.atlas.geography.atlas.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.collections.Iterables;

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

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof RelationBean)
        {
            final RelationBean that = (RelationBean) other;
            return Iterables.equals(this.getMemberIdentifiers(), that.getMemberIdentifiers())
                    && Iterables.equals(this.getMemberRoles(), that.getMemberRoles())
                    && Iterables.equals(this.getMemberTypes(), that.getMemberTypes());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getMemberIdentifiers(), this.getMemberRoles(),
                this.getMemberTypes());
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
