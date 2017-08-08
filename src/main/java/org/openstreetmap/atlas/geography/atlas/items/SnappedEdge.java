package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.Snapper.SnappedLocation;

/**
 * {@link SnappedLocation} on an {@link Edge}
 *
 * @author matthieun
 */
public class SnappedEdge extends SnappedLocation
{
    private static final long serialVersionUID = 804405113068154275L;

    private final Edge edge;

    public SnappedEdge(final SnappedLocation snap, final Edge edge)
    {
        super(snap.getOrigin(), snap, edge.asPolyLine());
        this.edge = edge;
    }

    public Edge getEdge()
    {
        return this.edge;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[SnappedEdge: Edge: ");
        builder.append(this.edge.getIdentifier());
        builder.append(", Origin: ");
        builder.append(getOrigin());
        builder.append(", Snap: ");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
}
