package org.openstreetmap.atlas.geography.atlas.items.complex;

import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.islands.ComplexIsland;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.ComplexWaterEntity;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.conversion.Converter;

/**
 * Abstract class to define the configuration reader which will read in a json file and provide an
 * api to use that configuration and generate a {@link ComplexEntity} from {@link AtlasEntity}.
 * Currently this is used to generate {@link ComplexWaterEntity} or {@link ComplexIsland}
 *
 * @param <T>
 *            Type of objects which will map {@link AtlasEntity} to any {@link ComplexEntity}
 * @author sbhalekar
 */
public abstract class WaterIslandConfigurationReader<T>
        implements Converter<AtlasEntity, Optional<? extends ComplexEntity>>
{
    private final T configurationMapper;

    public WaterIslandConfigurationReader(final Resource configurationResource)
    {
        this.configurationMapper = readConfiguration(configurationResource);
    }

    @Override
    public Optional<? extends ComplexEntity> convert(final AtlasEntity atlasEntity)
    {
        return createComplexEntity(atlasEntity);
    }

    /**
     * This method will use the configuration mapper to convert {@link AtlasEntity} into
     * {@link ComplexEntity}
     *
     * @param atlasEntity
     *            {@link AtlasEntity} which needs to be converted
     * @return An optional {@link ComplexEntity}
     */
    @SuppressWarnings("squid:S1452")
    protected abstract Optional<? extends ComplexEntity> createComplexEntity(
            AtlasEntity atlasEntity);

    protected T getConfigurationMapper()
    {
        return this.configurationMapper;
    }

    /**
     * Implementation should read the resource and generate a mapper which will be used to convert
     * an {@link AtlasEntity} to {@link ComplexEntity}
     *
     * @param configurationResource
     *            Resource for the configuration file
     * @return Mapper which would be used for the conversion
     */
    protected abstract T readConfiguration(Resource configurationResource);
}
