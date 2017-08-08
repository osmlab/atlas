package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.Map;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coverage for the name tag
 *
 * @author matthieun
 */
public class NameEdgeCoverage extends EdgeCoverage
{
    private static final Logger logger = LoggerFactory.getLogger(NameEdgeCoverage.class);

    private static final StringList EXACT_MATCHES = new StringList(new String[] { "name", "name_1",
            "alt_name", "int_name", "loc_name", "nat_name", "old_name", "reg_name", "short_name",
            "official_name", "name:left", "name:right", "ref" });
    private static final StringList START_WITH_MATCHES = new StringList(
            new String[] { "name:", "alt_name:", "old_name:", "alt_name_" });
    private static final StringList RELATION_EXACT_MATCHES = new StringList(
            new String[] { "name", "ref" });
    private static final StringList RELATION_START_WITH_MATCHES = new StringList(
            new String[] { "name:", "ref:" });

    private final String type;

    public NameEdgeCoverage(final Atlas atlas, final Predicate<Edge> filter, final String type)
    {
        super(logger, atlas, filter);
        this.type = type;
    }

    public NameEdgeCoverage(final Atlas atlas, final String type)
    {
        super(logger, atlas);
        this.type = type;
    }

    @Override
    protected boolean isCounted(final Edge edge)
    {
        if (edge.containsKey(EXACT_MATCHES))
        {
            return true;
        }
        if (edge.containsKeyStartsWith(START_WITH_MATCHES))
        {
            return true;
        }
        for (final Relation relation : edge.relations())
        {
            final Map<String, String> tags = relation.getTags();
            if (tags.containsKey("type") && "route".equals(tags.get("type"))
                    && tags.containsKey("route") && "road".equals(tags.get("route")))
            {
                if (relation.containsKey(RELATION_EXACT_MATCHES))
                {
                    return true;
                }
                if (relation.containsKeyStartsWith(RELATION_START_WITH_MATCHES))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected String type()
    {
        return this.type;
    }
}
