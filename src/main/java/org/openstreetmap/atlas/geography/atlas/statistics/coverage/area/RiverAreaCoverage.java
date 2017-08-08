package org.openstreetmap.atlas.geography.atlas.statistics.coverage.area;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.line.RiverLineCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class RiverAreaCoverage extends AreaCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(RiverAreaCoverage.class);

    public RiverAreaCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public RiverAreaCoverage(final Atlas atlas, final Predicate<Area> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Area item)
    {
        return item.containsValue("waterway", RiverLineCoverage.WATERWAY_MATCHES)
                || item.containsValue("natural", RiverLineCoverage.NATURAL_MATCHES)
                        && item.containsValue("water", RiverLineCoverage.WATER_MATCHES);
    }

    @Override
    protected String subType()
    {
        return "true";
    }

    @Override
    protected String type()
    {
        return "rivers_area";
    }
}
