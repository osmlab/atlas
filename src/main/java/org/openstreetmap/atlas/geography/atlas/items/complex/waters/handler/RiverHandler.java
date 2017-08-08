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
public class RiverHandler extends AbstractWaterHandler
{
    private static final Logger logger = LoggerFactory.getLogger(RiverHandler.class);

    public static boolean isRiver(final AtlasEntity atlasEntity)
    {
        /*
         * If (1) waterway=river (2) waterway=riverbank
         */
        if (Validators.isOfType(atlasEntity, WaterwayTag.class, WaterwayTag.RIVER,
                WaterwayTag.RIVERBANK))
        {
            return true;
        }

        /*
         * (3) natural == water && water=river
         */
        return Validators.isOfType(atlasEntity, NaturalTag.class, NaturalTag.WATER)
                && Validators.isOfType(atlasEntity, WaterTag.class, WaterTag.RIVER);
    }

    @Override
    public boolean canHandle(final AtlasEntity entity)
    {
        return isRiver(entity);
    }

    @Override
    public List<ItemType> getAllowedTypes()
    {
        return Arrays.asList(AREA, LINE, RELATION);
    }

    @Override
    public Logger getLogger()
    {
        return RiverHandler.logger;
    }

    @Override
    public WaterType getType()
    {
        return WaterType.RIVER;
    }
}
