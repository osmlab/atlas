package org.openstreetmap.atlas.tags.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java annotation for an Atlas tag's key field. We can find this value at runtime using java
 * introspection without resorting to idioms like 'KEY_' or '_TAG'
 *
 * @author cstaylor
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface TagKey
{
    /**
     * Some keys in OSM can be localized (for example, wikipedia:[language ISO2 code])
     *
     * @author cstaylor
     */
    enum KeyType
    {
        EXACT,
        LOCALIZED
    }

    KeyType value() default KeyType.EXACT;
}
