package org.openstreetmap.atlas.utilities.tuples;

import java.io.Serializable;

/**
 * A Generic Tuple implementation
 *
 * @author mgostintsev
 * @param <A>
 *            First object's type
 * @param <B>
 *            Second object's type
 */
public class Tuple<A, B> implements Serializable
{
    private static final long serialVersionUID = 7745080808877729817L;
    private final A first;
    private final B second;

    @SuppressWarnings("unchecked")
    public static <A, B> Tuple<A, B> cast(final Tuple<?, ?> tuple, final Class<A> aClass,
            final Class<B> bClass)
    {
        if (tuple.isInstanceOf(aClass, bClass))
        {
            return (Tuple<A, B>) tuple;
        }
        throw new ClassCastException("Unable to cast, class mismatch");
    }

    public static <A, B> Tuple<A, B> createTuple(final A first, final B second)
    {
        return new Tuple<>(first, second);
    }

    public Tuple(final A first, final B second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || getClass() != other.getClass())
        {
            return false;
        }

        final Tuple<?, ?> tuple = (Tuple<?, ?>) other;
        if (this.first == null)
        {
            if (tuple.first != null)
            {
                return false;
            }
        }
        else if (!this.first.equals(tuple.first))
        {
            return false;
        }
        if (this.second == null)
        {
            if (tuple.second != null)
            {
                return false;
            }
        }
        else if (!this.second.equals(tuple.second))
        {
            return false;
        }
        return true;
    }

    public A getFirst()
    {
        return this.first;
    }

    public B getSecond()
    {
        return this.second;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.first == null ? 0 : this.first.hashCode());
        result = prime * result + (this.second == null ? 0 : this.second.hashCode());
        return result;
    }

    public boolean isInstanceOf(final Class<?> classA, final Class<?> classB)
    {
        return classA.isInstance(this.first) && classB.isInstance(this.second);
    }

    @Override
    public String toString()
    {
        return String.format("(%s,%s)", this.first, this.second);
    }

}
