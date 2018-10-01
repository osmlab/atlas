package org.openstreetmap.atlas.geography.atlas.statistics.coverage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.statistics.AtlasStatistics.StatisticKey;
import org.openstreetmap.atlas.geography.atlas.statistics.AtlasStatistics.StatisticValue;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Ratio;
import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.slf4j.Logger;

/**
 * Coverage of an Atlas feature.
 *
 * @param <T>
 *            The type of {@link AtlasEntity}
 * @author matthieun
 */
public abstract class Coverage<T extends AtlasEntity>
{
    /**
     * @author matthieun
     */
    public enum CoverageType
    {
        DISTANCE,
        SURFACE,
        COUNT;

        public static CoverageType forName(final String name)
        {
            if (DISTANCE.name().equalsIgnoreCase(name))
            {
                return DISTANCE;
            }
            else if (SURFACE.name().equalsIgnoreCase(name))
            {
                return SURFACE;
            }
            else if (COUNT.name().equalsIgnoreCase(name))
            {
                return COUNT;
            }
            else
            {
                throw new CoreException("Unknown coverage type {}", name);
            }
        }
    }

    private static final int REPORT_FREQUENCY = 100_000;
    public static final String AGGREGATE_KEY = "all";
    public static final String NULL_KEY = "";

    private final Logger logger;
    private final Atlas atlas;
    private final Predicate<T> filter;
    private Map<String, Double> counted;
    private Map<String, Double> total;
    private Map<String, Long> validCount;
    private Map<String, Long> totalCount;
    private CounterWithStatistic statistic;
    // In case an item is spanning multiple count entities (ex. shards), supply a divisor that will
    // under-count the item.
    private Function<T, Integer> shardDivisor = null;

    private Comparator<String> keyComparator;

    public Coverage(final Logger logger, final Atlas atlas)
    {
        this(logger, atlas, item -> true);
    }

    /**
     * Construct.
     *
     * @param atlas
     *            The {@link Atlas} to crawl
     * @param filter
     *            The filter to apply to the crawled items.
     * @param logger
     *            The logger to use
     */
    public Coverage(final Logger logger, final Atlas atlas, final Predicate<T> filter)
    {
        this.logger = logger;
        this.atlas = atlas;
        this.filter = filter;
        this.counted = new HashMap<>();
        this.total = new HashMap<>();
        this.validCount = new HashMap<>();
        this.totalCount = new HashMap<>();
        this.statistic = null;
        this.keyComparator = null;
    }

    /**
     * @param key
     *            The key to get the ratio from
     * @return The count {@link Ratio} of the specified key.
     */
    public Ratio getCountCoverage(final String key)
    {
        if (!this.counted.containsKey(key))
        {
            throw new CoreException("Key {} is not valid.");
        }
        if (this.validCount.get(key) > this.totalCount.get(key))
        {
            throw new CoreException("Invalid Ratio: {} / {}", this.validCount.get(key),
                    this.totalCount.get(key));
        }
        if (this.totalCount.get(key) <= 0)
        {
            return Ratio.percentage(0);
        }
        return Ratio.ratio((double) this.validCount.get(key) / this.totalCount.get(key));
    }

    /**
     * @param key
     *            The key to get the ratio from
     * @return The coverage {@link Ratio} of the specified key.
     */
    public Ratio getCoverage(final String key)
    {
        if (!this.counted.containsKey(key))
        {
            throw new CoreException("Key {} is not valid.");
        }
        if (this.counted.get(key) > this.total.get(key))
        {
            throw new CoreException("Invalid Ratio: {} / {}", this.counted.get(key),
                    this.total.get(key));
        }
        if (this.total.get(key) <= 0.0)
        {
            throw new CoreException("Invalid Total: {}", this.total.get(key));
        }
        return Ratio.ratio(this.counted.get(key) / this.total.get(key));
    }

    public Map<StatisticKey, StatisticValue> getStatistic()
    {
        final Map<StatisticKey, StatisticValue> result = new HashMap<>();
        for (final String key : getKeys())
        {
            final StatisticKey statisticKey = new StatisticKey(key, type(), subType());
            final double count;
            final double totalCount;
            switch (coverageType())
            {
                case DISTANCE:
                case SURFACE:
                    count = this.counted.get(key);
                    totalCount = this.total.get(key);
                    break;
                case COUNT:
                    count = this.validCount.get(key);
                    totalCount = this.totalCount.get(key);
                    break;
                default:
                    throw new CoreException("Unknown coverage type {}", coverageType());
            }
            final StatisticValue statisticValue = new StatisticValue(count, totalCount);
            result.put(statisticKey, statisticValue);
        }
        return result;
    }

    /**
     * @param key
     *            The key to test for
     * @return True if this {@link Coverage} covers the specified key
     */
    public boolean hasKey(final String key)
    {
        return this.counted.containsKey(key);
    }

