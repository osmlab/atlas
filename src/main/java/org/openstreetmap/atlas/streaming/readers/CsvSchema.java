package org.openstreetmap.atlas.streaming.readers;

import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;

/**
 * Schema for a Csv {@link Resource}. Each item in a {@link CsvLine} is represented by its
 * {@link StringConverter}. All the {@link StringConverter}s must be supplied in proper order.
 *
 * @author matthieun
 */
public class CsvSchema
{
    private final List<StringConverter<? extends Object>> converters;

    public CsvSchema(final Iterable<StringConverter<? extends Object>> converters)
    {
        this.converters = Iterables.asList(converters);
    }

    @SafeVarargs
    public CsvSchema(final StringConverter<? extends Object>... converters)
    {
        this.converters = Iterables.asList(Iterables.iterable(converters));
    }

    /**
     * Get an item
     *
     * @param line
     *            The line to extract the item from
     * @param index
     *            The index at which the item is in the line
     * @return The item
     */
    protected Object get(final CsvLine line, final int index)
    {
        verifyIndex(index);
        return this.converters.get(index).convert(line.getValue(index));
    }

    /**
     * The number of columns in this schema
     *
     * @return The number of columns in this schema
     */
    protected int size()
    {
        return this.converters.size();
    }

    private void verifyIndex(final int index)
    {
        if (index < 0 || index >= size())
        {
            throw new CoreException(
                    "Index " + index + " out of CsvSchema bounds of 0 -> " + size());
        }
    }
}
