package org.openstreetmap.atlas.utilities.conversion;

import java.util.function.Function;

/**
 * Convert from A type to B type
 *
 * @param <A>
 *            The source type
 * @param <B>
 *            The target type
 * @author tony
 */
public interface Converter<A, B> extends Function<A, B>
{
    @Override
    default B apply(final A other)
    {
        return convert(other);
    }

    B convert(A object);
}