    /**
     * Execute this {@link Coverage}
     */
    public void run()
    {
        this.statistic = new CounterWithStatistic(this.logger, REPORT_FREQUENCY,
                this.getClass().getSimpleName());
        items().forEach(item ->
        {
            this.statistic.increment();
            keys(item).forEach(key ->
            {
                final double value = getValue(item);
                // The adjusted value is the value divided by the divisor. The divisor can be the
                // number of shards the feature crosses. For example, an Edge that spans three
                // shards will be counted one third. Then when aggregating the counts for all the
                // shards, this value will not have been counted three times too many.
                final double adjustedValue = this.shardDivisor == null ? value
                        : value / this.shardDivisor.apply(item);
                increment(key, adjustedValue, isCounted(item));
            });
        });
    }

    public void setShardDivisor(final Function<T, Integer> shardDivisor)
    {
        this.shardDivisor = shardDivisor;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(this.getClass().getSimpleName());
        builder.append(":\n");
        for (final String key : getKeys())
        {
            builder.append("\t");
            builder.append(key);
            builder.append(" = \n\t{\n\t\t");
            builder.append(getCoverage(key));
            builder.append(" of ");
            builder.append(String.format("%,.2f", this.total.get(key)));
            builder.append(" ");
            builder.append(getUnit());
            builder.append(",\n\t\t");
            builder.append(getCountCoverage(key));
            builder.append(" of ");
            builder.append(String.format("%,d", this.totalCount.get(key)));
            builder.append(" features\n\t},\n");
        }
        builder.append("]");
        return builder.toString();
    }

    protected abstract CoverageType coverageType();

    protected Atlas getAtlas()
    {
        return this.atlas;
    }

    /**
     * @return All the wanted {@link AtlasEntity}s
     */
    protected abstract Iterable<T> getEntities();

    protected Set<String> getKeys()
    {
        final Set<String> keySet;
        if (this.keyComparator != null)
        {
            keySet = new TreeSet<>(this.keyComparator);
        }
        else
        {
            keySet = new HashSet<>();
        }
        keySet.addAll(this.total.keySet());
        return keySet;
    }

    /**
     * Get all the categories (or "keys") for which an item can be accounted for. For example, an
     * Edge that is a highway would be categorized as "highway" and a trunk road edge would be in
     * the different category "trunk".
     *
     * @param item
     *            The item to test for
     * @return All the categories (or "keys") for which the item can be accounted for
     */
    protected abstract Set<String> getKeys(T item);

    /**
     * @return The unit (if any, empty {@link String} otherwise) that is used to meter each item.
     */
    protected abstract String getUnit();

    /**
     * The value used to meter an item.
     *
     * @param item
     *            The item to meter
     * @return The value of the item to meter.
     */
    protected abstract double getValue(T item);

    /**
     * Increment counts for an item with a specific key
     *
     * @param key
     *            One of the item's keys.
     * @param value
     *            The item's value
     * @param valid
     *            True if the item is valid within the requirements of the coverage metric.
     */
    protected void increment(final String key, final double value, final boolean valid)
    {
        if (value < 0.0)
        {
            throw new CoreException("Invalid value {}", value);
        }
        if (!this.total.containsKey(key))
        {
            this.total.put(key, 0.0);
            this.totalCount.put(key, 0L);
            this.counted.put(key, 0.0);
            this.validCount.put(key, 0L);
        }
        this.total.put(key, this.total.get(key) + value);
        this.totalCount.put(key, this.totalCount.get(key) + 1);
        if (valid)
        {
            this.counted.put(key, this.counted.get(key) + value);
            this.validCount.put(key, this.validCount.get(key) + 1);
        }
    }

    /**
     * @param item
     *            The item to test
     * @return True if the item is valid within the requirements of the coverage metric.
     */
    protected abstract boolean isCounted(T item);

    /**
     * @param keyComparator
     *            The String comparator to order the keys when printing
     */
    protected void setKeyComparator(final Comparator<String> keyComparator)
    {
        this.keyComparator = keyComparator;
    }

    protected abstract String subType();

    protected abstract String type();

    /**
     * @return The filtered {@link Iterable} of items to measure.
     */
    private Iterable<T> items()
    {
        return Iterables.filter(getEntities(), this.filter);
    }

    /**
     * The keys for an item. This method wraps the abstract getKeys() method and adds an all, which
     * will represent an aggregate of all the coverages. If the keys provided by the sub class is
     * empty, there is no need to add an "all" key. Instead a null key is added.
     *
     * @param item
     *            The item to get the keys from.
     * @return The keys from the sub-class, plus the "All" key.
     */
    private Set<String> keys(final T item)
    {
        final Set<String> result = getKeys(item);
        if (result.isEmpty())
        {
            result.add(NULL_KEY);
        }
        else
        {
            result.add(AGGREGATE_KEY);
        }
        return result;
    }
}
