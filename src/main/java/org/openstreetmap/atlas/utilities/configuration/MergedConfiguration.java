package org.openstreetmap.atlas.utilities.configuration;

import static org.openstreetmap.atlas.utilities.collections.Iterables.join;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Utility class used when reading from multiple underlying configurations. Property collisions are
 * handled using a last one wins policy. This enables both layered and partial configuration
 * organization schemes.
 *
 * @author cstaylor
 * @author brian_l_davis
 * @author jklamer
 */
public class MergedConfiguration implements Configuration
{
    /**
     * Configurable that calls out to the underlying configuration's Configurables
     *
     * @param <R>
     *            configured type
     * @param <T>
     *            transformed type
     * @author cstaylor
     */
    private class MergedConfigurable<R, T> implements Configurable
    {
        private final R defaultValue;
        private final String key;
        private final Function<R, T> transform;

        MergedConfigurable(final String key, final R defaultValue, final Function<R, T> transform)
        {
            this.key = key;
            this.transform = transform;
            this.defaultValue = defaultValue;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public <V> V value()
        {
            Object value = MergedConfiguration.this.configurations.stream()
                    .map(config -> config.get(this.key)).map(Configurable::value)
                    .filter(Objects::nonNull).findFirst().orElse(this.defaultValue);

            if (value instanceof Map)
            {
                final Map mergeMap = new HashMap();
                MergedConfiguration.this.configurations.stream()
                        .map(config -> config.get(this.key).value())
                        .filter(found -> found instanceof Map)
                        .collect(Collectors.toCollection(LinkedList::new)).descendingIterator()
                        .forEachRemaining(found -> mergeMap.putAll((Map) found));
                value = mergeMap;
            }

            return (V) this.transform.apply((R) value);
        }

        @Override
        public <V> Optional<V> valueOption()
        {
            return Optional.ofNullable(value());
        }
    }

    private final List<Configuration> configurations;

    public MergedConfiguration(final Configuration... configurations)
    {
        this(Arrays.asList(configurations));
    }

    public MergedConfiguration(final List<Configuration> configurations)
    {
        this.configurations = Collections.unmodifiableList(configurations);
    }

    public MergedConfiguration(final Resource first, final Iterable<Resource> configurations)
    {
        final LinkedList<Configuration> mergedConfigurations = new LinkedList<>();
        Iterables.stream(join(first, configurations)).map(StandardConfiguration::new)
                .forEach(mergedConfigurations::addFirst);
        this.configurations = Collections.unmodifiableList(mergedConfigurations);
    }

    public MergedConfiguration(final Resource first, final Resource... configurations)
    {
        this(first, Iterables.iterable(configurations));
    }

    /**
     * Note that the implementation of {@link Configuration#configurationDataKeySet()} for
     * {@link MergedConfiguration} will perform a set merge operation on the keysets of the
     * underlying {@link StandardConfiguration}s. Keep this in mind when using this method.
     */
    @Override
    public Set<String> configurationDataKeySet()
    {
        // merge the keysets of the underlying StandardConfigurations
        final Set<String> keySet = new HashSet<>();
        this.configurations
                .forEach(configuration -> keySet.addAll(configuration.configurationDataKeySet()));
        return keySet;
    }

    @Override
    public Configuration configurationForKeyword(final String keyword)
    {
        final List<Configuration> configurationsByKeyword = this.configurations.stream()
                .map(configuration -> configuration.configurationForKeyword(keyword))
                .collect(Collectors.toList());
        return Iterables.equals(this.configurations, configurationsByKeyword) ? this
                : new MergedConfiguration(configurationsByKeyword);
    }

    @Override
    public Configurable get(final String key)
    {
        return new MergedConfigurable<>(key, null, Function.identity());
    }

    @Override
    public <R, T> Configurable get(final String key, final Function<R, T> transform)
    {
        return new MergedConfigurable<>(key, null, transform);
    }

    @Override
    public <R, T> Configurable get(final String key, final R defaultValue,
            final Function<R, T> transform)
    {
        return new MergedConfigurable<>(key, defaultValue, transform);
    }

    @Override
    public <T> Configurable get(final String key, final T defaultValue)
    {
        return new MergedConfigurable<>(key, defaultValue, Function.identity());
    }

    @Override
    public Optional<Configuration> subConfiguration(final String key)
    {
        final Object all = this.get("").value();
        if (all == null)
        {
            return Optional.empty();
        }
        final Map<String, Object> map;
        if (all instanceof Map)
        {
            map = (Map<String, Object>) all;
        }
        else
        {
            map = new HashMap<>();
            map.put("", all);
        }
        final StandardConfiguration standardConfiguration = new StandardConfiguration("", map);
        return standardConfiguration.subConfiguration(key);
    }
}
