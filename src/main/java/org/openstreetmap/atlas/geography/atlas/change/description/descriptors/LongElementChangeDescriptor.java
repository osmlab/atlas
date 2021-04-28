package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A {@link ChangeDescriptor} for an element of type {@link Long}. E.g. cases include
 * startNodeIdentifiers, inEdgeIdentifiers, parentRelations, etc.
 * 
 * @author lcram
 */
public class LongElementChangeDescriptor implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final Long beforeElement;
    private final Long afterElement;
    private final ChangeDescriptorName name;

    public LongElementChangeDescriptor(final ChangeDescriptorType changeType,
            final Long beforeElement, final Long afterElement, final ChangeDescriptorName name)
    {
        this.changeType = changeType;
        this.beforeElement = beforeElement;
        this.afterElement = afterElement;
        this.name = name;
    }

    public LongElementChangeDescriptor(final ChangeDescriptorType changeType,
            final Long afterElement, final ChangeDescriptorName name)
    {
        this.changeType = changeType;
        this.beforeElement = null;
        this.afterElement = afterElement;
        this.name = name;
    }

    public Long getAfterElement()
    {
        return this.afterElement;
    }

    public Long getBeforeElement()
    {
        return this.beforeElement;
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }

    @Override
    public ChangeDescriptorName getName()
    {
        return this.name;
    }

    @Override
    public JsonElement toJsonElement()
    {
        final JsonObject descriptor = (JsonObject) ChangeDescriptor.super.toJsonElement();
        if (this.beforeElement != null)
        {
            descriptor.addProperty("beforeElement", this.beforeElement.toString());
        }
        if (this.afterElement != null)
        {
            descriptor.addProperty("afterElement", this.afterElement.toString());
        }
        return descriptor;
    }

    @Override
    public String toString()
    {
        if (this.changeType == ChangeDescriptorType.UPDATE)
        {
            return this.name + "(" + this.getChangeDescriptorType() + ", " + this.getBeforeElement()
                    + " => " + this.getAfterElement() + ")";
        }
        if (this.changeType == ChangeDescriptorType.REMOVE)
        {
            return this.name + "(" + this.getChangeDescriptorType() + ", " + this.getBeforeElement()
                    + ")";
        }
        return this.name + "(" + this.getChangeDescriptorType() + ", " + this.getAfterElement()
                + ")";
    }
}
