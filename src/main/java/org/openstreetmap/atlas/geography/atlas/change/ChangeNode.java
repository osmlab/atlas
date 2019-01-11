package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
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

    protected ChangeNode(final ChangeAtlas atlas, final Node source, final Node override)
    {
        super(atlas);
        this.source = source;
        this.override = override;
    }

    @Override
    public long getIdentifier()
    {
        return attribute(Node::getIdentifier);
    }

    @Override
    public Location getLocation()
    {
        return attribute(Node::getLocation);
    }

    @Override
    public Map<String, String> getTags()
    {
        return attribute(Node::getTags);
    }

    public SortedSet<Long> inEdgeIdentifiers()
    {
        return attribute(Node::inEdges).stream().map(Edge::getIdentifier)
                .filter(edgeIdentifier -> getChangeAtlas().edge(edgeIdentifier) != null)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        return inEdgeIdentifiers().stream()
                .map(edgeIdentifier -> getChangeAtlas().edge(edgeIdentifier))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public SortedSet<Long> outEdgeIdentifiers()
    {
        return attribute(Node::outEdges).stream().map(Edge::getIdentifier)
                .filter(edgeIdentifier -> getChangeAtlas().edge(edgeIdentifier) != null)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        return outEdgeIdentifiers().stream()
                .map(edgeIdentifier -> getChangeAtlas().edge(edgeIdentifier))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Set<Relation> relations()
    {
        return ChangeEntity.filterRelations(attribute(AtlasEntity::relations), getChangeAtlas());
    }

    private <T extends Object> T attribute(final Function<Node, T> memberExtractor)
    {
        return ChangeEntity.getAttributeOrBackup(this.source, this.override, memberExtractor);
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }
}
