package org.openstreetmap.atlas.geography.atlas.items.complex.water.finder;

import org.openstreetmap.atlas.geography.atlas.items.complex.WaterIslandConfigurationReader;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.WaterType;
import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * Class which ties a water configuration reader to {@link WaterType}
 * 
 * @param <T>
 * @author sbhalekar
 */
public abstract class WaterConfigurationReader<T> extends WaterIslandConfigurationReader<T>
{
    private final WaterType waterType;

    public WaterConfigurationReader(final Resource configurationResource, final WaterType waterType)
    {
        super(configurationResource);
        this.waterType = waterType;
    }

    protected WaterType getWaterBodyType()
    {
        return this.waterType;
    }
}
