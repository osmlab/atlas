package org.openstreetmap.atlas.streaming.resource;

import java.io.InputStream;
import java.util.function.Predicate;

import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * A resource that has a built-in filter for the lines method.
 *
 * @author matthieun
 */
public class LineFilteredResource implements Resource
{
    private final Resource source;
    private final Predicate<String> lineFilter;

    public LineFilteredResource(final Resource source, final Predicate<String> lineFilter)
    {
        this.source = source;
        this.lineFilter = lineFilter;
    }

    @Override
    public long length()
    {
        return this.source.length();
    }

    @Override
    public Iterable<String> lines()
    {
        return Iterables.stream(this.source.lines()).filter(this.lineFilter).collect();
    }

    @Override
    public InputStream read()
    {
        return this.source.read();
    }

}
