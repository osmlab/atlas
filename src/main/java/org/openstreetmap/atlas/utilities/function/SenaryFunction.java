package org.openstreetmap.atlas.utilities.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that accepts six arguments and produces a result. This is the six-arity
 * specialization of {@link Function}.
 *
 * @param <P>
 *            the type of the first argument to the function
 * @param <Q>
 *            the type of the second argument to the function
 * @param <S>
 *            the type of the third argument to the function
 * @param <T>
 *            the type of the fourth argument to the function
 * @param <U>
 *            the type of the fifth argument to the function
 * @param <V>
 *            the type of the sixth argument to the function
 * @param <R>
 *            the type of the result of the function
 * @author lcram
 */
@FunctionalInterface
public interface SenaryFunction<P, Q, S, T, U, V, R>
{
    /**
     * Returns a composed function that first applies this function to its input, and then applies
     * the {@code after} function to the result. If evaluation of either function throws an
     * exception, it is relayed to the caller of the composed function.
     *
     * @param <W>
     *            the type of output of the {@code after} function, and of the composed function
     * @param after
     *            the function to apply after this function is applied
     * @return a composed function that first applies this function and then applies the
     *         {@code after} function
     * @throws NullPointerException
     *             if after is null
     */
    default <W> SenaryFunction<P, Q, S, T, U, V, W> andThen(
            final Function<? super R, ? extends W> after)
    {
        Objects.requireNonNull(after);
        return (final P p, final Q q, final S s, final T t, final U u, final V v) -> after
                .apply(apply(p, q, s, t, u, v));
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param p
     *            the first function argument
     * @param q
     *            the second function argument
     * @param s
     *            the third function argument
     * @param t
     *            the fourth function argument
     * @param u
     *            the fifth function argument
     * @param v
     *            the sixth function argument
     * @return the function result
     */
    R apply(P p, Q q, S s, T t, U u, V v);
}
