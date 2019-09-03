package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * @author lcram
 * @param <T>
 *            the type of the set elements
 */
public abstract class GenericSetChangeDescriptor<T> implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final T element;

    public GenericSetChangeDescriptor(final ChangeDescriptorType changeType, final T element)
    {
        this.changeType = changeType;
        this.element = element;
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }

    public T getElement()
    {
        return this.element;
    }
}
