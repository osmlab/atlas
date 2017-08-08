package org.openstreetmap.atlas.utilities.arrays;

import java.io.Serializable;
import java.util.stream.IntStream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * Wrapper for a primitive array corresponding to a given type.
 *
 * @author matthieun
 * @param <T>
 *            The non-primitive type of the primitive type this array is wrapping.
 */
public abstract class PrimitiveArray<T> implements Serializable
{
    private static final long serialVersionUID = 7815464773512459667L;

    private final int size;

    /**
     * @param size
     *            The size of the primitive array
     */
    public PrimitiveArray(final int size)
    {
        this.size = size;
    }

    /**
     * @param index
     *            The index wanted
     * @return The item at the index
     */
    public abstract T get(int index);

    /**
     * Build a new empty primitive array of the specified size.
     *
     * @param size
     *            the specified size
     * @return a new empty primitive array of the specified size
     */
    public abstract PrimitiveArray<T> getNewArray(int size);

    /**
     * Set an item at the specified index in the array.
     *
     * @param index
     *            The specified index
     * @param item
     *            The item to set at the specified index
     */
    public abstract void set(int index, T item);

    /**
     * @return The size of this primitive array
     */
    public int size()
    {
        return this.size;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        final StringList list = new StringList();
        for (int i = 0; i < size(); i++)
        {
            list.add(this.get(i).toString());
        }
        builder.append(list.join(", "));
        builder.append("]");
        return builder.toString();
    }

    /**
     * Trim this array
     *
     * @param size
     *            The size to trim to
     * @return The trimmed array with data originating from this array up to the size
     */
    public PrimitiveArray<T> trimmed(final int size)
    {
        if (size >= this.size)
        {
            return this;
        }
        final PrimitiveArray<T> result = getNewArray(size);
        // Equivalent of for (int i = 0; i < size; i++)
        IntStream.range(0, size).forEach(i -> result.set(i, get(i)));
        return result;
    }

    /**
     * Copy this primitive array into another primitive array of bigger size.
     *
     * @param newSize
     *            The bigger size
     * @return The new bigger array, copied from this one.
     */
    public PrimitiveArray<T> withNewSize(final int newSize)
    {
        if (newSize < size())
        {
            throw new CoreException("Cannot copy into a smaller array. This is " + size()
                    + " and the new size asked is " + newSize);
        }
        final PrimitiveArray<T> young = getNewArray(newSize);
        for (int i = 0; i < size(); i++)
        {
            young.set(i, get(i));
        }
        return young;
    }
}
