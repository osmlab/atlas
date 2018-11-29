package org.openstreetmap.atlas.geography.atlas.bloated;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Simple interface for all the Bloated entities. As each one extends its parent class already
 * (Node, Edge, Area, ...) this cannot be an abstract class.
 *
 * @author matthieun
 */
public interface BloatedEntity
{
    static AtlasEntity from(final AtlasEntity reference)
    {
        final ItemType type = reference.getType();
        switch (type)
        {
            case NODE:
                return BloatedNode.from((Node) reference);
            case EDGE:
                return BloatedEdge.from((Edge) reference);
            case AREA:
                return BloatedArea.from((Area) reference);
            case LINE:
                return BloatedLine.from((Line) reference);
            case POINT:
                return BloatedPoint.from((Point) reference);
            case RELATION:
                return BloatedRelation.from((Relation) reference);
            default:
                throw new CoreException("Unknown ItemType {}", type);
        }
    }

    static AtlasEntity shallowFrom(final AtlasEntity reference)
    {
        final ItemType type = reference.getType();
        switch (type)
        {
            case NODE:
                return BloatedNode.shallowFrom((Node) reference);
            case EDGE:
                return BloatedEdge.shallowFrom((Edge) reference);
            case AREA:
                return BloatedArea.shallowFrom((Area) reference);
            case LINE:
                return BloatedLine.shallowFrom((Line) reference);
            case POINT:
                return BloatedPoint.shallowFrom((Point) reference);
            case RELATION:
                return BloatedRelation.shallowFrom((Relation) reference);
            default:
                throw new CoreException("Unknown ItemType {}", type);
        }
    }

    long getIdentifier();

    /**
     * @return True when that entity contains only its identifier as effective data.
     */
    boolean isSuperShallow();
}
