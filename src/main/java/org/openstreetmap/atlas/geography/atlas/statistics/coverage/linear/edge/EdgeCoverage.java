package org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.LinearCoverage;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.slf4j.Logger;

/**
 * Highway type separated {@link Edge} coverage
 *
 * @author matthieun
 */
public abstract class EdgeCoverage extends LinearCoverage<Edge>
{
    private static final Comparator<String> KEY_COMPARATOR = (key1, key2) ->
    {
        final Taggable taggable1 = Taggable.with(Maps.hashMap(HighwayTag.KEY, key1));
        final Taggable taggable2 = Taggable.with(Maps.hashMap(HighwayTag.KEY, key2));
        final Optional<HighwayTag> tag1 = HighwayTag.highwayTag(taggable1);
        final Optional<HighwayTag> tag2 = HighwayTag.highwayTag(taggable2);
        if (tag1.isPresent() && tag2.isPresent())
        {
            return tag1.get().compareTo(tag2.get());
        }
        else
        {
            return key1.compareTo(key2);
        }
    };

    public EdgeCoverage(final Logger logger, final Atlas atlas)
    {
        super(logger, atlas);
        this.setKeyComparator(KEY_COMPARATOR);
    }

    public EdgeCoverage(final Logger logger, final Atlas atlas, final Predicate<Edge> filter)
    {
        super(logger, atlas, filter.and(Edge::isMasterEdge));
        this.setKeyComparator(KEY_COMPARATOR);
    }

    @Override
    public String toString()
    {
        return super.toString();
    }

    @Override
    protected Iterable<Edge> getEntities()
    {
        return getAtlas().edges();
    }

    @Override
    protected Set<String> getKeys(final Edge edge)
    {
        final Set<String> result = new HashSet<>();
        result.add(edge.highwayTag().getTagValue());
        return result;
    }

    @Override
    protected String subType()
    {
        return "true";
    }
}
