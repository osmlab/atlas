package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Used by the {@link MultiNode} to hold multiple versions of identical {@link Node}s. This is in
 * case one of the {@link Node}s has a parent {@link Relation} that was not contained in one of the
 * sub-{@link Atlas}es of the containing {@link MultiAtlas}.
 *
 * @author matthieun
 */
public class SubNodeList implements Iterable<Node>, Serializable
{
    private static final long serialVersionUID = -1413359659676228024L;

    private final List<Node> subNodes;
    private final Node fixNode;

    public SubNodeList(final List<Node> subNodes, final Node fixNode)
    {
        if (subNodes == null)
        {
            throw new CoreException("Cannot have a null list of sub nodes.");
        }
        this.subNodes = subNodes;
        this.fixNode = fixNode;
    }

    public Node getFixNode()
    {
        return this.fixNode;
    }

    public List<Node> getSubNodes()
    {
        return this.subNodes;
    }

    public boolean hasFixNode()
    {
        return this.fixNode != null;
    }

    @Override
    public Iterator<Node> iterator()
    {
        return this.subNodes.iterator();
    }

    public int size()
    {
        return this.subNodes.size() + (hasFixNode() ? 1 : 0);
    }
}
