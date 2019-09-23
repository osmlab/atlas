package org.openstreetmap.atlas.utilities.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * {@link List} of {@link String}s with convenience methods
 *
 * @author matthieun
 */
public class StringList implements Iterable<String>, Serializable
{
    private static final long serialVersionUID = -7923796535827613632L;
    private final List<String> list;

    public static StringList split(final String item, final String separator)
    {
        return split(item, separator, 0);
    }

    /**
     * Split the string item up to limit pieces. Because the separator is wrapped in regex quotes
     * (\Q and \E), this method does not support regex.
     *
     * @param item
     *            The string to split
     * @param separator
     *            A string used to separate the input item
     * @param limit
     *            The limit parameter controls the number of times the pattern is applied and
     *            therefore affects the length of the resulting array. If the limit n is greater
     *            than zero then the pattern will be applied at most n - 1 times, the array's length
     *            will be no greater than n, and the array's last entry will contain all input
     *            beyond the last matched delimiter.
     * @return A StringList object
     */
    public static StringList split(final String item, final String separator, final int limit)
    {
        return new StringList(Iterables.asList(item.split(Pattern.quote(separator), limit)));
    }

    public static StringList splitByRegex(final String item, final String separator)
    {
        return splitByRegex(item, separator, 0);
    }

    /**
     * Split the string item up to limit pieces, supports regex.
     *
     * @param item
     *            The string to split
     * @param regex
     *            A string used to separate the input item
     * @param limit
     *            The limit parameter controls the number of times the pattern is applied and
     *            therefore affects the length of the resulting array. If the limit n is greater
     *            than zero then the pattern will be applied at most n - 1 times, the array's length
     *            will be no greater than n, and the array's last entry will contain all input
     *            beyond the last matched delimiter.
     * @return A StringList object
     */
    public static StringList splitByRegex(final String item, final String regex, final int limit)
    {
        return new StringList(Iterables.asList(item.split(regex, limit)));
    }

    public StringList()
    {
        this.list = new ArrayList<>();
    }

    public StringList(final Iterable<String> list)
    {
        this.list = Iterables.asList(list);
    }

    public StringList(final List<String> list)
    {
        this.list = list;
    }

    public StringList(final String... array)
    {
        this.list = Iterables.asList(array);
    }

    public void add(final Object string)
    {
        this.list.add(String.valueOf(string));
    }

    public void add(final String string)
    {
        this.list.add(string);
    }

    public void addAll(final Iterable<String> split)
    {
        split.forEach(this::add);
    }

    public boolean contains(final String item)
    {
        for (final String candidate : this)
        {
            if (candidate.equals(item))
            {
                return true;
            }
        }
        return false;
    }

    public Optional<String> first()
    {
        if (this.size() <= 0)
        {
            return Optional.empty();
        }
        return Optional.of(this.get(0));
    }

    public synchronized String get(final int index)
    {
        if (index >= size())
        {
            throw new CoreException("Cannot get item out of bounds: {} in size = {}", index,
                    size());
        }
        return this.list.get(index);
    }

    public boolean isEmpty()
    {
        return this.size() == 0;
    }

    @Override
    public Iterator<String> iterator()
    {
        return this.list.iterator();
    }

    public String join(final String separator)
    {
        final StringBuilder result = new StringBuilder();
        int index = 0;
        for (final String string : this)
        {
            result.append(string);
            index++;
            if (index < size())
            {
                result.append(separator);
            }
        }
        return result.toString();
    }

    public Optional<String> last()
    {
        if (this.size() <= 0)
        {
            return Optional.empty();
        }
        return Optional.of(this.get(size() - 1));
    }

    public void remove(final int index)
    {
        this.list.remove(index);
    }

    public int size()
    {
        return this.list.size();
    }

    /**
     * @param item
     *            The item to test for
     * @return True if the item starts with some element in this list.
     */
    public boolean startsWithContains(final String item)
    {
        for (final String candidate : this)
        {
            if (item.startsWith(candidate))
            {
                return true;
            }
        }
        return false;
    }

    public Stream<String> stream()
    {
        return this.list.stream();
    }

    @Override
    public String toString()
    {
        return this.list.toString();
    }
}
