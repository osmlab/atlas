package org.openstreetmap.atlas.geography.atlas.items;

import java.util.function.Function;

/**
 * @author Yazad Khambata
 */
public enum ConnectedNodeType implements ConnectedEntityType<Edge, Node>
{
    START("startNode", Edge::start),

    END("endNode", Edge::end);

    private final String propertyName;

    private final Function<Edge, Node> accessFunction;

    ConnectedNodeType(final String propertyName, final Function<Edge, Node> accessFunction)
    {
        this.propertyName = propertyName;
        this.accessFunction = accessFunction;
    }

    @Override
    public Function<Edge, Node> getAccessFunction()
    {
        return this.accessFunction;
    }

    @Override
    public String getPropertyName()
    {
        return this.propertyName;
    }
}
