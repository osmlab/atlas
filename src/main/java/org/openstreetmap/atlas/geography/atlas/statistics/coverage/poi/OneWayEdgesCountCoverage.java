package org.openstreetmap.atlas.geography.atlas.statistics.coverage.poi;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.OneWayEdgeCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for the name tag
 *
 * @author matthieun
 */
public class OneWayEdgesCountCoverage extends EdgesCountCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(OneWayEdgesCountCoverage.class);

    public OneWayEdgesCountCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public OneWayEdgesCountCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return edge.containsValue(OneWayEdgeCoverage.ONE_WAY_KEY, OneWayEdgeCoverage.ONE_WAYS)
                || edge.containsValue(OneWayEdgeCoverage.JUNCTION_KEY,
                        OneWayEdgeCoverage.ROUNDABOUT);
    }

    @Override
    protected String type()
    {
        return "one_way_count";
    }
}
