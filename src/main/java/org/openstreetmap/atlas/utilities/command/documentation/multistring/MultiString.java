package org.openstreetmap.atlas.utilities.command.documentation.multistring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that provides a simple implementation of multi-line string literals. Based on a
 * reference implementation from Adrian Walker.
 *
 * @see "http://www.adrianwalker.org/2011/12/java-multiline-string.html"
 * @author lcram
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface MultiString
{

}
