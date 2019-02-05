package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lcram
 */
public final class Lists
{
    @SafeVarargs
    public static <T> List<T> arrayList(final T... elements)
    {
        final List<T> result = new ArrayList<>();
        for (final T element : elements)
        {
            result.add(element);
        }
        return result;
    }

    private Lists()
    {

    }
}
