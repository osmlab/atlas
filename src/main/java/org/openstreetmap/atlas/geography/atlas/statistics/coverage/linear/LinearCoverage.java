package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.Coverage;
import org.slf4j.Logger;

/**
 * @param <T>
 *            The type of {@link LineItem}
 * @author matthieun
 */
public abstract class LinearCoverage<T extends LineItem> extends Coverage<T>
{
    public LinearCoverage(final Logger logger, final Atlas atlas)
    {
        super(logger, atlas);
    }

    public LinearCoverage(final Logger logger, final Atlas atlas, final Predicate<T> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected CoverageType coverageType()
    {
        return CoverageType.DISTANCE;
    }

    @Override
    protected String getUnit()
    {
        return "kilometers";
    }

    @Override
    protected double getValue(final T item)
    {
        return item.asPolyLine().length().asKilometers();
    }
}
