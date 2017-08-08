package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.line;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.LinearCoverage;
import org.slf4j.Logger;

/**
 * @author matthieun
 */
public abstract class LineCoverage extends LinearCoverage<Line>
{

    public LineCoverage(final Logger logger, final Atlas atlas)
    {
        super(logger, atlas);
    }

    public LineCoverage(final Logger logger, final Atlas atlas, final Predicate<Line> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected Iterable<Line> getEntities()
    {
        return getAtlas().lines();
    }

    @Override
    protected Set<String> getKeys(final Line item)
    {
        return new HashSet<>();
    }

    @Override
    protected String subType()
    {
        return "true";
    }
}
