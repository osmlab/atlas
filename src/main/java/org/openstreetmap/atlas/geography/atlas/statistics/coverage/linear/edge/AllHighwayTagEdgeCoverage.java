package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AllHighwayTagEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(AllHighwayTagEdgeCoverage.class);

    private final String type;

    public AllHighwayTagEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter,
            final String type)
    {
        super(logger, atlas, filter);
        this.type = type;
    }

    public AllHighwayTagEdgeCoverage(final Atlas atlas, final String type)
    {
        super(logger, atlas);
        this.type = type;
    }

    @Override
    protected boolean isCounted(final Edge item)
    {
        return true;
    }

    @Override
    protected String type()
    {
        return this.type;
    }
}
