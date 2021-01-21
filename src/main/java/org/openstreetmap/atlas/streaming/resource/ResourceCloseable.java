package org.openstreetmap.atlas.streaming.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Use when the returned resource has some items that cannot be closed prior to finishing with this
 * resource.
 *
 * @author Taylor Smock
 */
public interface ResourceCloseable extends Resource, AutoCloseable
{
    @Override
    default void close() throws Exception
    {
        final Collection<Exception> exceptions = new ArrayList<>();
        for (final AutoCloseable closeable : Stream.of(getDependencies()).filter(Objects::nonNull)
                .collect(Collectors.toList()))
        {
            try
            {
                closeable.close();
            }
            catch (final Exception exception)
            {
                exceptions.add(exception);
            }
        }
        if (!exceptions.isEmpty())
        {
            throw new CoreException(
                    "{} exceptions thrown while closing {}, only showing the first thrown exception",
                    exceptions.size(), this.getName(), exceptions.iterator().next());
        }
    }

    /**
     * Get the {@link AutoCloseable} resources that this resource depends upon.
     *
     * @return An array of {@link AutoCloseable} dependencies
     */
    default AutoCloseable[] getDependencies()
    {
        return new AutoCloseable[0];
    }
}
