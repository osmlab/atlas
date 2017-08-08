package org.openstreetmap.atlas.geography.atlas.inspection;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * This interface provides default implementations for common types of entity classification. It's
 * unlikely that implementations of this interface would need to override the default behaviour,
 * though still an obvious option.
 *
 * @author brian_l_davis
 */
public interface EntityClassifier
{
    /**
     * Checks if the feature is an edge
     *
     * @param entity
     *            The entity to check in
     * @return True if it's a edge
     */
    default boolean isEdge(final AtlasEntity entity)
    {
        return entity instanceof Edge;
    }

    /**
     * Checks if the feature is a line
     *
     * @param entity
     *            The entity to check in
     * @return True if it's a line
     */
    default boolean isLine(final AtlasEntity entity)
    {
        return entity instanceof Line;
    }

    /**
     * Checks if the feature is a road
     *
     * @param entity
     *            The entity to check in
     * @return True if it's a road
     */
    default boolean isRoad(final AtlasEntity entity)
    {
        return isEdge(entity) || isLine(entity) && Validators.isOfType(entity, HighwayTag.class,
                HighwayTag.PROPOSED, HighwayTag.CONSTRUCTION);
    }
}
