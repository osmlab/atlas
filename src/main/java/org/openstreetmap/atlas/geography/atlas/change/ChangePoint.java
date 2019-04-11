package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Point} that references a {@link ChangeAtlas}. That {@link Point} makes sure that all the
 * parent {@link Relation}s are {@link ChangeRelation}s.
 * <p>
 * NOSONAR here to avoid "Subclasses that add fields should override "equals" (squid:S2160)". Here
 * the equals from the parent works.
 *
 * @author matthieun
 */
public class ChangePoint extends Point // NOSONAR
{
    private static final long serialVersionUID = 4353679260691518275L;

    private final Point source;
    private final Point override;

    // Computing Parent Relations is very expensive, so we cache it here.
    private transient Set<Relation> relationsCache;
    private transient Object relationsCacheLock = new Object();

    protected ChangePoint(final ChangeAtlas atlas, final Point source, final Point override)
    {
        super(atlas);
        this.source = source;
        this.override = override;
    }

    @Override
    public long getIdentifier()
    {
        return attribute(Point::getIdentifier, "identifier");
    }

    @Override
    public Location getLocation()
    {
        return attribute(Point::getLocation, "location");
    }

    @Override
    public Map<String, String> getTags()
    {
        return attribute(Point::getTags, "tags");
    }

    @Override
    public Set<Relation> relations()
    {
        final Supplier<Set<Relation>> creator = () -> ChangeEntity
                .filterRelations(attribute(AtlasEntity::relations, "relations"), getChangeAtlas());
        return ChangeEntity.getOrCreateCache(this.relationsCache,
                cache -> this.relationsCache = cache, this.relationsCacheLock, creator);
    }

    private <T extends Object> T attribute(final Function<Point, T> memberExtractor,
            final String name)
    {
        return ChangeEntity.getAttributeOrBackup(this.source, this.override, memberExtractor, name);
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }
}
