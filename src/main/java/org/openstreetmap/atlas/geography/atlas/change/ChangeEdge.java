package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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
        return attribute(Edge::asPolyLine);
    }

    @Override
    public Node end()
    {
        return ChangeEntity.getOrCreateCache(fieldCache, lock, supplier);
        Node localEndNode = this.endNodeCache;
        if (localEndNode == null)
        {
            synchronized (this.endNodeCacheLock)
            {
                localEndNode = this.endNodeCache;
                if (localEndNode == null)
                {
                    localEndNode = getChangeAtlas().node(endNodeIdentifier());
                    this.endNodeCache = localEndNode;
                }
            }
        }
        return localEndNode;
    }

    public long endNodeIdentifier()
    {
        return attribute(Edge::end).getIdentifier();
    }

    @Override
    public long getIdentifier()
    {
        return attribute(Edge::getIdentifier);
    }

    @Override
    public Map<String, String> getTags()
    {
        return attribute(Edge::getTags);
    }

    @Override
    public Set<Relation> relations()
    {
        return ChangeEntity.getOrCreateCache(this.relationsCache, this.relationsCacheLock,
                () -> ChangeEntity.filterRelations(attribute(AtlasEntity::relations),
                        getChangeAtlas()));
    }

    @Override
    public Node start()
    {
        return ChangeEntity.getOrCreateCache(fieldCache, lock, supplier);
        Node localStartNode = this.startNodeCache;
        if (localStartNode == null)
        {
            synchronized (this.startNodeCacheLock)
            {
                localStartNode = this.startNodeCache;
                if (localStartNode == null)
                {
                    localStartNode = getChangeAtlas().node(startNodeIdentifier());
                    this.startNodeCache = localStartNode;
                }
            }
        }
        return localStartNode;
    }

    public long startNodeIdentifier()
    {
        return attribute(Edge::start).getIdentifier();
    }

    private <T extends Object> T attribute(final Function<Edge, T> memberExtractor)
    {
        return ChangeEntity.getAttributeOrBackup(this.source, this.override, memberExtractor);
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }
}
