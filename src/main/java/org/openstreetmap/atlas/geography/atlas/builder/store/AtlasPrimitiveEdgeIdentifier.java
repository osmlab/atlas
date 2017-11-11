package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.io.Serializable;

/**
 * @author Sid
 */
public class AtlasPrimitiveEdgeIdentifier implements Serializable
{
    private static final long serialVersionUID = 6082277761179021192L;

    private long identifier;

    public static AtlasPrimitiveEdgeIdentifier from(final AtlasPrimitiveEdge atlasPrimitiveEdge)
    {
        return new AtlasPrimitiveEdgeIdentifier(atlasPrimitiveEdge.getIdentifier());
    }

    public AtlasPrimitiveEdgeIdentifier()
    {
    }

    public AtlasPrimitiveEdgeIdentifier(final long identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(final Object other)
    {
        return other instanceof AtlasPrimitiveEdgeIdentifier
                && this.getIdentifier() == ((AtlasPrimitiveEdgeIdentifier) other).getIdentifier();
    }

    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(this.identifier);
    }

    public void setIdentifier(final long identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public String toString()
    {
        return "AtlasPrimitiveEdgeIdentifier [identifier=" + this.identifier + "]";
    }
}
