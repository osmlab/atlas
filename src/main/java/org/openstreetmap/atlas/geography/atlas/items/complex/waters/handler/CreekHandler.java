package org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler;

import static org.openstreetmap.atlas.geography.atlas.items.ItemType.AREA;
import static org.openstreetmap.atlas.geography.atlas.items.ItemType.LINE;

import java.util.Arrays;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.WaterType;
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sid
 */
public class CreekHandler extends AbstractWaterHandler
{
    private static final Logger logger = LoggerFactory.getLogger(CreekHandler.class);

    public static boolean isCreek(final AtlasEntity atlasEntity)
    {
        return Validators.isOfType(atlasEntity, WaterwayTag.class, WaterwayTag.STREAM);
    }

    @Override
    public boolean canHandle(final AtlasEntity entity)
    {
        return isCreek(entity);
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
        return WaterType.CREEK;
    }
}
