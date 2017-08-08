package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.AccessTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for roads with Access = No or Access = Private.Does not include Highway = private
 *
 * @author pmi
 */

public class PrivateAccessEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(PrivateAccessEdgeCoverage.class);

    public PrivateAccessEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public PrivateAccessEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return AccessTag.isPrivate(edge);
    }

    @Override
    protected String type()
    {
        return "length_roads_private";
    }
}
