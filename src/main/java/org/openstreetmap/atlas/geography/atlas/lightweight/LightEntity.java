package org.openstreetmap.atlas.geography.atlas.lightweight;

import javax.annotation.Nullable;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * A lightweight Atlas entity. In this case, light weight refers to size in memory. These are
 * primarily useful for raw identifier uses. First generation lightweight entities may also have
 * geometry.
 *
 * @param <C>
 *            The primitive type
 * @author Taylor Smock
 */
public interface LightEntity<C extends LightEntity<C>>
{
    /** A common empty long array to avoid additional empty arrays (memory) */
    long[] EMPTY_LONG_ARRAY = {};
    /** A common empty location array to avoid additional empty arrays (memory) */
    Location[] EMPTY_LOCATION_ARRAY = {};

    /**
     * Create a {@link LightEntity} from a given {@link AtlasEntity} reference. The
     * {@link LightEntity}'s fields will match the fields of the reference. The returned
     * {@link LightEntity} may or may not be full, i.e. some of its associated fields will be
     * {@code null}. Currently no tags are saved in light entities. The <i>only</i> items guaranteed
     * to exist is the id of the entity. First generation entities also have geometry, but any
     * second generation entities do not. For example, the {@link AtlasEntity#relations()} method
     * only returns {@link LightRelation}s with an identifier and no geometry. For this reason,
     * {@link LightEntity}s should only be used with code that expects {@link LightEntity}s.
     *
     * @param reference
     *            the reference to copy
     * @return the full entity
     */
    static AtlasEntity from(final AtlasEntity reference)
    {
        final ItemType type = reference.getType();
        switch (type)
        {
            case NODE:
                return LightNode.from((Node) reference);
            case EDGE:
                return LightEdge.from((Edge) reference);
            case AREA:
                return LightArea.from((Area) reference);
            case LINE:
                return LightLine.from((Line) reference);
            case POINT:
                return LightPoint.from((Point) reference);
            case RELATION:
                return LightRelation.from((Relation) reference);
            default:
                throw new CoreException("Unknown ItemType {}", type);
        }
    }

    /**
     * Get the geometry of the entity
     *
     * @return The geometry
     */
    @Nullable
    Iterable<Location> getGeometry();

    /**
     * Get relation identifiers
     *
     * @return The relation identifiers
     */
    long[] getRelationIdentifiers();
}
