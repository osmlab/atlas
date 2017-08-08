package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.TollTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Edge Coverage for roads with toll = yes
 *
 * @author pmi
 */

public class TollEdgeCoverage extends EdgeCoverage
{

    private static final Logger logger = LoggerFactory.getLogger(TollEdgeCoverage.class);

    public TollEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public TollEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return TollTag.isYes(edge);
    }

    @Override
    protected String type()
    {
        return "length_toll_roads";
    }

}
