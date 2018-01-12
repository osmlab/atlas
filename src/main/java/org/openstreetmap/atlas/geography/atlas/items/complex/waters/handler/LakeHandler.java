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
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sid
 */
public class LakeHandler extends AbstractWaterHandler
{
    private static final Logger logger = LoggerFactory.getLogger(LakeHandler.class);

    public static boolean isLake(final AtlasEntity entity)
    {
        /*
         * If area (1) natural == water (2) natural == water && water == "lake"
         */
        if (Validators.isOfType(entity, NaturalTag.class, NaturalTag.WATER))
        {
            if (Validators.isOfType(entity, WaterTag.class, WaterTag.LAKE, WaterTag.OXBOW))
            {
                return true;
            }
            // We have some rivers where no water tag is specified, but waterway is present
            if (entity.getTag(WaterwayTag.KEY).isPresent()
                    || entity.getTag(LandUseTag.KEY).isPresent())
            {
                return false;
            }
            // Other water bodies, such as harbors, may have natural=water, but no water tag. We
            // want to exclude these to avoid creating duplicate water bodies.
            if (HarbourHandler.isHarbour(entity))
            {
                return false;
            }
            // According to OSM : A water body is by default a lake, when no water tag is specified
            return !entity.getTag(WaterTag.KEY).isPresent();
        }
        return false;
    }

    @Override
    public boolean canHandle(final AtlasEntity entity)
    {
        return isLake(entity);
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
        return WaterType.LAKE;
    }
}
