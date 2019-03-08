package org.openstreetmap.atlas.utilities.function;

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents an operation upon four operands of the same type, producing a result of the same type
 * as the operands. This is a specialization of {@link QuadFunction} for the case where the operands
 * and the result are all of the same type.
 *
 * @param <T>
 *            the type of the operands and result of the operator
 * @author lcram
 */
@FunctionalInterface
public interface QuaternaryOperator<T> extends QuadFunction<T, T, T, T, T>
{
    /**
     * Returns a {@link QuaternaryOperator} which returns the greatest of four elements according to
     * the specified {@code Comparator}.
     *
     * @param <T>
     *            the type of the input arguments of the comparator
     * @param comparator
     *            a {@code Comparator} for comparing the four values
     * @return a {@code QuaternaryOperator} which returns the greatest of its operands, according to
     *         the supplied {@code Comparator}
     * @throws NullPointerException
     *             if the argument is null
     */
    public static <T> QuaternaryOperator<T> maxBy(final Comparator<? super T> comparator)
    {
        Objects.requireNonNull(comparator);
        return (a, b, c, d) ->
        {
            T largerOfAandB;
            T largerOfCandD;
            if (comparator.compare(a, b) >= 0)
            {
                largerOfAandB = a;
            }
            else
            {
                largerOfAandB = b;
            }
            if (comparator.compare(c, d) >= 0)
            {
                largerOfCandD = c;
            }
            else
            {
                largerOfCandD = d;
            }
            return comparator.compare(largerOfAandB, largerOfCandD) >= 0 ? largerOfAandB
                    : largerOfCandD;
        };
    }

    /**
     * Returns a {@link QuaternaryOperator} which returns the smallest of four elements according to
     * the specified {@code Comparator}.
     *
     * @param <T>
     *            the type of the input arguments of the comparator
     * @param comparator
     *            a {@code Comparator} for comparing the two values
     * @return a {@code QuaternaryOperator} which returns the smallest of its operands, according to
     *         the supplied {@code Comparator}
     * @throws NullPointerException
     *             if the argument is null
     */
    public static <T> QuaternaryOperator<T> minBy(final Comparator<? super T> comparator)
    {
        Objects.requireNonNull(comparator);
        return (a, b, c, d) ->
        {
            T smallerOfAandB;
            T smallerOfCandD;
            if (comparator.compare(a, b) <= 0)
            {
                smallerOfAandB = a;
            }
            else
            {
                smallerOfAandB = b;
            }
            if (comparator.compare(c, d) <= 0)
            {
                smallerOfCandD = c;
            }
            else
            {
                smallerOfCandD = d;
            }
            return comparator.compare(smallerOfAandB, smallerOfCandD) <= 0 ? smallerOfAandB
                    : smallerOfCandD;
        };
    }
}
