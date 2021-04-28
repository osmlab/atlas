package org.openstreetmap.atlas.utilities.configuration;

import java.util.List;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;

import com.google.common.collect.Lists;

/**
 * Helper class to read a {@link Configuration}
 * 
 * @author matthieun
 */
public class ConfigurationReader
{
    private static final String CONFIGURATION_PATH_NAME_DEFAULT = "N/A";

    private final String root;

    public ConfigurationReader(final String root)
    {
        this.root = root;
    }

    public final String configurationKey(final String key)
    {
        return this.root.isEmpty() ? key : this.root + "." + key;
    }

    public String configurationValue(final Configuration configuration, final String key)
    {
        final String result = configuration
                .get(configurationKey(key), CONFIGURATION_PATH_NAME_DEFAULT).value();
        if (CONFIGURATION_PATH_NAME_DEFAULT.equals(result))
        {
            throw new CoreException("Malformed configuration for {}", configurationKey(key));
        }
        return result;
    }

    public <U> U configurationValue(final Configuration configuration, final String key,
            final U defaultValue)
    {
        return configuration.get(configurationKey(key), defaultValue).value();
    }

    public <R, T> T configurationValue(final Configuration configuration,
            final Function<R, T> defaultValue)
    {
        return configuration.get(this.root, defaultValue).value();
    }

    public List<String> configurationValues(final Configuration configuration, final String key,
            final List<String> defaultValue)
    {
        return configuration.get(configurationKey(key), defaultValue).value();
    }

    public List<String> configurationValues(final Configuration configuration, final String key)
    {
        final List<String> defaults = Lists.newArrayList(CONFIGURATION_PATH_NAME_DEFAULT);
        final List<String> result = configurationValues(configuration, key, defaults);
        if (defaults.equals(result))
        {
            throw new CoreException("Malformed configuration for {}", configurationKey(key));
        }
        return result;
    }

    public boolean isPresent(final Configuration configuration, final String key)
    {
        final Object result = configuration
                .get(configurationKey(key), CONFIGURATION_PATH_NAME_DEFAULT).value();
        return !CONFIGURATION_PATH_NAME_DEFAULT.equals(result);
    }
}
