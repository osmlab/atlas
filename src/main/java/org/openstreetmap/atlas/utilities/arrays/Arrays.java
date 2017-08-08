package org.openstreetmap.atlas.utilities.arrays;

import java.util.Collection;

/**
 * Utility method for arrays
 *
 * @author matthieun
 */
public final class Arrays
{
    public static int[] addNewItem(final int[] existing, final int newValue)
    {
        final int[] result = new int[existing.length + 1];
        for (int i = 0; i < existing.length; i++)
        {
            result[i] = existing[i];
        }
        result[result.length - 1] = newValue;
        return result;
    }

    public static long[] addNewItem(final long[] existing, final long newValue)
    {
        final long[] result = new long[existing.length + 1];
        for (int i = 0; i < existing.length; i++)
        {
            result[i] = existing[i];
        }
        result[result.length - 1] = newValue;
        return result;
    }

    public static long[] addNewItemAndResizeOnlyIfNecessary(final long[] existing,
            final long newValue, final int index)
    {
        long[] result = existing;
        if (index >= existing.length)
        {
            // Needs resizing. Double the size...
            result = java.util.Arrays.copyOf(existing, existing.length * 2);
        }
        result[index] = newValue;
        return result;
    }

    public static long[] toArray(final Collection<Long> list)
    {
        final long[] result = new long[list.size()];
        int index = 0;
        for (final Long value : list)
        {
            result[index++] = value;
        }
        return result;
    }

    public static long[] trimToSize(final long[] existing, final int size)
    {
        if (size <= 0)
        {
            return new long[0];
        }
        return java.util.Arrays.copyOfRange(existing, 0, size - 1);
    }

    private Arrays()
    {
    }
}
