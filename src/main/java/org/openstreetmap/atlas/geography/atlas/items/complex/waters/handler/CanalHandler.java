package org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler;

import static org.openstreetmap.atlas.geography.atlas.items.ItemType.AREA;
import static org.openstreetmap.atlas.geography.atlas.items.ItemType.LINE;
import static org.openstreetmap.atlas.geography.atlas.items.ItemType.RELATION;

import java.util.Arrays;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.WaterTag;
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sid
 */
public class CanalHandler extends AbstractWaterHandler
{
    private static final Logger logger = LoggerFactory.getLogger(CanalHandler.class);

    public static boolean isCanal(final AtlasEntity entity)
    {
        /*
         * (1) natural=water and water=canal & waterway=canal
         */
        if (Validators.isOfType(entity, WaterwayTag.class, WaterwayTag.CANAL))
        {
            return true;
        }
        if (Validators.isOfType(entity, NaturalTag.class, NaturalTag.WATER)
                && Validators.isOfType(entity, WaterTag.class, WaterTag.CANAL))
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean canHandle(final AtlasEntity entity)
    {
        return isCanal(entity);
    }

    @Override
    public List<ItemType> getAllowedTypes()
    {
        return Arrays.asList(AREA, LINE, RELATION);
    }

    @Override
    public Logger getLogger()
    {
        return logger;
    }

    @Override
    public WaterType getType()
    {
        return WaterType.CANAL;
    }
}
