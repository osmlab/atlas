package org.openstreetmap.atlas.geography.atlas.changeset;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * A simple implementation of {@link ChangeItemMember} interface.
 *
 * @author Yiqing Jin
 * @deprecated - see new API under org.openstreetmap.atlas.geography.atlas.change package.
 */
@Deprecated
public class SimpleChangeItemMember implements ChangeItemMember, Serializable // NOSONAR
{
    private static final long serialVersionUID = 3261727439156010800L;

    private long identifier;
    private ItemType type;
    private String role;

    public SimpleChangeItemMember(final long identifier, final String role, final ItemType type)
    {
        this.identifier = identifier;
        this.type = type;
        this.role = role;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (!(other instanceof SimpleChangeItemMember))
        {
            return false;
        }
        final SimpleChangeItemMember that = (SimpleChangeItemMember) other;
        return this.getIdentifier() == that.getIdentifier() && this.getType() == that.getType()
                && StringUtils.equals(this.getRole(), that.getRole());
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public String getRole()
    {
        return this.role;
    }

    @Override
    public ItemType getType()
    {
        return this.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getIdentifier(), this.getRole(), this.getType());
    }

    public void setIdentifier(final long identifier)
    {
        this.identifier = identifier;
    }

    public void setRole(final String role)
    {
        this.role = role;
    }

    public void setType(final ItemType type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[Identifier: ");
        builder.append(this.getIdentifier());
        builder.append(", Role: ");
        builder.append(this.getRole());
        builder.append(", Type: ");
        builder.append(this.getType());
        builder.append("]");
        return builder.toString();
    }

}
