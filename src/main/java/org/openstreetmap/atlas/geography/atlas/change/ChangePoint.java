package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author matthieun
 */
public class ChangePoint extends Point // NOSONAR
{
    private static final long serialVersionUID = 4353679260691518275L;

    private final Point source;
    private final Point override;

    protected ChangePoint(final ChangeAtlas atlas, final Point source, final Point override)
    {
        super(atlas);
        this.source = source;
        this.override = override;
    }

    @Override
    public long getIdentifier()
    {
        return attribute(Point::getIdentifier);
    }

    @Override
    public Location getLocation()
    {
        return attribute(Point::getLocation);
    }

    @Override
    public Map<String, String> getTags()
    {
        return attribute(Point::getTags);
    }

    @Override
    public Set<Relation> relations()
    {
        return attribute(Point::relations).stream()
                .map(relation -> getChangeAtlas().relation(relation.getIdentifier()))
                .collect(Collectors.toSet());
    }

    private <T extends Object> T attribute(final Function<Point, T> memberExtractor)
    {
        return ChangeEntity.getAttributeOrBackup(this.source, this.override, memberExtractor);
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }
}
