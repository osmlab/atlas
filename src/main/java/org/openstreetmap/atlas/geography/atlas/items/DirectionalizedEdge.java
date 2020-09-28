package org.openstreetmap.atlas.geography.atlas.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.tags.MaxSpeedBackwardTag;
import org.openstreetmap.atlas.tags.MaxSpeedForwardTag;
import org.openstreetmap.atlas.tags.MaxSpeedTag;

/**
 * A {@link DirectionalizedEdge} is an {@link Edge} but with the tags interpreted with this
 * {@link Edge}'s direction. For example, if this {@link Edge} is backwards from its OSM way, and
 * the way has a maxspeed:backward tag, here it will be translated into a maxspeed tag. Also the
 * maxspeed:forward tag will be filtered out (it will be used by the reverse edge).
 *
 * @author matthieun
 */
public class DirectionalizedEdge extends Edge
{
    private static final long serialVersionUID = -1165815834787481668L;

    private final Edge source;

    protected DirectionalizedEdge(final Edge source)
    {
        super(source.getAtlas());
        this.source = source;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return this.source.asPolyLine();
    }

    @Override
    public Edge directionalized()
    {
        return this;
    }

    @Override
    public Node end()
    {
        return this.source.end();
    }

    @Override
    public long getIdentifier()
    {
        return this.source.getIdentifier();
    }

    @Override
    public Map<String, String> getTags()
    {
        if (MaxSpeedTag.hasExtendedMaxSpeed(this.source))
        {
            final Map<String, String> tags = new HashMap<>();
            this.source.getTags().forEach((key, value) ->
            {
                if (MaxSpeedForwardTag.KEY.equals(key))
                {
                    if (this.source.isMainEdge())
                    {
                        tags.put(MaxSpeedTag.KEY, value);
                    }
                }
                else if (MaxSpeedBackwardTag.KEY.equals(key))
                {
                    if (!this.source.isMainEdge())
                    {
                        tags.put(MaxSpeedTag.KEY, value);
                    }
                }
                else
                {
                    tags.put(key, value);
                }
            });
            return tags;
        }
        return this.source.getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        return this.source.relations();
    }

    @Override
    public Optional<Edge> reversed()
    {
        final Optional<Edge> reversed = super.reversed();
        if (reversed.isPresent())
        {
            return Optional.of(reversed.get().directionalized());
        }
        else
        {
            return reversed;
        }
    }

    @Override
    public Node start()
    {
        return this.source.start();
    }
}
