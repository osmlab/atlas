package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

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
    private final String description;

    public GenericElementChangeDescriptor(final ChangeDescriptorType changeType,
            final T beforeElement, final T afterElement, final String description)
    {
        this.changeType = changeType;
        this.beforeElement = beforeElement;
        this.afterElement = afterElement;
        this.description = description;
    }

    public GenericElementChangeDescriptor(final ChangeDescriptorType changeType,
            final T afterElement, final String description)
    {
        this.changeType = changeType;
        this.beforeElement = null;
        this.afterElement = afterElement;
        this.description = description;
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

    public String getDescription()
    {
        return this.description;
    }

    @Override
    public String toString()
    {
        if (this.changeType == ChangeDescriptorType.UPDATE)
        {
            return this.description + "(" + this.getChangeDescriptorType() + ", "
                    + this.getBeforeElement() + " => " + this.getAfterElement() + ")";
        }
        return this.description + "(" + this.getChangeDescriptorType() + ", "
                + this.getAfterElement() + ")";
    }
}
