package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.names.NoNameTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for the no_name tag
 *
 * @author matthieun
 */
public class NoNameEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(NoNameEdgeCoverage.class);

    public NoNameEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public NoNameEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return NoNameTag.isNoName(edge);
    }

    @Override
    protected String type()
    {
        return "no_name_roads";
    }
}
