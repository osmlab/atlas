package org.openstreetmap.atlas.utilities.configuration;

import java.util.Optional;

/**
 * Configurable wrapper
 *
 * @author brian_l_davis
 */
public interface Configurable
{
    /**
     * @param <Type>
     *            property type
     * @return the current value
     */
    <Type> Type value();

    /**
     * @param <Type>
     *            property type
     * @return Optional of the current value, wrapping {@code null}
     */
    <Type> Optional<Type> valueOption();
}
