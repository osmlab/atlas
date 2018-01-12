package org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler;

import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterbody;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.ComplexWaterway;
import org.slf4j.Logger;

/**
 * @author Sid
 */
public abstract class AbstractWaterHandler implements WaterHandler
{
    public abstract List<ItemType> getAllowedTypes();

    public abstract Logger getLogger();

    @Override
    public Optional<ComplexWaterEntity> handle(final AtlasEntity entity)
    {
        if (canHandle(entity) && getAllowedTypes().contains(entity.getType()))
        {
            return handleType(entity);
        }
        return Optional.empty();
    }

    public Optional<ComplexWaterEntity> handleType(final AtlasEntity entity)
    {
        try
        {
            if (entity instanceof Relation || entity instanceof Area)
            {
                final ComplexWaterbody complexWaterbody = new ComplexWaterbody(entity, getType());
                return Optional.of(complexWaterbody);
            }
            /*
             * TODO We currently don't process waterway relations. So if it is a way and it is part
             * of relation where it is a side stream, main stream or tributary, we process them.
             */
            else if (entity instanceof Line)
            {
                final ComplexWaterway complexWaterway = new ComplexWaterway(entity, getType());
                return Optional.of(complexWaterway);
            }
        }
        catch (final Exception e)
        {
            final String msg = "Skipping entity : " + entity;
            getLogger().warn(msg, e);
        }
        return Optional.empty();
    }

}
