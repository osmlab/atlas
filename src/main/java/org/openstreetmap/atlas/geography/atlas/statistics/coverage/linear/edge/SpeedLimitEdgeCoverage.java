package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.MaxSpeedTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for the maxspeed tag
 *
 * @author matthieun
 */
public class SpeedLimitEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(SpeedLimitEdgeCoverage.class);

    public SpeedLimitEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public SpeedLimitEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return MaxSpeedTag.hasExtendedMaxSpeed(edge);
    }

    @Override
    protected String type()
    {
        return "speed_limit";
    }
}
