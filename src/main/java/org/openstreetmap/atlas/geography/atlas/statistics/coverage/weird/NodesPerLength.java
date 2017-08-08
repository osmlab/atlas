package org.openstreetmap.atlas.geography.atlas.statistics.coverage.weird;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.Coverage;
import org.slf4j.Logger;

/**
 * @author matthieun
 */
public class NodesPerLength extends Coverage<Edge>
{
    public NodesPerLength(final Logger logger, final Atlas atlas)
    {
        super(logger, atlas);
    }

    public NodesPerLength(final Logger logger, final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected CoverageType coverageType()
    {
        // This is a hack to not have to re-code the whole system. Here the distance measure will be
        // the number of points per km, in each edge.
        return CoverageType.DISTANCE;
    }

    @Override
    protected Iterable<Edge> getEntities()
    {
        return getAtlas().edges();
    }

    @Override
    protected Set<String> getKeys(final Edge item)
    {
        // Only "All" will show up.
        return new HashSet<>();
    }

    @Override
    protected String getUnit()
    {
        return "Nodes per km";
    }

    @Override
    protected double getValue(final Edge item)
    {
        return item.asPolyLine().size() / item.length().asKilometers();
    }

    @Override
    protected boolean isCounted(final Edge item)
    {
        return true;
    }

    @Override
    protected String subType()
    {
        return "true";
    }

    @Override
    protected String type()
    {
        return "nodes_per_km";
    }
}
