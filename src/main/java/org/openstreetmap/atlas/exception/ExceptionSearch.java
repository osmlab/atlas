package org.openstreetmap.atlas.exception;

import java.util.Optional;

/**
 * Utility class for searching an exception cause chain for a particular exception type
 *
 * @author cstaylor
 * @param <T>
 *            the type of exception we're searching for
 */
public final class ExceptionSearch<T extends Throwable>
{
    private final Class<T> target;

    public static <T extends Throwable> ExceptionSearch<T> find(final Class<T> target)
    {
        if (target == null)
        {
            throw new IllegalArgumentException("target is null");
        }
        return new ExceptionSearch<>(target);
    }

    private ExceptionSearch(final Class<T> target)
    {
        this.target = target;
    }

    public Optional<T> within(final Throwable source)
    {
        if (this.target == null)
        {
            throw new IllegalStateException("target is null");
        }
        return Optional.ofNullable(within0(source));
    }

    private T within0(final Throwable source)
    {
        if (source == null)
        {
            return null;
        }
        if (this.target.isInstance(source))
        {
            return this.target.cast(source);
        }
        return within0(source.getCause());
    }
}
