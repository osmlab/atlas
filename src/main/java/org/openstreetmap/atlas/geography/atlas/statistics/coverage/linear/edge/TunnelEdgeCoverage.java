package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.TunnelTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for the Tunnel tag
 *
 * @author pmi
 */
public class TunnelEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(TunnelEdgeCoverage.class);

    public TunnelEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public TunnelEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return TunnelTag.isTunnel(edge);
    }

    @Override
    protected String type()
    {
        return TunnelTag.KEY;
    }
}
