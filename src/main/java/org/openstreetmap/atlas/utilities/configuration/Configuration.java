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
     * @param <Raw>
     *            configured type
     * @param <Transformed>
     *            transformed type
     * @return a {@link Configurable} wrapper
     */
    <Raw, Transformed> Configurable get(String key, Function<Raw, Transformed> transform);

    /**
     * Returns a {@link Configurable} wrapper around the configured property.
     *
     * @param key
     *            property key
     * @param defaultValue
     *            value returned if not found in the configuration
     * @param transform
     *            applied to the configured property
     * @param <Raw>
     *            configured type
     * @param <Transformed>
     *            transformed type
     * @return a {@link Configurable} wrapper
     */
    <Raw, Transformed> Configurable get(String key, Raw defaultValue,
            Function<Raw, Transformed> transform);

    /**
     * Returns a {@link Configurable} wrapper around the configured property.
     *
     * @param key
     *            property key
     * @param defaultValue
     *            value returned if not found in the configuration
     * @param <Type>
     *            configured type
     * @return a {@link Configurable} wrapper
     */
    <Type> Configurable get(String key, Type defaultValue);
}
