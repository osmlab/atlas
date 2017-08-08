package org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler;

import static org.openstreetmap.atlas.geography.atlas.items.ItemType.AREA;
import static org.openstreetmap.atlas.geography.atlas.items.ItemType.RELATION;

import java.util.Arrays;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType;
import org.openstreetmap.atlas.tags.LandUseTag;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.WaterTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sid
 */
public class ReservoirHandler extends AbstractWaterHandler
{
    private static final Logger logger = LoggerFactory.getLogger(ReservoirHandler.class);

    public static boolean isReservoir(final AtlasEntity atlasEntity)
    {
        /*
         * (1) natural == water && water == "reservoir"
         */
        if (Validators.isOfType(atlasEntity, NaturalTag.class, NaturalTag.WATER)
                && Validators.isOfType(atlasEntity, WaterTag.class, WaterTag.RESERVOIR))
        {
            return true;
        }

        /*
         * (2) landuse = reservoir
         */
        return Validators.isOfType(atlasEntity, LandUseTag.class, LandUseTag.RESERVOIR);
    }

    @Override
    public boolean canHandle(final AtlasEntity entity)
    {
        return isReservoir(entity);
    }

    @Override
    public List<ItemType> getAllowedTypes()
    {
        return Arrays.asList(AREA, RELATION);
    }

    @Override
    public Logger getLogger()
    {
        return logger;
    }

    @Override
    public WaterType getType()
    {
        return WaterType.RESERVOIR;
    }
}
