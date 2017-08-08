package org.openstreetmap.atlas.tags.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java annotation for constants in an Atlas tag definition for Tag.Validation.MATCH rules
 *
 * @author cstaylor
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface TagValue
{
    /**
     * We can support exact string match values or regular expression patterns. The default is exact
     * string matches
     *
     * @author cstaylor
     */
    enum ValueType
    {
        EXACT,
        REGEX;
    }

    ValueType value() default ValueType.EXACT;
}
