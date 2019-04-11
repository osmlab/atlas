package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Edge} that references a {@link ChangeAtlas}. That {@link Edge} makes sure that all the
 * connected {@link Node}s are {@link ChangeNode}s, and that all the parent {@link Relation}s are
 * {@link ChangeRelation}s.
 * <p>
 * NOSONAR here to avoid "Subclasses that add fields should override "equals" (squid:S2160)". Here
 * the equals from the parent works.
 *
 * @author matthieun
 */
public class ChangeEdge extends Edge // NOSONAR
{
    private static final long serialVersionUID = -5658471275390043045L;

    // At most one of those two can be null. Not using Optional here as it is not Serializable.
    private final Edge source;
    private final Edge override;

    // Computing Parent Relations is very expensive, so we cache it here.
    private transient Set<Relation> relationsCache;
    private transient Object relationsCacheLock = new Object();

    // Computing Start Node is very expensive, so we cache it here.
    private transient Node startNodeCache;
    private transient Object startNodeCacheLock = new Object();

    // Computing End Node is very expensive, so we cache it here.
    private transient Node endNodeCache;
    private transient Object endNodeCacheLock = new Object();

    protected ChangeEdge(final ChangeAtlas atlas, final Edge source, final Edge override)
    {
        super(atlas);
        this.source = source;
        this.override = override;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return attribute(Edge::asPolyLine, "polyLine");
    }

    @Override
    public Node end()
    {
        final Supplier<Node> creator = () -> getChangeAtlas().node(endNodeIdentifier());
        return ChangeEntity.getOrCreateCache(this.endNodeCache, cache -> this.endNodeCache = cache,
                this.endNodeCacheLock, creator);
    }

    public long endNodeIdentifier()
    {
        return attribute(Edge::end, "end node").getIdentifier();
    }

    @Override
    public long getIdentifier()
    {
        return attribute(Edge::getIdentifier, "identifier");
    }

    @Override
    public Map<String, String> getTags()
    {
        return attribute(Edge::getTags, "tags");
    }

    @Override
    public Set<Relation> relations()
    {
        final Supplier<Set<Relation>> creator = () -> ChangeEntity
                .filterRelations(attribute(AtlasEntity::relations, "relations"), getChangeAtlas());
        return ChangeEntity.getOrCreateCache(this.relationsCache,
                cache -> this.relationsCache = cache, this.relationsCacheLock, creator);
    }

    @Override
    public Node start()
    {
        final Supplier<Node> creator = () -> getChangeAtlas().node(startNodeIdentifier());
        return ChangeEntity.getOrCreateCache(this.startNodeCache,
                cache -> this.startNodeCache = cache, this.startNodeCacheLock, creator);
    }

    public long startNodeIdentifier()
    {
        return attribute(Edge::start, "start node").getIdentifier();
    }

    private <T extends Object> T attribute(final Function<Edge, T> memberExtractor,
            final String name)
    {
        return ChangeEntity.getAttributeOrBackup(this.source, this.override, memberExtractor, name);
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }
}
