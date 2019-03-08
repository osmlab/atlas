package org.openstreetmap.atlas.utilities.collections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.openstreetmap.atlas.exception.CoreException;

import com.google.common.reflect.TypeToken;

/**
 * A way to build an EnumSet from a stream of strings, where the case of the strings don't matter
 * and ALL will automatically add all declared enum constants to the EnumSet. Since we're using
 * generic types, we have some issues where the parameterized type information is only contained in
 * concrete subclasses of this collector. That means for each Enum we want to collect, we'll need an
 * empty subclass so we can recover the type information. For a testcase showing how to make it
 * work, check out EnumSetCollectionTestCase
 *
 * @author cstaylor
 * @param <T>
 *            The Java enum to store in an EnumSet that can be queried for values
 */
public abstract class EnumSetCollector<T extends Enum<T>>
        implements Collector<String, Set<String>, EnumSet<T>>
{
    @SuppressWarnings("rawtypes")
    private Class enumClass;

    private Method valueOfMethod;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    /**
     * We need to know the enum's class, but we can't get at it directly because we're a generic
     * interface. Following directions listed out in this article:
     * http://stackoverflow.com/questions/3609799/how-to-get-type-parameter-values-using-java-
     * reflection
     */
    protected EnumSetCollector()
    {
        final TypeToken<?> resolver = TypeToken.of(getClass());

        for (final TypeVariable<Class<EnumSetCollector>> typeVariable : EnumSetCollector.class
                .getTypeParameters())
        {
            final TypeToken<?> currentToken = resolver.resolveType(typeVariable);
            this.enumClass = currentToken.getRawType();
            try
            {
                this.valueOfMethod = this.enumClass.getMethod("valueOf", String.class);
            }
            catch (final NoSuchMethodException oops)
            {
                throw new CoreException(
                        String.format("%s isn't a Java enum", this.enumClass.getName()));
            }
        }
    }

    @Override
    public BiConsumer<Set<String>, String> accumulator()
    {
        return (enumset, value) -> enumset.add(value.toUpperCase());
    }

    @Override
    public Set<java.util.stream.Collector.Characteristics> characteristics()
    {
        return EnumSet.of(Characteristics.UNORDERED);
    }

    @Override
    public BinaryOperator<Set<String>> combiner()
    {
        return (left, right) ->
        {
            left.addAll(right);
            return left;
        };
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public Function<Set<String>, EnumSet<T>> finisher()
    {
        return working ->
        {
            if (working.contains("ALL"))
            {
                return EnumSet.allOf(this.enumClass);
            }
            try
            {
                final EnumSet<T> returnValue = EnumSet.noneOf(this.enumClass);
                for (final String constant : working)
                {
                    returnValue.add((T) this.valueOfMethod.invoke(null, constant));
                }
                return returnValue;
            }
            catch (final InvocationTargetException | IllegalAccessException oops)
            {
                throw new CoreException("Can't find enum value for: {}", working);
            }
        };
    }

    @Override
    public Supplier<Set<String>> supplier()
    {
        return this.enumClass == null ? null : () -> new HashSet<>();
    }
}
