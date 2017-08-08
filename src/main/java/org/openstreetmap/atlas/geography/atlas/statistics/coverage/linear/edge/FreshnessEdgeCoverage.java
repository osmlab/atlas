package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.statistics.AtlasStatistics.StatisticKey;
import org.openstreetmap.atlas.geography.atlas.statistics.AtlasStatistics.StatisticValue;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class FreshnessEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(FreshnessEdgeCoverage.class);

    public FreshnessEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public FreshnessEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    public Map<StatisticKey, StatisticValue> getStatistic()
    {
        final Map<StatisticKey, StatisticValue> old = super.getStatistic();
        final Map<String, Double> totals = new HashMap<>();
        final Map<String, Double> monthAll = new HashMap<>();
        old.forEach((oldKey, oldValue) ->
        {
            final StringList split = StringList.split(oldKey.getTag(), "_");
            if (split.size() == 2)
            {
                final String tag = split.get(0);
                final String month = split.get(1);
                Double allResult = oldValue.getCount();
                if (monthAll.containsKey(month))
                {
                    allResult += monthAll.get(month);
                }
                monthAll.put(month, allResult);
                Double result = oldValue.getCount();
                if (totals.containsKey(tag))
                {
                    result += totals.get(tag);
                }
                totals.put(tag, result);
            }
        });
        final Map<StatisticKey, StatisticValue> result = new HashMap<>();
        old.forEach((oldKey, oldValue) ->
        {
            final StringList split = StringList.split(oldKey.getTag(), "_");
            if (split.size() == 2)
            {
                final StatisticKey key = new StatisticKey(split.get(0), oldKey.getType(),
                        split.get(1));
                final StatisticValue value = new StatisticValue(oldValue.getCount(),
                        totals.get(key.getTag()));
                result.put(key, value);
            }
        });
        monthAll.forEach((month, count) -> result.put(
                new StatisticKey(AGGREGATE_KEY, type(), month),
                new StatisticValue(monthAll.get(month),
                        monthAll.values().stream().reduce((left, right) -> left + right).get())));
        return result;
    }

    @Override
    protected Set<String> getKeys(final Edge edge)
    {
        final Optional<Time> lastEditOption = edge.lastEdit();
        if (!lastEditOption.isPresent())
        {
            return new HashSet<>();
        }
        final Time lastEdit = lastEditOption.get();
        final String year = String.valueOf(lastEdit.year());
        final String month = String.valueOf(lastEdit.month());
        final Set<String> result = new HashSet<>();
        result.add(edge.highwayTag().getTagValue() + "_" + year + "-" + month);
        return result;
    }

    @Override
    protected boolean isCounted(final Edge item)
    {
        return true;
    }

    @Override
    protected String subType()
    {
        return "dummy";
    }

    @Override
    protected String type()
    {
        return "freshness";
    }
}
