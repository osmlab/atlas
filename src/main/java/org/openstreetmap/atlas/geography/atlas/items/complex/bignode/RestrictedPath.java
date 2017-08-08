package org.openstreetmap.atlas.geography.atlas.items.complex.bignode;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Route;

/**
 * A turn restriction within a {@link BigNode}. It indicates which {@link BigNode} it belongs to, as
 * well as the {@link Route} path that is forbidden within the {@link BigNode}
 *
 * @author matthieun
 */
public class RestrictedPath implements Located, Serializable
{
    private static final long serialVersionUID = 8003332683119555591L;

    private final Route route;
    private final BigNode parent;

    protected RestrictedPath(final BigNode parent, final Route route)
    {
        this.route = route;
        this.parent = parent;
    }

    @Override
    public Rectangle bounds()
    {
        return this.route.bounds();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof RestrictedPath)
        {
            return this.route.equals(((RestrictedPath) other).getRoute())
                    && this.parent == ((RestrictedPath) other).getParent();
        }
        return false;
    }

    public BigNode getParent()
    {
        return this.parent;
    }

    public Route getRoute()
    {
        return this.route;
    }

    @Override
    public int hashCode()
    {
        return this.route.hashCode();
    }

    @Override
    public String toString()
    {
        return "[BigNode Restricted Path: " + this.route + "]";
    }
}
