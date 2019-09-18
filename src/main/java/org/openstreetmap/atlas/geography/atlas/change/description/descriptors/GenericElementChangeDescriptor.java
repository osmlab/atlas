package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A {@link ChangeDescriptor} for any kind of generic element. For e.g. a {@link Long} in cases of
 * startNodeIdentifier or inEdgeIdentifier.
 * 
 * @author lcram
 * @param <T>
 *            the type of the element
 */
public class GenericElementChangeDescriptor<T> implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final T beforeElement;
    private final T afterElement;
    private final ChangeDescriptorName name;

    public GenericElementChangeDescriptor(final ChangeDescriptorType changeType,
            final T beforeElement, final T afterElement, final ChangeDescriptorName name)
    {
        this.changeType = changeType;
        this.beforeElement = beforeElement;
        this.afterElement = afterElement;
        this.name = name;
    }

    public GenericElementChangeDescriptor(final ChangeDescriptorType changeType,
            final T afterElement, final ChangeDescriptorName name)
    {
        this.changeType = changeType;
        this.beforeElement = null;
        this.afterElement = afterElement;
        this.name = name;
    }

    public T getAfterElement()
    {
        return this.afterElement;
    }

    public T getBeforeElement()
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
            descriptor.addProperty("beforeView", this.beforeElement.toString());
        }
        if (this.afterElement != null)
        {
            descriptor.addProperty("afterView", this.afterElement.toString());
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
