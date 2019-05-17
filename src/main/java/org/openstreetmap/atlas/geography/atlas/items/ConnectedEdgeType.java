package org.openstreetmap.atlas.geography.atlas.items;

import java.util.SortedSet;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@link Node}s have in and out edges, this enum enables generalizing logic around this node and
 * edge connection.
 *
 * @author Yazad Khambata
 */
@AllArgsConstructor
@Getter
public enum ConnectedEdgeType
{
    IN("inEdges", Node::inEdges),

    OUT("outEdges", Node::outEdges);

    private String propertyName;

    private Function<Node, SortedSet<Edge>> accessFunction;
}
