package org.openstreetmap.atlas.utilities.testing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very simple Java reflection code used when parsing an array of Strings set on a bean in key=value
 * pairs. It looks for a method named set[key], where the first character of key is upper-cased. The
 * reflected method must take a single String as the parameter.
 *
 * @author cstaylor
 */
public class BeanHandler implements FieldHandler
{
    private static final Logger logger = LoggerFactory.getLogger(BeanHandler.class);

    @Override
    public void create(final Field field, final CoreTestRule rule, final CreationContext context)
    {
        try
        {
            final Class<?> fieldClass = field.getType();
            final Optional<Constructor<?>> constructor = findConstructorIn(fieldClass);
            if (constructor.isPresent())
            {
                final Object object = constructor.get().newInstance();
                final Bean bean = field.getAnnotation(Bean.class);
                Arrays.asList(bean.value()).stream().map(value -> StringList.split(value, "="))
                        .filter(list -> list.size() == 2).forEach(stringList ->
                        {
                            String key = stringList.get(0);
                            key = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
                            try
                            {
                                final Method method = fieldClass.getDeclaredMethod(key,
                                        String.class);
                                method.invoke(object, stringList.get(1));
                            }
                            catch (final NoSuchMethodException | InvocationTargetException
                                    | IllegalAccessException oops)
                            {
                                throw new CoreException("Couldn't call {} on {}", key, fieldClass,
                                        oops);
                            }
                        });
                field.set(rule, object);
            }
        }
        catch (final InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e)
        {
            throw new CoreException("Unable to create rule", e);
        }
    }

    @Override
    public boolean handles(final Field field)
    {
        return findConstructorIn(field.getType()).isPresent();
    }

    private Optional<Constructor<?>> findConstructorIn(final Class<?> klass)
    {
        try
        {
            return Optional.of(klass.getDeclaredConstructor());
        }
        catch (final Exception oops)
        {
            logger.warn("Couldn't find default constructor", oops);
            return Optional.empty();
        }
    }
}
