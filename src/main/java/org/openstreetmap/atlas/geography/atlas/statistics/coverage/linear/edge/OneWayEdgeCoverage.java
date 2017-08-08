package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for the lanes tag
 *
 * @author matthieun
 */
public class OneWayEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(OneWayEdgeCoverage.class);

    public static final Set<String> ONE_WAYS = Iterables.asSet(new String[] { "yes", "1", "-1" });
    public static final Set<String> ROUNDABOUT = Iterables.asSet(new String[] { "roundabout" });
    public static final String ONE_WAY_KEY = "oneway";
    public static final String JUNCTION_KEY = "junction";

    public OneWayEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public OneWayEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return edge.containsValue(ONE_WAY_KEY, ONE_WAYS)
                || edge.containsValue(JUNCTION_KEY, ROUNDABOUT);
    }

    @Override
    protected String type()
    {
        return "one_way";
    }
}
