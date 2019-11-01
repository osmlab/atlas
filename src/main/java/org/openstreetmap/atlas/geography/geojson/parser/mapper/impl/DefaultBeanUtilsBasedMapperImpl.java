package org.openstreetmap.atlas.geography.geojson.parser.mapper.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.Validate;
import org.openstreetmap.atlas.geography.geojson.parser.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yazad Khambata
 */
public enum DefaultBeanUtilsBasedMapperImpl implements Mapper
{
    instance;

    private static final Logger log = LoggerFactory
            .getLogger(DefaultBeanUtilsBasedMapperImpl.class);
    private static final Set<Class<?>> scalarTypes = new HashSet<>(
            Arrays.asList(String.class, Integer.class, Long.class, Float.class, Double.class,
                    Short.class, Boolean.class, Byte.class));

    @Override
    public <T> T map(final Map<String, Object> map, final Class<T> targetClass)
    {
        final T bean = create(targetClass);

        populate(map, bean);

        return bean;
    }

    private <T> void copyProperty(final BeanUtilsBean beanUtilsBean, final T bean,
            final String name, final Object value)
    {
        try
        {
            beanUtilsBean.copyProperty(bean, name, value);
        }
        catch (final Exception e)
        {
            throw new IllegalStateException(
                    "Failed to copy " + value + " in " + bean.getClass() + "#" + name + ".", e);
        }
    }

    private <T> T create(final Class<T> targetClass)
    {
        Validate.notNull(targetClass, "null class cannot be instantiated.");

        try
        {
            return targetClass.newInstance();
        }
        catch (final Exception e)
        {
            throw new IllegalStateException("Failed to construct instance of class: " + targetClass
                    + "; isArray: " + targetClass.isArray(), e);
        }
    }

    private <C> boolean isScalarType(final Class<C> clazz)
    {
        return scalarTypes.contains(clazz) || clazz.isPrimitive();
    }

    private <T> void populate(final Map<String, Object> map, final T bean)
    {
        try
        {
            Validate.notNull(map, "input map is NULL.");
            Validate.notNull(bean, "bean is NULL");

            final BeanUtilsBean beanUtilsBean = new BeanUtilsBean();

            final PropertyDescriptor[] propertyDescriptors = beanUtilsBean.getPropertyUtils()
                    .getPropertyDescriptors(bean);

            // Start with the concrete object
            for (final PropertyDescriptor propertyDescriptor : propertyDescriptors)
            {
                try
                {
                    final String name = propertyDescriptor.getName();
                    final Class<?> propertyType = propertyDescriptor.getPropertyType();

                    final Object value = map.get(name);

                    if (value == null)
                    {
                        continue;
                    }

                    if (isScalarType(propertyType) || Map.class.isAssignableFrom(propertyType))
                    {
                        // Scalar types or value is a Map in concrete class.
                        // Map values can be scalar or nested maps of scalars.
                        copyProperty(beanUtilsBean, bean, name, value);
                    }
                    else if (!propertyType.isArray())
                    {
                        // User-defined concrete classes.
                        final T child = (T) create(propertyType);
                        populate((Map<String, Object>) value, child);

                        copyProperty(beanUtilsBean, bean, name, child);
                    }
                    else
                    {
                        // Array case.
                        final List<Object> values = (List<Object>) value;
                        if (values == null || values.isEmpty() || values.get(0) == null)
                        {
                            continue;
                        }

                        if (isScalarType(values.get(0).getClass()))
                        {
                            copyProperty(beanUtilsBean, bean, name, values.toArray());
                        }
                        else
                        {
                            log.info("values: {}.", values);
                            final Class<?> componentType = propertyType.getComponentType();
                            final Object valuesAsObjects = values.stream().map(item ->
                            {
                                Validate.notNull(item,
                                        "item is NULL, do you have a trailing comma in the JSON?");

                                final T child = (T) create(componentType);
                                populate((Map<String, Object>) item, child);
                                return child;
                            }).toArray(Propersize -> (Object[]) Array.newInstance(componentType,
                                    values.size()));

                            copyProperty(beanUtilsBean, bean, name, valuesAsObjects);
                        }
                    }
                }
                catch (final Exception e)
                {
                    throw new IllegalStateException("Population failed. propertyDescriptor name: "
                            + propertyDescriptor.getName() + "; map: " + map + "; bean: " + bean
                            + ".", e);
                }
            }
        }
        catch (final Exception e)
        {
            throw new IllegalStateException(
                    "Population fialed. map: " + map + "; bean: " + bean + ".", e);
        }
    }
}
