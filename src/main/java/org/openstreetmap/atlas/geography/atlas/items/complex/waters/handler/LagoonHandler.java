package org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler;

import static org.openstreetmap.atlas.geography.atlas.items.ItemType.AREA;
import static org.openstreetmap.atlas.geography.atlas.items.ItemType.LINE;

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
 * @author Sid
 */
public class LagoonHandler extends AbstractWaterHandler
{
    private static final Logger logger = LoggerFactory.getLogger(LagoonHandler.class);

    public static boolean isLagoon(final AtlasEntity atlasEntity)
    {
        return Validators.isOfType(atlasEntity, NaturalTag.class, NaturalTag.WATER)
                && Validators.isOfType(atlasEntity, WaterTag.class, WaterTag.LAGOON);
    }

    @Override
    public boolean canHandle(final AtlasEntity entity)
    {
        return isLagoon(entity);
    }

    @Override
    public List<ItemType> getAllowedTypes()
    {
        return Arrays.asList(AREA, LINE);
    }

    @Override
    public Logger getLogger()
    {
        return logger;
    }

    @Override
    public WaterType getType()
    {
        return WaterType.LAGOON;
    }
}
