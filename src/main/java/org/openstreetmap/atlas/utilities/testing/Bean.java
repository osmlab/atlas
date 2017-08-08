package org.openstreetmap.atlas.utilities.testing;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Uses introspection to set String values. There are plenty of frameworks out there that do this
 * already (Apache BeanUtils, minimalcode), but I didn't want to bring in yet another framework
 * unless we absolutely needed to.
 *
 * @author cstaylor
 */
@Retention(value = RUNTIME)
@Target(value = FIELD)
public @interface Bean
{
    String[] value() default {};
}
