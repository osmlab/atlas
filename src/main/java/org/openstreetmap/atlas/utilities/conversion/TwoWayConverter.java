package org.openstreetmap.atlas.utilities.conversion;

/**
 * Not only can convert from A type to B type, this interface supports backward convert from B to A
 * as well
 *
 * @param <A>
 *            The source type
 * @param <B>
 *            The target type
 * @author tony
 */
public interface TwoWayConverter<A, B> extends Converter<A, B>
{
    A backwardConvert(B object);

    default Converter<B, A> revert()
    {
        return object -> backwardConvert(object);
    }
}
