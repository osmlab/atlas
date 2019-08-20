package org.openstreetmap.atlas.streaming.readers;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.StringList;

import au.com.bytecode.opencsv.CSVParser;

/**
 * A Csv line
 *
 * @author matthieun
 */
public final class CsvLine implements Iterable<Object>
{
    private final CsvSchema schema;
    private final String[] items;

    public static CsvLine build(final CsvSchema schema, final String line)
    {
        final String[] items;
        try
        {
            items = new CSVParser().parseLine(line);
        }
        catch (final IOException e)
        {
            throw new CoreException("Could not parse line " + line, e);
        }
        if (items != null && schema != null)
        {
            if (items.length == schema.size())
            {
                return new CsvLine(schema, items);
            }
            throw new CoreException("Line -- " + line + " -- has " + items.length
                    + " arguments instead of " + schema.size() + " expected in the schema.");
        }
        throw new CoreException("line or schema was null.");
    }

    /**
     * Force use of factory method
     */
    private CsvLine(final CsvSchema schema, final String[] line)
    {
        this.schema = schema;
        this.items = line;
    }

    /**
     * Get an item in this line
     *
     * @param index
     *            The index at which the item is in the line
     * @return The item.
     */
    public Object get(final int index)
    {
        verifyIndex(index);
        return this.schema.get(this, index);
    }

    @Override
    public Iterator<Object> iterator()
    {
        return new Iterator<Object>()
        {
            private int index = 0;

            @Override
            public boolean hasNext()
            {
                return this.index < CsvLine.this.items.length;
            }

            @Override
            public Object next()
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }
                return get(this.index++);
            }
        };
    }

    @Override
    public String toString()
    {
        final StringList list = new StringList(() -> new Iterator<String>()
        {
            private final Iterator<Object> objects = CsvLine.this.iterator();

            @Override
            public boolean hasNext()
            {
                return this.objects.hasNext();
            }

            @Override
            public String next()
            {
                final Object next = this.objects.next();
                return "\"" + next.toString() + "\"";
            }
        });
        return list.join(",");
    }

    protected String getValue(final int index)
    {
        verifyIndex(index);
        return this.items[index];
    }

    private void verifyIndex(final int index)
    {
        if (index < 0 || index >= this.items.length)
        {
            throw new CoreException(
                    "Item index " + index + " is out of range: 0 -> " + this.items.length);
        }
    }
}
