package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.line;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class RiverLineCoverage extends LineCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(RiverLineCoverage.class);

    public static final StringList WATERWAY_MATCHES = new StringList(
            new String[] { "river", "stream", "wadi", "canal" });
    public static final StringList NATURAL_MATCHES = new StringList(new String[] { "water" });
    public static final StringList WATER_MATCHES = new StringList(
            new String[] { "river", "canal" });

    public RiverLineCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public RiverLineCoverage(final Atlas atlas, final Predicate<Line> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Line item)
    {
        return item.containsValue("waterway", WATERWAY_MATCHES)
                || item.containsValue("natural", NATURAL_MATCHES)
                        && item.containsValue("water", WATER_MATCHES);
    }

    @Override
    protected String type()
    {
        return "river_length";
    }
}
