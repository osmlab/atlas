package org.openstreetmap.atlas.geography.atlas.statistics.coverage.area;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class LakeAreaCoverage extends AreaCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(LakeAreaCoverage.class);

    private static final StringList NATURAL_MATCHES = new StringList(new String[] { "water" });
    private static final StringList WATER_MATCHES = new StringList(
            new String[] { "lake", "pond", "reflecting_pool", "reservoir" });
    private static final StringList LANDUSE_MATCHES = new StringList(new String[] { "basin" });

    public LakeAreaCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public LakeAreaCoverage(final Atlas atlas, final Predicate<Area> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected boolean isCounted(final Area item)
    {
        return item.containsValue("natural", NATURAL_MATCHES)
                && (item.containsValue("water", WATER_MATCHES) || item.tag("water") == null)
                || item.containsValue("landuse", LANDUSE_MATCHES);
    }

    @Override
    protected String subType()
    {
        return "true";
    }

    @Override
    protected String type()
    {
        return "lakes_area";
    }
}
