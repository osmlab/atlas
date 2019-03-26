package org.openstreetmap.atlas.utilities.configuration;

import java.util.Set;
import java.util.function.Function;

/**
 * Configuration interface providing key-value look with support for defaults and transformations.
 *
 * @author brian_l_davis
 */
public interface Configuration
{
    /**
     * Returns a set view of all the top level keys in this {@link Configuration}.
     *
     * @return the set of top level keys
     */
    Set<String> configurationDataKeySet();

    /**
     * Returns a returns a copy Configuration specific to a keyword with overwritten values
     *
     * @param keyword
     *            keyword string
     * @return Configuration
     */
    Configuration configurationForKeyword(String keyword);

    /**
     * Returns a {@link Configurable} wrapper around the configured property.
     *
     * @param key
     *            property key
     * @return a {@link Configurable} wrapper
     */
    Configurable get(String key);

    /**
     * Returns a {@link Configurable} wrapper around the configured property.
     *
     * @param key
     *            property key
     * @param transform
     *            applied to the configured property
     * @param <R>
     *            configured type
     * @param <T>
     *            transformed type
     * @return a {@link Configurable} wrapper
     */
    <R, T> Configurable get(String key, Function<R, T> transform);

    /**
     * Returns a {@link Configurable} wrapper around the configured property.
     *
     * @param key
     *            property key
     * @param defaultValue
     *            value returned if not found in the configuration
     * @param transform
     *            applied to the configured property
     * @param <R>
     *            configured type
     * @param <T>
     *            transformed type
     * @return a {@link Configurable} wrapper
     */
    <R, T> Configurable get(String key, R defaultValue, Function<R, T> transform);

    /**
     * Returns a {@link Configurable} wrapper around the configured property.
     *
     * @param key
     *            property key
     * @param defaultValue
     *            value returned if not found in the configuration
     * @param <T>
     *            configured type
     * @return a {@link Configurable} wrapper
     */
    <T> Configurable get(String key, T defaultValue);
}
