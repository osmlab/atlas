package org.openstreetmap.atlas.exception;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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
    public static final String TOKEN = CoreException.class.getSimpleName();
    private static final long serialVersionUID = 5019327451085548495L;
    protected static final UnaryOperator<Object[]> REFINE_ARGUMENTS = arguments ->
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
    protected static final Function<Object[], Optional<Throwable>> CAUSE_FROM = arguments -> arguments.length != REFINE_ARGUMENTS
            .apply(arguments).length ? Optional.of((Throwable) arguments[arguments.length - 1])
                    : Optional.empty();

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

    protected static String messageWithToken(final String message)
    {
        final String separator = "; ";

        return new StringBuilder(TOKEN).append(separator).append(message).toString();
    }

    public CoreException(final String message)
    {
        super(message);
    }

    /**
     * Create a new CoreException with a specified message
     *
     * @param message
     *            The message (formatted with {@link MessageFormatter#arrayFormat})
     * @param arguments
     *            The arguments (if the <i>last</i> argument is a {@link Throwable}, that becomes
     *            the cause)
     */
    public CoreException(final String message, final Object... arguments)
    {
        super(MessageFormatter.arrayFormat(message, REFINE_ARGUMENTS.apply(arguments)).getMessage(),
                CAUSE_FROM.apply(arguments).orElse(null));
    }

    public CoreException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public CoreException(final String message, final Throwable cause, final Object... arguments)
    {
        super(MessageFormatter.arrayFormat(message, arguments).getMessage(), cause);
    }
}
