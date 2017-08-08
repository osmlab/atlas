package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.LanesTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for the lanes tag
 *
 * @author matthieun
 */
public class LanesEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(LanesEdgeCoverage.class);

    public LanesEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public LanesEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return LanesTag.numberOfLanes(edge).isPresent();
    }

    @Override
    protected String type()
    {
        return "length_road_lanes";
    }
}
