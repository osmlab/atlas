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
public class RailLineCoverage extends LineCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(RailLineCoverage.class);

    private static final StringList RAILWAY_MATCHES = new StringList(
            new String[] { "narrow_gauge", "rail" });

    public RailLineCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public RailLineCoverage(final Atlas atlas, final Predicate<Line> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Line item)
    {
        return item.containsValue("railway", RAILWAY_MATCHES);
    }

    @Override
    protected String type()
    {
        return "rail_length";
    }
}
