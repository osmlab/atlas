package org.openstreetmap.atlas.geography.atlas.pbf.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.RailwayTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

/**
 * A wrapper for tags for an OSM entity
 *
 * @author tony
 * @author matthieun
 */
public class TagMap implements Taggable
{
    private final Map<String, String> tags;

    public TagMap(final Collection<Tag> tagCollection)
    {
        this.tags = new HashMap<>();
        tagCollection.forEach(tag -> this.tags.put(tag.getKey(), tag.getValue()));
    }

    public PbfOneWay getOneWay()
    {
        return PbfOneWay.forTag(this);
    }

    @Override
    public Optional<String> getTag(final String key)
    {
        return Optional.ofNullable(this.tags.get(key));
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.tags;
    }

    public boolean hasHighwayTag()
    {
        return HighwayTag.highwayTag(this).isPresent();
    }

    public boolean isEmpty()
    {
        return this.tags.size() == 0;
    }

    public boolean matchAtlasEdge()
    {
        return (matchEdge() || matchFerry() || matchPier()) && getOneWay() != PbfOneWay.CLOSED;
    }

    /**
     * @return {@code true} for ways with core highway tag
     */
    public boolean matchEdge()
    {
        return HighwayTag.isCoreWay(this);
    }

    public boolean matchFerry()
    {
        return RouteTag.isFerry(this);
    }

    public boolean matchPier()
    {
        return ManMadeTag.isPier(this);
    }

    public boolean matchRailway()
    {
        return RailwayTag.isRailway(this);
    }
}
