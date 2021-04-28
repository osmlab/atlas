package org.openstreetmap.atlas.geography.atlas.items;

import java.util.SortedSet;
import java.util.function.Function;

/**
 * {@link Node}s have in and out edges, this enum enables generalizing logic around this node and
 * edge connection.
 *
 * @author Yazad Khambata
 */
public enum ConnectedEdgeType implements ConnectedEntityType<Node, SortedSet<Edge>>
{
    IN("inEdges", Node::inEdges),

    OUT("outEdges", Node::outEdges);

    private final String propertyName;

    private final Function<Node, SortedSet<Edge>> accessFunction;

    ConnectedEdgeType(final String propertyName,
            final Function<Node, SortedSet<Edge>> accessFunction)
    {
        this.propertyName = propertyName;
        this.accessFunction = accessFunction;
    }

    @Override
    public Function<Node, SortedSet<Edge>> getAccessFunction()
    {
        return this.accessFunction;
    }

    @Override
    public String getPropertyName()
    {
        return this.propertyName;
    }
}
