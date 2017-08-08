package org.openstreetmap.atlas.utilities.tuples;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Class that allows results to be one of two types. You can use the function like so: Either
 * &lt;String, Int&gt;.left("Test").apply( left -&gt; {System.out.println("String:" + left);}, right
 * -&gt; {System.out.println("Integer:" + right);} );
 *
 * @param <L>
 *            The type for the left value
 * @param <R>
 *            The type for the right value
 * @author cuthbertm
 */
public final class Either<L, R> implements Serializable
{
    private static final long serialVersionUID = 158343315469036806L;

    private final L left;
    private final R right;

    public static <L, R> Either<L, R> left(final L value)
    {
        return new Either<>(value, null);
    }

    public static <L, R> Either<L, R> right(final R value)
    {
        return new Either<>(null, value);
    }

    private Either(final L left, final R right)
    {
        this.left = left;
        this.right = right;
    }

    /**
     * Apply function that allows you to execute against the left or right values
     *
     * @param leftFunction
     *            The function to execute against the left value
     * @param rightFunction
     *            The function to execute against the right value
     */
    public void apply(final Consumer<? super L> leftFunction,
            final Consumer<? super R> rightFunction)
    {
        if (this.left != null)
        {
            leftFunction.accept(this.left);
        }
        else if (this.right != null)
        {
            rightFunction.accept(this.right);
        }
    }

    public L getLeft()
    {
        return this.left;
    }

    public R getRight()
    {
        return this.right;
    }

    public boolean isLeft()
    {
        return this.left != null;
    }

    public boolean isRight()
    {
        return this.right != null;
    }
}
