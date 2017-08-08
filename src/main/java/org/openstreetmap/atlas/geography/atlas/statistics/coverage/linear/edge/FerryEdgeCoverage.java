package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.RouteTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for Route=Ferry tag
 *
 * @author pmi
 */
public class FerryEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(FerryEdgeCoverage.class);

    public FerryEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public FerryEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return RouteTag.isFerry(edge);
    }

    @Override
    protected String type()
    {
        return "ferry";
    }
}
