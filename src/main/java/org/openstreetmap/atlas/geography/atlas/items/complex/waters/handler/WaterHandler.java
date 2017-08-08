package org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler;

import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType;

/**
 * @author Sid
 */
public interface WaterHandler
{
    /**
     * This function checks to see if the entity can be handled by the particular (River, lake, etc)
     * handler
     *
     * @param entity
     *            Atlas entity
     * @return boolean whether the handler can handle this entity
     */
    boolean canHandle(AtlasEntity entity);

    /**
     * This returns the type of WaterBody.
     *
     * @return type of WaterBody(LAKE, RIVER) etc
     */
    WaterType getType();

    /**
     * Main function that returns a waterBody result that contains a feature and an optional list of
     * associatedWaterElements (in case of large water-bodies).
     *
     * @param entity
     *            Atlas entity
     * @return WaterResult
     */
    Optional<ComplexWaterEntity> handle(AtlasEntity entity);
}
