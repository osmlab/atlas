package org.openstreetmap.atlas.geography.atlas.statistics.coverage.poi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.statistics.AtlasStatistics.StatisticKey;
import org.openstreetmap.atlas.geography.atlas.statistics.AtlasStatistics.StatisticValue;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.Coverage;
import org.openstreetmap.atlas.tags.LastEditUserNameTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Count the last user editing features
 *
 * @author matthieun
 */
public class LastUserNameCountCoverage extends SimpleCoverage<AtlasEntity>
{
    private static final Logger logger = LoggerFactory.getLogger(LastUserNameCountCoverage.class);

    private final long cutoff;

    /**
     * @param atlas
     *            The atlas to read from
     * @param cutoff
     *            The cutoff number of last edited features under which a user is not counted.
     */
    public LastUserNameCountCoverage(final Atlas atlas, final long cutoff)
    {
        super(logger, atlas, CoverageType.COUNT);
        this.cutoff = cutoff;
    }

    @Override
    public Map<StatisticKey, StatisticValue> getStatistic()
    {
        // Do like in Freshness, re-assign the key as a Sub-Type
        final Map<StatisticKey, StatisticValue> old = super.getStatistic();
        double total = 0.0;
        for (final Entry<StatisticKey, StatisticValue> entry : old.entrySet())
        {
            if (!entry.getKey().getTag().equals(AGGREGATE_KEY))
            {
                total += entry.getValue().getCount();
            }
        }
        final double totalCount = total;
        final Map<StatisticKey, StatisticValue> result = new HashMap<>();
        final List<Double> aggregatedCounts = new ArrayList<>();
        aggregatedCounts.add(0.0);
        old.forEach((oldKey, oldValue) ->
        {
            if (oldValue.getCount() >= this.cutoff)
            {
                if (!oldKey.getTag().equals(AGGREGATE_KEY))
                {
                    final StatisticKey key = new StatisticKey(Coverage.NULL_KEY, oldKey.getType(),
                            oldKey.getTag());
                    final StatisticValue value = new StatisticValue(oldValue.getCount(),
                            totalCount);
                    result.put(key, value);
                }
            }
            else
            {
                aggregatedCounts.set(0, aggregatedCounts.get(0) + oldValue.getCount());
            }
        });
        result.put(new StatisticKey(Coverage.NULL_KEY, type(), "_all_others_below_" + this.cutoff),
                new StatisticValue(aggregatedCounts.get(0), totalCount));
        return result;
    }

    @Override
    protected Iterable<AtlasEntity> getEntities()
    {
        return getAtlas();
    }

    @Override
    protected Set<String> getKeys(final AtlasEntity item)
    {
        // In CountCoverage, the key is always "All", but in this case, override that to make it the
        // last user's name.
        final HashSet<String> result = new HashSet<>();
        final Optional<String> lastUser = item.lastUserName();
        if (lastUser.isPresent())
        {
            result.add(lastUser.get());
        }
        else
        {
            result.add("unknown_last_user");
        }
        return result;
    }

    @Override
    protected String type()
    {
        return LastEditUserNameTag.KEY;
    }

    @Override
    protected Predicate<Taggable> validKeyValuePairs()
    {
        // count all the values.
        return taggable -> true;
    }
}
