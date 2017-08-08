package org.openstreetmap.atlas.utilities.testing;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openstreetmap.atlas.exception.CoreException;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Any JUnit rules that subclass from CoreTestRule can have their values set through annotation
 * instead of direct manipulation.
 *
 * @author cstaylor
 */
public class CoreTestRule implements TestRule
{
    /**
     * This is how we compare the sort order of two Annotation classes; used by AnnotationComparator
     *
     * @author cstaylor
     */
    static class AnnotationClassComparator implements Comparator<Class<? extends Annotation>>
    {
        public static final int sortValueFor(final Class<? extends Annotation> klass)
        {
            final int returnValue = annotationLookup.indexOf(klass);
            return returnValue > -1 ? returnValue : Integer.MAX_VALUE;
        }

        @Override
        public int compare(final Class<? extends Annotation> first,
                final Class<? extends Annotation> second)
        {
            return sortValueFor(first) - sortValueFor(second);
        }
    }

    /**
     * This is how we compare the sort order of two Annotation objects; used by FieldComparator
     *
     * @author cstaylor
     */
    static class AnnotationComparator implements Comparator<Annotation>
    {
        public static final int sortValueFor(final Annotation annotation)
        {
            return AnnotationClassComparator.sortValueFor(annotation.annotationType());
        }

        @Override
        public int compare(final Annotation first, final Annotation second)
        {
            return sortValueFor(first) - sortValueFor(second);
        }
    }

    /**
     * A wrapper over a guava table for setting and retrieving objects by name and type. You can
     * think of this as a way for us to bind together references during annotation processing
     *
     * @author cstaylor
     */
    static class CreationContextImpl implements CreationContext
    {
        private final Table<String, Class<?>, Object> values;

        CreationContextImpl()
        {
            this.values = HashBasedTable.create();
        }

        /**
         * Get a saved object by name and its class
         */
        @Override
        public <T> T get(final String name, final Class<T> klass)
        {
            final Object val = this.values.get(name, klass);
            if (val != null)
            {
                return klass.cast(val);
            }
            return null;
        }

        /**
         * Set an object by name and its class; the passed class doesn't need to be the
         * implementation class, but it must be possible to cast object as a reference of that type
         * (for example, klass = java.awt.Window.class, object = java.awt.Frame instance)
         */
        @Override
        public <T> void set(final String name, final Class<T> klass, final T object)
        {
            this.values.put(name, klass, object);
        }
    }

    /**
     * This is how we sort the fields in the evaluate() method
     *
     * @author cstaylor
     */
    static class FieldComparator implements Comparator<Field>
    {
        public static final int sortValueFor(final Field field)
        {
            int currentValue = Integer.MAX_VALUE;
            for (final Annotation current : field.getAnnotations())
            {
                currentValue = Math.min(currentValue, AnnotationComparator.sortValueFor(current));
            }
            return currentValue;
        }

        @Override
        public int compare(final Field first, final Field second)
        {
            return sortValueFor(first) - sortValueFor(second);
        }
    }

    /**
     * Static global map of Java Annotation classes to FieldHandler instance
     */
    private static final SortedMap<Class<? extends Annotation>, FieldHandler> supportedAnnotations;

    /**
     * The priority order for the annotations we'll process
     */
    private static final List<Class<? extends Annotation>> annotationLookup;

    /**
     * Initialize both static tables
     */
    static
    {
        final List<Class<? extends Annotation>> temporaryLookupTable = Arrays
                .asList(TestAtlas.class, Bean.class);
        annotationLookup = Collections.unmodifiableList(temporaryLookupTable);

        final TreeMap<Class<? extends Annotation>, FieldHandler> temp = new TreeMap<>(
                new AnnotationClassComparator());
        temp.put(TestAtlas.class, new TestAtlasHandler());
        temp.put(Bean.class, new BeanHandler());
        supportedAnnotations = Collections.unmodifiableSortedMap(temp);
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return new Statement()
        {
            /**
             * Algorithm for automatically generating test fixture objects from the annotations
             * declared in our subclass: - Gather all of the public fields together - Filter out any
             * fields we don't know to support - Sort the remaining fields into dependency order;
             * basic objects first, complex objects in order of their dependency; this means we can
             * declare variables in any order in our subclasses - For each field, get the
             * annotations on that field - If we support the annotation, ask its handler to create
             * the object and store the new object into the field's slot using reflection - After
             * we've finished populating the fields in our subclass, call the super version of
             * evaluate()
             */
            @Override
            public void evaluate() throws Throwable
            {
                /* Step 1: Get our fields */
                final List<Field> fields = findAllFieldsIn(CoreTestRule.this.getClass(),
                        new ArrayList<>());

                /* Step 2: Sort the remaining fields */
                Collections.sort(fields, new FieldComparator());

                /* Step 3: Iterate the fields and execute each */
                try
                {
                    final CreationContextImpl context = new CreationContextImpl();
                    for (final Field field : fields)
                    {
                        for (final Annotation annotation : field.getAnnotations())
                        {
                            final FieldHandler handler = supportedAnnotations
                                    .get(annotation.annotationType());
                            if (handler != null && handler.handles(field))
                            {
                                handler.create(field, CoreTestRule.this, context);
                            }
                        }
                    }
                }
                catch (final Throwable oops)
                {
                    throw new CoreException("Error when processing fields in test code annotations",
                            oops);
                }
                base.evaluate();
            }
        };
    }

    /**
     * Do we handle this Field?
     *
     * @param field
     *            the field to interrogate
     * @return true if we can handle it, false otherwise
     */
    private boolean filter(final Field field)
    {
        return FieldComparator.sortValueFor(field) < Integer.MAX_VALUE;
    }

    @SuppressWarnings("unchecked")
    private List<Field> findAllFieldsIn(final Class<? extends CoreTestRule> klass,
            final List<Field> fields)
    {
        /*
         * Shamelessly stolen from SO:
         * http://stackoverflow.com/questions/1196192/how-do-i-read-a-private-field-in-java
         */
        for (final Field field : klass.getDeclaredFields())
        {
            if (filter(field))
            {
                field.setAccessible(true);
                fields.add(field);
            }
        }
        if (CoreTestRule.class.isAssignableFrom(klass.getSuperclass()))
        {
            findAllFieldsIn((Class<? extends CoreTestRule>) klass.getSuperclass(), fields);
        }
        return fields;
    }
}
