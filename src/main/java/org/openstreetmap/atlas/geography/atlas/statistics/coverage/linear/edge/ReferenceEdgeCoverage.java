package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Edge Coverage for route shield related tags
 *
 * @author pmi
 */
public class ReferenceEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(ReferenceEdgeCoverage.class);

    private static final StringList REF_KEYS = new StringList(new String[] { "ref", "int_ref" });

    public ReferenceEdgeCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public ReferenceEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    // If any of the possible ref keys are present, count the edge
    @Override
    protected boolean isCounted(final Edge edge)
    {
        return edge.containsKey(REF_KEYS);
    }

    @Override
    protected String type()
    {
        return "length_roads_refs";
    }

}
