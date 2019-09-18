package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A {@link ChangeDescriptor} for relation member changes.
 * 
 * @author lcram
 */
public class RelationMemberChangeDescriptor implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final long identifier;
    private final ItemType type;
    private final String role;

    public RelationMemberChangeDescriptor(final ChangeDescriptorType changeType,
            final long identifier, final ItemType type, final String role)
    {
        this.changeType = changeType;
        this.identifier = identifier;
        this.type = type;
        this.role = role;
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }

    public long getIdentifier()
    {
        return this.identifier;
    }

    public ItemType getItemType()
    {
        return this.type;
    }

    @Override
    public ChangeDescriptorName getName()
    {
        return ChangeDescriptorName.RELATION_MEMBER;
    }

    public String getRole()
    {
        return this.role;
    }

    @Override
    public JsonElement toJsonElement()
    {
        final JsonObject descriptor = (JsonObject) ChangeDescriptor.super.toJsonElement();
        descriptor.addProperty("itemType", this.type.toString());
        descriptor.addProperty("id", this.identifier);
        descriptor.addProperty("role", this.role);
        return descriptor;
    }

    @Override
    public String toString()
    {
        return getName().toString() + "(" + this.changeType + ", " + this.type + ", "
                + this.identifier + ", " + this.role + ")";
    }
}
