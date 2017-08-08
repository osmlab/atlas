package org.openstreetmap.atlas.geography.atlas.statistics.coverage.area;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.Coverage;
import org.slf4j.Logger;

/**
 * @author matthieun
 */
public abstract class AreaCoverage extends Coverage<Area>
{
    public AreaCoverage(final Logger logger, final Atlas atlas)
    {
        super(logger, atlas);
    }

    public AreaCoverage(final Logger logger, final Atlas atlas, final Predicate<Area> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected CoverageType coverageType()
    {
        return CoverageType.DISTANCE;
    }

    @Override
    protected Iterable<Area> getEntities()
    {
        return getAtlas().areas();
    }

    @Override
    protected Set<String> getKeys(final Area area)
    {
        return new HashSet<>();
    }

    @Override
    protected String getUnit()
    {
        return "kilometer squared";
    }

    @Override
    protected double getValue(final Area area)
    {
        return area.asPolygon().surface().asKilometerSquared();
    }
}
