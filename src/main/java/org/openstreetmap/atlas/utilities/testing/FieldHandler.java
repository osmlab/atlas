package org.openstreetmap.atlas.utilities.testing;

import java.lang.reflect.Field;

/**
 * A FieldHandler inspects JUnit rule class fields for annotations, interprets the annotations, and
 * then sets the field's value based on that interpretation.
 *
 * @author cstaylor
 */
public interface FieldHandler
{
    /**
     * Based on the annotations set on field, create an object compatible with Field based on any
     * annotations, other values in rule, and existing data within context
     *
     * @param field
     *            the field and associated annotations in question
     * @param rule
     *            the containing rule where field exists
     * @param context
     *            a cache of previously made objects that may be reused depending on the test
     *            implementation
     */
    void create(Field field, CoreTestRule rule, CreationContext context);

    /**
     * Is this the kind of field we can handle? Implementations may check the type of the field or
     * the annotations on it to determine if we should handle setting the value of the field
     *
     * @param field
     *            the field in question
     * @return true if this field is suitable for a call to create, false otherwise
     */
    boolean handles(Field field);
}
