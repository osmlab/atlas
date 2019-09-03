package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * @author lcram
 * @param <T>
 *            the type of the set elements
 */
public class GenericSetChangeDescriptor<T> implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final T element;
    private final String description;

    public GenericSetChangeDescriptor(final ChangeDescriptorType changeType, final T element,
            final String description)
    {
        this.changeType = changeType;
        this.element = element;
        this.description = description;
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

    public T getElement()
    {
        return this.element;
    }

    @Override
    public String toString()
    {
        return this.description + "(" + this.getChangeDescriptorType() + ", " + this.getElement()
                + ")";
    }
}
