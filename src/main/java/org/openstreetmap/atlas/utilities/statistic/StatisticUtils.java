package org.openstreetmap.atlas.utilities.statistic;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.apache.commons.math3.stat.StatUtils;
import org.openstreetmap.atlas.utilities.scalars.Ratio;

/**
 * Some useful tools can consume a Number / Object collection, then return univariate statistic.
 * Most implementation is based on Java 8 Statistic objects and Stream function, except for
 * percentile method which is based on apache StatUtils class
 *
 * @author tony
 */
public final class StatisticUtils
{
    /**
     * @param collection
     *            The collection of items
     * @param ratio
     *            The percentage
     * @return Returns an estimate of the <code>p</code>th percentile of the values in collection
     */
    public static double percentile(final Collection<Double> collection, final Ratio ratio)
    {
        final double[] array = toDoubleArray(collection);
        return StatUtils.percentile(array, ratio.asPercentage());
    }

    /**
     * Get statistic by using a customized binary operator. An example of sum of square of a list of
     * Double would be:
     * <p>
     * <code> {@literal Optional<Double> sumOfSquare = StatisticUtils.summarizing(list, (a,b) -> a*a + b*b)};
     * </code>
     * </p>
     *
     * @param collection
     *            The collection of items
     * @param accumulator
     *            The binary operator that accumulates values
     * @param <T>
     *            The type of the statistic
     * @return the result value of corresponding accumulator operation
     */
    public static <T> Optional<T> summarizing(final Collection<T> collection,
            final BinaryOperator<T> accumulator)
    {
        return collection.stream().reduce(accumulator);
    }

    /**
     * @param collection
     *            The collection of items
     * @return A state object for collecting statistics such as count, min, max, sum, and average.
     */
    public static DoubleSummaryStatistics summarizingDouble(final Collection<Double> collection)
    {
        return collection.stream().mapToDouble(value -> value).summaryStatistics();
    }

    /**
     * Get summary statistic from an object collection. An example to get summary of bank account
     * balance from a list of Person would be
     * <p>
     * <code> {@literal DoubleSummaryStatistics stat =
     * StatisticUtils.summarizingDouble(list, x->x.getBankAccountBalance());} </code>
     * </p>
     *
     * @param collection
     *            The collection of items
     * @param function
     *            You need to specify the function to get double value for each object T
     * @param <T>
     *            The type of the statistic
     * @return A state object for collecting statistics such as count, min, max, sum, and average.
     */
    public static <T> DoubleSummaryStatistics summarizingDouble(final Collection<T> collection,
            final ToDoubleFunction<? super T> function)
    {
        return collection.stream().mapToDouble(function).summaryStatistics();
    }

    /**
     * @param collection
     *            The collection of items
     * @return A state object for collecting statistics such as count, min, max, sum, and average.
     */
    public static IntSummaryStatistics summarizingInt(final Collection<Integer> collection)
    {
        return collection.stream().mapToInt(value -> value).summaryStatistics();
    }

    /**
     * Get summary statistic from an object collection. An example to get summary of age from a
     * Person list would be
     * <p>
     * <code> {@literal IntSummaryStatistics stat = StatisticUtils.summarizingInt(list, x->x.getAge());} </code>
     * </p>
     *
     * @param collection
     *            The collection of items
     * @param function
     *            You need to specify the function to get int value for each object T
     * @param <T>
     *            The type of the statistic
     * @return A state object for collecting statistics such as count, min, max, sum, and average.
     */
    public static <T> IntSummaryStatistics summarizingInt(final Collection<T> collection,
            final ToIntFunction<? super T> function)
    {
        return collection.stream().mapToInt(function).summaryStatistics();
    }

    /**
     * @param collection
     *            The collection of items
     * @return A state object for collecting statistics such as count, min, max, sum, and average.
     */
    public static LongSummaryStatistics summarizingLong(final Collection<Long> collection)
    {
        return collection.stream().mapToLong(value -> value).summaryStatistics();
    }

    /**
     * Get summary statistic from an object collection. An example to get summary of age from a
     * Person list would be (suppose a person can live longer than 2^31 - 1)
     * <p>
     * <code> {@literal LongSummaryStatistics stat = StatisticUtils.summarizingLong(list, x->x.getAge());} </code>
     * </p>
     *
     * @param collection
     *            The collection of items
     * @param function
     *            You need to specify the function to get long value for each object T
     * @param <T>
     *            The type of the statistic
     * @return A state object for collecting statistics such as count, min, max, sum, and average.
     */
    public static <T> LongSummaryStatistics summarizingLong(final Collection<T> collection,
            final ToLongFunction<? super T> function)
    {
        return collection.stream().mapToLong(function).summaryStatistics();
    }

    private static double[] toDoubleArray(final Collection<Double> collection)
    {
        final double[] array = new double[collection.size()];
        int index = 0;
        for (final Double element : collection)
        {
            array[index++] = element;
        }
        return array;
    }

    private StatisticUtils()
    {
    }

}
