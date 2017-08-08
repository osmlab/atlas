package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.SurfaceTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for the surface tag
 *
 * @author matthieun
 */
public class SurfaceEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(SurfaceEdgeCoverage.class);

    public SurfaceEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public SurfaceEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        return SurfaceTag.get(edge).isPresent();
    }

    @Override
    protected String type()
    {
        return "surface";
    }
}
