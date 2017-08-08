package org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler;

import static org.openstreetmap.atlas.geography.atlas.items.ItemType.AREA;

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
public class PondHandler extends AbstractWaterHandler
{
    private static final Logger logger = LoggerFactory.getLogger(PondHandler.class);

    public static boolean isPond(final AtlasEntity atlasEntity)
    {
        /*
         * (1) natural == water && water=pond
         */
        if (Validators.isOfType(atlasEntity, NaturalTag.class, NaturalTag.WATER)
                && Validators.isOfType(atlasEntity, WaterTag.class, WaterTag.POND))
        {
            return true;
        }

        /*
         * (2) landuse=pond
         */
        return Validators.isOfType(atlasEntity, LandUseTag.class, LandUseTag.POND);
    }

    @Override
    public boolean canHandle(final AtlasEntity entity)
    {
        return isPond(entity);
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
        return WaterType.POND;
    }
}
