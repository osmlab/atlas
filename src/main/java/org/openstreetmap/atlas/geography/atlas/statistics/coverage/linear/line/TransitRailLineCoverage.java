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
public class TransitRailLineCoverage extends LineCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(TransitRailLineCoverage.class);

    private static final StringList RAILWAY_MATCHES = new StringList(
            new String[] { "funicular", "light_rail", "monorail", "subway", "tram" });

    public TransitRailLineCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public TransitRailLineCoverage(final Atlas atlas, final Predicate<Line> filter)
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
        return "transit_rail_length";
    }
}
