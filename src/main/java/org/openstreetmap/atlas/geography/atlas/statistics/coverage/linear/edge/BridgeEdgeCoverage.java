package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for the Bridge tag
 *
 * @author pmi
 */
public class BridgeEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(BridgeEdgeCoverage.class);

    public BridgeEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public BridgeEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return BridgeTag.isBridge(edge);
    }

    @Override
    protected String type()
    {
        return BridgeTag.KEY;
    }
}
