package org.openstreetmap.atlas.utilities.configuration;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

/**
 * Standard implementation of the Configuration interface supporting dot-notation key-value lookup.
 *
 * @author cstaylor
 * @author brian_l_davis
 * @author jklamer
 */
public class StandardConfiguration implements Configuration
{
    /**
     * Enum for the supported configuration file formats
     */
    public enum ConfigurationFormat
    {
        JSON,
        YAML,
        UNKNOWN
    }

    /**
     * Configurable implementation that pulls from the outer class's data table
     *
     * @param <R>
     *            configured type
     * @param <T>
     *            transformed type
     * @author cstaylor
     * @author brian_l_davis
     * @author cameron_frenette
     */
    private final class StandardConfigurable<R, T> implements Configurable
    {
        private final T defaultValue;
        private final String key;
        private final Function<R, T> transform;

        private StandardConfigurable(final String key, final R defaultValue,
                final Function<R, T> transform)
        {
            this.key = key;
            this.transform = transform;
            this.defaultValue = Optional.ofNullable(defaultValue).map(transform).orElse(null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V> V value()
        {
            try
            {
                final R found = (R) resolve(this.key, StandardConfiguration.this.configurationData);
                return (V) Optional.ofNullable(found).map(this.transform).orElse(this.defaultValue);
            }
            catch (final ClassCastException e)
            {
                logger.error(String.format("Invalid configuration type for %s", this.key), e);
            }
            return null;
        }

        @Override
        public <V> Optional<V> valueOption()
        {
            return Optional.ofNullable(value());
        }
    }

    // "override" is no longer available to use as a configuration key
    private static final String OVERRIDE_STRING = "override";
    private static final String DOT = ".";
    private static final Logger logger = LoggerFactory.getLogger(StandardConfiguration.class);
    private Map<String, Object> configurationData;
    private final String name;

    public StandardConfiguration(final Resource resource)
    {
        this(resource, ConfigurationFormat.UNKNOWN);
    }

    public StandardConfiguration(final Resource resource, final ConfigurationFormat configFormat)
    {
        this.name = resource.getName();
        final byte[] configBytes = resource.readBytesAndClose();

        switch (configFormat)
        {
            case JSON:
                this.configurationData = this.readConfigurationMapFromJSON(configBytes)
                        .orElseThrow(() -> new CoreException("Unable to load JSON configuration."));
                return;
            case YAML:
                this.configurationData = this.readConfigurationMapFromYAML(configBytes)
                        .orElseThrow(() -> new CoreException("Unable to load YAML configuration."));
                return;
            case UNKNOWN:
            default:
                // If the config format is unknown, attempt to load the config with each format
                // until one finds some data
                final Optional<Map<String, Object>> loadedConfigMap = Stream
                        .<Supplier<Optional<Map<String, Object>>>> of(
                                () -> this.readConfigurationMapFromJSON(configBytes),
                                () -> this.readConfigurationMapFromYAML(configBytes))
                        .map(Supplier::get).filter(Optional::isPresent).map(Optional::get)
                        .findFirst();

                this.configurationData = loadedConfigMap.orElseThrow(
                        () -> new CoreException("Unable to load UNKNOWN configuration."));
        }
    }

    public StandardConfiguration(final String name, final Map<String, Object> configurationData)
    {
        this.name = name;
        this.configurationData = configurationData;
    }

    @Override
    public Set<String> configurationDataKeySet()
    {
        return new HashSet<>(this.configurationData.keySet());
    }

    @Override
    public Configuration configurationForKeyword(final String keyword)
    {
        final Optional<Map<String, Object>> overrideDataForKeyword = this
                .getOverrideDataForKeyword(keyword, this.configurationData);
        if (overrideDataForKeyword.isPresent())
        {
            return new MergedConfiguration(
                    new StandardConfiguration(this.name, overrideDataForKeyword.get()), this);
        }
        return this;
    }

    @Override
    public Configurable get(final String key)
    {
        return new StandardConfigurable<>(key, null, Function.identity());
    }

    @Override
    public <R, T> Configurable get(final String key, final Function<R, T> transform)
    {
        return new StandardConfigurable<>(key, null, transform);
    }

    @Override
    public Configurable get(final String key, final Object defaultValue)
    {
        return new StandardConfigurable<>(key, defaultValue, Function.identity());
    }

    @Override
    public <R, T> Configurable get(final String key, final R defaultValue,
            final Function<R, T> transform)
    {
        return new StandardConfigurable<>(key, defaultValue, transform);
    }

    @Override
    public String toString()
    {
        return this.name != null ? this.name : super.toString();
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> getOverrideDataForKeyword(final String keyword,
            final Map<String, Object> currentContext)
    {
        final List<String> overrideKeyPrefixList = Arrays.asList(OVERRIDE_STRING, keyword);
        final String overrideKeyPrefixString = String.join(DOT, overrideKeyPrefixList);
        final Map<String, Object> overrideData = new HashMap<>();
        for (final Entry<String, Object> entry : currentContext.entrySet())
        {
            final String key = entry.getKey();
            if (!key.equals(OVERRIDE_STRING))
            {
                final String overrideKey = String.join(DOT, overrideKeyPrefixString, key);
                final Optional<Object> specificOverrideData = Optional
                        .ofNullable(this.resolve(overrideKey, currentContext));
                if (specificOverrideData.isPresent())
                {
                    overrideData.put(key, specificOverrideData.get());
                }
                else
                {
                    final Object nextContext = entry.getValue();
                    if (nextContext instanceof Map)
                    {
                        this.getOverrideDataForKeyword(keyword, (Map<String, Object>) nextContext)
                                .ifPresent(moreOverrideData -> overrideData.put(key,
                                        moreOverrideData));
                    }
                }
            }
        }
        return Optional.of(overrideData).filter(data -> !data.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> readConfigurationMapFromJSON(final byte[] readBytes)
    {
        logger.info("Attempting to load configuration as JSON");
        try (ByteArrayInputStream read = new ByteArrayInputStream(readBytes))
        {
            final ObjectMapper objectMapper = new ObjectMapper();
            final SimpleModule simpleModule = new SimpleModule();
            simpleModule.addDeserializer(Map.class, new ConfigurationDeserializer());
            objectMapper.registerModule(simpleModule);
            final JsonParser parser = new JsonFactory().createParser(read);
            final Map<String, Object> readConfig = objectMapper.readValue(parser, Map.class);
            logger.info("Success! Loaded JSON configuration");
            return Optional.of(readConfig);
        }
        catch (final Exception jsonReadException)
        {
            logger.warn("Unable to parse config file as JSON");
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> readConfigurationMapFromYAML(final byte[] readBytes)
    {
        final ByteArrayInputStream read = new ByteArrayInputStream(readBytes);
        logger.info("Attempting to load configuration as YAML.");
        try
        {
            final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            final SimpleModule simpleModule = new SimpleModule();
            simpleModule.addDeserializer(Map.class, new ConfigurationDeserializer());
            objectMapper.registerModule(simpleModule);
            final YAMLParser parser = new YAMLFactory().createParser(read);
            final Map<String, Object> readConfig = objectMapper.readValue(parser, Map.class);
            logger.info("Success! Loaded YAML configuration.");
            return Optional.of(readConfig);
        }
        catch (final Exception yamlReadException)
        {
            logger.warn("Unable to parse config file as YAML");
            return Optional.empty();
        }
        finally
        {
            IOUtils.closeQuietly(read);
        }
    }

    @SuppressWarnings("unchecked")
    private Object resolve(final String key, final Map<String, Object> currentContext)
    {
        if (StringUtils.isEmpty(key))
        {
            return currentContext;
        }
        final LinkedList<String> rootParts = new LinkedList<>(Arrays.asList(key.split("\\.")));
        final LinkedList<String> childParts = new LinkedList<>();
        while (!rootParts.isEmpty())
        {
            final String currentKey = String.join(DOT, rootParts);
            final Object nextItem = currentContext.get(currentKey);
            if (nextItem instanceof Map)
            {
                final String nextKey = String.join(DOT, childParts);
                return resolve(nextKey, (Map<String, Object>) nextItem);
            }
            if (nextItem != null)
            {
                return nextItem;
            }
            childParts.addFirst(rootParts.removeLast());
        }
        return null;
    }

}
