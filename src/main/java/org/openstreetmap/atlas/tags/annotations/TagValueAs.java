package org.openstreetmap.atlas.tags.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for changing how Validators will interpret a tag's value. Only applies for enums
 * constants since their values are usually directly lower-cased.
 *
 * @author cstaylor
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface TagValueAs
{
    boolean deprecated() default false;

    String value();
}
