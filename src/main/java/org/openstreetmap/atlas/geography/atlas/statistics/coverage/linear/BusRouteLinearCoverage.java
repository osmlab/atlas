package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class BusRouteLinearCoverage extends LinearCoverage<LineItem>
{
    private static final Logger logger = LoggerFactory.getLogger(BusRouteLinearCoverage.class);

    private static final StringList RELATION_TYPE_MATCHES = new StringList(
            new String[] { "route" });
    private static final StringList RELATION_ROUTE_MATCHES = new StringList(new String[] { "bus" });

    public BusRouteLinearCoverage(final Atlas atlas)
    {
        super(logger, atlas);
    }

    public BusRouteLinearCoverage(final Atlas atlas, final Predicate<LineItem> filter)
    {
        super(logger, atlas, filter);
    }

    @Override
    protected Iterable<LineItem> getEntities()
    {
        return getAtlas().lineItems();
    }

    @Override
    protected Set<String> getKeys(final LineItem item)
    {
        return new HashSet<>();
    }

    @Override
    protected boolean isCounted(final LineItem item)
    {
        for (final Relation relation : item.relations())
        {
            if (relation.containsValue("type", RELATION_TYPE_MATCHES)
                    && relation.containsValue("route", RELATION_ROUTE_MATCHES))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String subType()
    {
        return "true";
    }

    @Override
    protected String type()
    {
        return "transit_bus_length";
    }

}
