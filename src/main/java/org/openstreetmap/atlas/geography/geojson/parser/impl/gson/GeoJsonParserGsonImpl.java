package org.openstreetmap.atlas.geography.geojson.parser.impl.gson;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.Validate;
import org.openstreetmap.atlas.geography.geojson.parser.GeoJsonParser;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.Type;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Yazad Khambata
 */
public enum GeoJsonParserGsonImpl implements GeoJsonParser
{
    
    instance;
    
    private static final Logger log = LoggerFactory.getLogger(GeoJsonParserGsonImpl.class);
    private static final Set<Class<?>> scalarTypes = new HashSet<>(
            Arrays.asList(String.class, Integer.class, Long.class, Float.class, Double.class,
                    Short.class, Boolean.class, Byte.class));
    
    @Override
    public GeoJsonItem deserialize(final String geoJson)
    {
        log.info("geoJson:: {}.", geoJson);
        
        final Map<String, Object> map = toMap(geoJson);
        
        return deserialize(map);
    }
    
    @Override
    public GeoJsonItem deserialize(final Map<String, Object> map)
    {
        log.info("map:: {}.", map);
        
        final Type type = TypeUtil.identifyStandardType(getType(map));
        
        return type.construct(GeoJsonParserGsonImpl.instance, map);
    }
    
    @Override
    public <T> T deserializeExtension(String json, Class<T> targetClass)
    {
        final Map<String, Object> map = toMap(json);
        return deserializeExtension(map, targetClass);
    }
    
    @Override
    public <T> T deserializeExtension(Map<String, Object> map, Class<T> targetClass)
    {
        final T t = create(targetClass);
        
        populate(map, t);
        
        return t;
    }
    
    private <T> void copyProperty(BeanUtilsBean beanUtilsBean, T t, String name, Object value)
    {
        try
        {
            beanUtilsBean.copyProperty(t, name, value);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(
                    "Failed to copy " + value + " in " + t.getClass() + "#" + name + ".", e);
        }
    }
    
    private <T> T create(Class<T> targetClass)
    {
        try
        {
            return targetClass.newInstance();
        }
        catch (final Exception e)
        {
            throw new IllegalStateException(e);
        }
    }
    
    private String getType(final Map<String, Object> map)
    {
        final Object type = map.get("type");
        Validate.isTrue(type instanceof String);
        return (String) type;
    }
    
    private <C> boolean isScalarType(final Class<C> clazz)
    {
        return scalarTypes.contains(clazz) || clazz.isPrimitive();
    }
    
    private <T> void populate(Map<String, Object> map, T t)
    {
        Validate.notNull(map, "input map is NULL.");
        Validate.notNull(t, "t is NULL");
        
        log.info("map: {}.", map);
        final BeanUtilsBean beanUtilsBean = new BeanUtilsBean();
        
        final PropertyDescriptor[] propertyDescriptors =
                beanUtilsBean.getPropertyUtils().getPropertyDescriptors(t);
        
        //Start with the concrete object
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors)
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
                //Scalar types or value is a Map in concrete class.
                //Map values can be scalar or nested maps of scalars.
                copyProperty(beanUtilsBean, t, name, value);
            }
            else if (!propertyType.isArray())
            {
                //User-defined concrete classes.
                final T child = (T) create(propertyType);
                populate((Map<String, Object>) value, child);
                
                copyProperty(beanUtilsBean, t, name, child);
            }
            else
            {
                //Array case.
                final List<Object> values = (List<Object>) value;
                if (values == null || values.isEmpty() || values.get(0) == null)
                {
                    continue;
                }
                
                if (isScalarType(values.get(0).getClass()))
                {
                    copyProperty(beanUtilsBean, t, name, values.toArray());
                }
                else
                {
                    log.info("values: {}.", values);
                    final Class<?> componentType = propertyType.getComponentType();
                    final Object valuesAsObjects = values.stream().map(item ->
                    {
                        Validate.notNull(item, "item is NULL, do you have a trailing comma in the JSON?");
                        
                        final T child = (T) create(componentType);
                        populate((Map<String, Object>) item, child);
                        return child;
                    }).toArray((size) -> (Object[]) Array.newInstance(componentType, values.size()));
    
                    copyProperty(beanUtilsBean, t, name, valuesAsObjects);
                }
            }
        }
    }
    
    private Map<String, Object> toMap(String geoJson)
    {
        final Gson gson = new GsonBuilder().create();
        return (Map<String, Object>) gson.fromJson(geoJson, Object.class);
    }
}
