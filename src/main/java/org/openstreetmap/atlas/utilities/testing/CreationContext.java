package org.openstreetmap.atlas.utilities.testing;

/**
 * Lookup table based on name of the field, the data type of what can be stored in the field, and
 * the object that corresponds to the mapping of name and implementation class.
 *
 * @author cstaylor
 */
public interface CreationContext
{
    /**
     * Based on a name and implementation class pair, return an instance of type T, which should be
     * of the same type as klass
     *
     * @param name
     *            the name of the object to retrieve
     * @param klass
     *            the type of the object to retrieve
     * @param <T>
     *            the type of object we're looking for within this context
     * @return T if it exists within this context, null otherwise
     */
    <T> T get(String name, Class<T> klass);

    /**
     * Set a mapping between the class pair of klass and name to an object
     *
     * @param name
     *            the name of the object
     * @param klass
     *            the type of the object
     * @param object
     *            the instance of klass that should be retained
     * @param <T>
     *            the type of object we're setting within this context
     */
    <T> void set(String name, Class<T> klass, T object);
}
