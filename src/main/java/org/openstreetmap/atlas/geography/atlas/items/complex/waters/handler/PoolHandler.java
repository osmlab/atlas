package org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler;

import static org.openstreetmap.atlas.geography.atlas.items.ItemType.AREA;

import java.util.Arrays;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.WaterTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These aren't private swimming pools. natural=water water=reflecting_pool
 *
 * @author Sid
 */
public class PoolHandler extends AbstractWaterHandler
{
    private static final Logger logger = LoggerFactory.getLogger(PoolHandler.class);

    public static boolean isPool(final AtlasEntity atlasEntity)
    {
        /*
         * natural=water water=reflecting_pool. It is safe to exclude private swimming pools
         * (leisure=swimming_pool)
         */
        return Validators.isOfType(atlasEntity, NaturalTag.class, NaturalTag.WATER)
                && Validators.isOfType(atlasEntity, WaterTag.class, WaterTag.POOL);
    }

    @Override
    public boolean canHandle(final AtlasEntity entity)
    {
        return isPool(entity);
    }

    @Override
    public List<ItemType> getAllowedTypes()
    {
        return Arrays.asList(AREA);
    }

    @Override
    public Logger getLogger()
    {
        return logger;
    }

    @Override
    public WaterType getType()
    {
        return WaterType.POOL;
    }
}
