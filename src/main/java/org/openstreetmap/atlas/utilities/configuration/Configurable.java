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
     * @param <V>
     *            property type
     * @return the current value
     */
    <V> V value();

    /**
     * @param <V>
     *            property type
     * @return Optional of the current value, wrapping {@code null}
     */
    <V> Optional<V> valueOption();
}
