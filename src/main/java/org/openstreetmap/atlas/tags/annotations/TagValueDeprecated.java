package org.openstreetmap.atlas.tags.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a particular TagValue shouldn't be used anymore, we can mark it with this annotation. In the
 * future the tag integrity check can use this information to flag tag values as obsolete
 *
 * @author cstaylor
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface TagValueDeprecated
{
    boolean value() default true;
}
