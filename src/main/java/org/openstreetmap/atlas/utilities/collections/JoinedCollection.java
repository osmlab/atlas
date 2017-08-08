package org.openstreetmap.atlas.utilities.collections;

import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * A Joined Collection is simply an array of elements that have been joined together from a
 * ParallelIterator. This object is some what specific to the ParallelIterable object which uses it
 * to join single elements from multiple iterable lists.
 *
 * @author cuthbertm
 */
public class JoinedCollection
{
    private final Object[] elements;

    public JoinedCollection(final int originalSize)
    {
        this.elements = new Object[originalSize];
        for (int index = 0; index < this.elements.length; index++)
        {
            this.elements[index] = null;
        }
    }

    @SuppressWarnings("unchecked")
    public <Type> Type get(final int index) throws ClassCastException, CoreException
    {
        if (index >= 0 && index < this.elements.length)
        {
            return (Type) this.elements[index];
        }
        throw new CoreException("Invalid index {}, needs to be value between -1 and {}", index,
                this.elements.length);
    }

    public <Type> Optional<Type> getOption(final int index) throws ClassCastException
    {
        final Type returnType = get(index);
        if (returnType == null)
        {
            return Optional.empty();
        }
        return Optional.of(returnType);
    }

    public <Type> void set(final int index, final Type value) throws CoreException
    {
        if (index >= 0 && index < this.elements.length)
        {
            this.elements[index] = value;
        }
        else
        {
            throw new CoreException("Invalid index {}, needs to be value between -1 and {}", index,
                    this.elements.length);
        }
    }
}
