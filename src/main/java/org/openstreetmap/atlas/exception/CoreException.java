package org.openstreetmap.atlas.exception;

import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.helpers.MessageFormatter;

/**
 * This is a universal Exception in Core. Can use substitutions in messages according to very simple
 * substitution rules. Substitutions can be made 1, 2 or more arguments.
 * <p>
 * For example,
 * <p>
 * <code>
 * new CoreException(&quot;Hi {}, how are {}?&quot;, &quot;there&quot;, &quot;you&quot;);
 * </code>
 * <p>
 * is same as
 * <p>
 * <code>
 * new CoreException(&quot;Hi there, how are you?&quot;);
 * </code>
 *
 * @author matthieun
 * @author tony
 * @author Yazad Khambata
 */
public class CoreException extends RuntimeException
{
    private static final long serialVersionUID = 5019327451085548495L;

    public static final String TOKEN = CoreException.class.getSimpleName();

    private static final Function<Object[], Object[]> refineArguments = arguments ->
    {
        if (arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable)
        {
            final Object[] result = new Object[arguments.length - 1];
            for (int i = 0; i < arguments.length - 1; i++)
            {
                result[i] = arguments[i];
            }
            return result;
        }
        else
        {
            return arguments;
        }
    };

    public static Supplier<CoreException> supplier(final String message)
    {
        return () -> new CoreException(message);
    }

    public static Supplier<CoreException> supplier(final String message, final Object... arguments)
    {
        return () -> new CoreException(message, arguments);
    }

    public static Supplier<CoreException> supplier(final String message, final Throwable cause)
    {
        return () -> new CoreException(message, cause);
    }

    public CoreException(final String message)
    {
        super(message);
    }

    public CoreException(final String message, final Object... arguments)
    {
        super(MessageFormatter.arrayFormat(message, refineArguments.apply(arguments)).getMessage(),
                arguments.length != refineArguments.apply(arguments).length
                        ? (Throwable) arguments[arguments.length - 1]
                        : null);
    }

    public CoreException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public CoreException(final String message, final Throwable cause, final Object... arguments)
    {
        super(MessageFormatter.arrayFormat(message, arguments).getMessage(), cause);
    }

    protected static String messageWithToken(final String message)
    {
        final String separator = "; ";

        return new StringBuilder(TOKEN).append(separator).append(message).toString();
    }
}
