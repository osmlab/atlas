package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Node} that references a {@link ChangeAtlas}. That {@link Node} makes sure that all the
 * connected {@link Edge}s are {@link ChangeEdge}s, and that all the parent {@link Relation}s are
 * {@link ChangeRelation}s.
 * <p>
 * NOSONAR here to avoid "Subclasses that add fields should override "equals" (squid:S2160)". Here
 * the equals from the parent works.
 *
 * @author matthieun
 */
public class ChangeNode extends Node // NOSONAR
{
    private static final long serialVersionUID = 4353679260691518275L;

    private final Node source;
    private final Node override;

    // Computing Parent Relations is very expensive, so we cache it here.
    private transient Set<Relation> relationsCache;
    private transient Object relationsCacheLock = new Object();

    // Computing In Edges is very expensive, so we cache it here.
    private transient SortedSet<Edge> inEdgesCache;
    private transient Object inEdgesCacheLock = new Object();

    // Computing Out Edges is very expensive, so we cache it here.
    private transient SortedSet<Edge> outEdgesCache;
    private transient Object outEdgesCacheLock = new Object();

    protected ChangeNode(final ChangeAtlas atlas, final Node source, final Node override)
    {
        super(atlas);
        this.source = source;
        this.override = override;
    }

    @Override
    public long getIdentifier()
    {
        return attribute(Node::getIdentifier, "identifier");
    }

    @Override
    public Location getLocation()
    {
        return attribute(Node::getLocation, "location");
    }

    @Override
    public Map<String, String> getTags()
    {
        return attribute(Node::getTags, "tags");
    }

    public SortedSet<Long> inEdgeIdentifiers()
    {
        return attribute(Node::inEdges, "in edges").stream().map(Edge::getIdentifier)
                .filter(edgeIdentifier -> getChangeAtlas().edge(edgeIdentifier) != null)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        final Supplier<SortedSet<Edge>> creator = () -> inEdgeIdentifiers().stream()
                .map(edgeIdentifier -> getChangeAtlas().edge(edgeIdentifier))
                .collect(Collectors.toCollection(TreeSet::new));

        return ChangeEntity.getOrCreateCache(this.inEdgesCache, cache -> this.inEdgesCache = cache,
                this.inEdgesCacheLock, creator);
    }

    public SortedSet<Long> outEdgeIdentifiers()
    {
        return attribute(Node::outEdges, "out edges").stream().map(Edge::getIdentifier)
                .filter(edgeIdentifier -> getChangeAtlas().edge(edgeIdentifier) != null)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        final Supplier<SortedSet<Edge>> creator = () -> outEdgeIdentifiers().stream()
                .map(edgeIdentifier -> getChangeAtlas().edge(edgeIdentifier))
                .collect(Collectors.toCollection(TreeSet::new));

        return ChangeEntity.getOrCreateCache(this.outEdgesCache,
                cache -> this.outEdgesCache = cache, this.outEdgesCacheLock, creator);
    }

    @Override
    public Set<Relation> relations()
    {
        final Supplier<Set<Relation>> creator = () -> ChangeEntity
                .filterRelations(attribute(AtlasEntity::relations, "relations"), getChangeAtlas());
        return ChangeEntity.getOrCreateCache(this.relationsCache,
                cache -> this.relationsCache = cache, this.relationsCacheLock, creator);
    }

    private <T extends Object> T attribute(final Function<Node, T> memberExtractor,
            final String name)
    {
        return ChangeEntity.getAttributeOrBackup(this.source, this.override, memberExtractor, name);
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }
}
