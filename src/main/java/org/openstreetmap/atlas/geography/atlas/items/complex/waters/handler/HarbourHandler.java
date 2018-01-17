package org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler;

import static org.openstreetmap.atlas.geography.atlas.items.ItemType.AREA;
import static org.openstreetmap.atlas.geography.atlas.items.ItemType.RELATION;

import java.util.Arrays;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType;
import org.openstreetmap.atlas.tags.HarbourTag;
import org.openstreetmap.atlas.tags.IndustrialTag;
import org.openstreetmap.atlas.tags.LandUseTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Harbor/harbour water body handler.
 *
 * @author mgostintsev
 */
public class HarbourHandler extends AbstractWaterHandler
{
    private static final Logger logger = LoggerFactory.getLogger(HarbourHandler.class);

    public static boolean isHarbour(final AtlasEntity entity)
    {
        // Excluding industrial landuse, ports and shipyards, as those are often used to tag
        // non-water geometry.
        return Validators.isOfType(entity, HarbourTag.class, HarbourTag.YES)
                && !Validators.isOfType(entity, LandUseTag.class, LandUseTag.INDUSTRIAL,
                        LandUseTag.PORT)
                && !Validators.isOfType(entity, IndustrialTag.class, IndustrialTag.PORT,
                        IndustrialTag.SHIPYARD);
    }

    @Override
    public boolean canHandle(final AtlasEntity entity)
    {
        return isHarbour(entity);
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
        return WaterType.HARBOUR;
    }
}
