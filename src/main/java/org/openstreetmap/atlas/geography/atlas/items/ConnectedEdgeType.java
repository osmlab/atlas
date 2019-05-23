package org.openstreetmap.atlas.geography.atlas.items;

import java.util.SortedSet;
import java.util.function.Function;

/**
 * {@link Node}s have in and out edges, this enum enables generalizing logic around this node and
 * edge connection.
 *
 * @author Yazad Khambata
 */
public enum ConnectedEdgeType
{
    IN("inEdges", Node::inEdges),

    OUT("outEdges", Node::outEdges);

    private String propertyName;

    private Function<Node, SortedSet<Edge>> accessFunction;

    ConnectedEdgeType(final String propertyName, final Function<Node, SortedSet<Edge>> accessFunction)
    {
        this.propertyName = propertyName;
        this.accessFunction = accessFunction;
    }

    public String getPropertyName()
    {
        return this.propertyName;
    }

    public Function<Node, SortedSet<Edge>> getAccessFunction()
    {
        return this.accessFunction;
    }
}
