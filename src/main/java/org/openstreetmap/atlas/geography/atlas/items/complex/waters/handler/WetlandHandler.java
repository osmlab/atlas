package org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler;

import static org.openstreetmap.atlas.geography.atlas.items.ItemType.AREA;
import static org.openstreetmap.atlas.geography.atlas.items.ItemType.LINE;

import java.util.Arrays;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.WetlandTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sid
 */
public class WetlandHandler extends AbstractWaterHandler
{

    private static final Logger logger = LoggerFactory.getLogger(WetlandHandler.class);

    /*
     * natural=wetland & wetland=swamps, marshes, reedbed
     */
    public static boolean isWetland(final AtlasEntity atlasEntity)
    {
        return Validators.isOfType(atlasEntity, NaturalTag.class, NaturalTag.WETLAND)
                && Validators.isOfType(atlasEntity, WetlandTag.class, WetlandTag.SWAMP,
                        WetlandTag.MARSH, WetlandTag.REEDBED);
    }

    @Override
    public boolean canHandle(final AtlasEntity entity)
    {
        return isWetland(entity);
    }

    @Override
    public List<ItemType> getAllowedTypes()
    {
        return Arrays.asList(AREA, LINE);
    }

    @Override
    public Logger getLogger()
    {
        return WetlandHandler.logger;
    }

    @Override
    public WaterType getType()
    {
        return WaterType.WETLAND;
    }
}
