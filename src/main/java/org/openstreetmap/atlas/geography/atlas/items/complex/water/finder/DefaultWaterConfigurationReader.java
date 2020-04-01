package org.openstreetmap.atlas.geography.atlas.items.complex.water.finder;

import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.ComplexWaterbody;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.ComplexWaterway;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.WaterType;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.configuration.ConfiguredFilter;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

/**
 * Configuration reader which would read a configuration file and only convert the default
 * configuration filter into a {@link ConfiguredFilter}
 *
 * @author sbhalekar
 */
public class DefaultWaterConfigurationReader extends WaterConfigurationReader<ConfiguredFilter>
{
    public DefaultWaterConfigurationReader(final Resource resource, final WaterType waterType)
    {
        super(resource, waterType);
    }

    public DefaultWaterConfigurationReader(final String resourceFileName, final WaterType waterType)
    {
        this(new InputStreamResource(
                () -> DefaultWaterConfigurationReader.class.getResourceAsStream(resourceFileName)),
                waterType);
    }

    @Override
    public ConfiguredFilter readConfiguration(final Resource configurationResource)
    {
        return ConfiguredFilter.getDefaultFilter(new StandardConfiguration(configurationResource));
    }

    @Override
    protected Optional<ComplexWaterEntity> createComplexEntity(final AtlasEntity atlasEntity)
    {
        final WaterType waterBodyType = this.getWaterBodyType();
        Optional<ComplexWaterEntity> complexWaterEntity = Optional.empty();

        if (this.getConfigurationMapper().test(atlasEntity))
        {
            if (atlasEntity instanceof Relation || atlasEntity instanceof Area)
            {
                complexWaterEntity = Optional.of(new ComplexWaterbody(atlasEntity, waterBodyType));
            }
            else if (atlasEntity instanceof Line)
            {
                complexWaterEntity = Optional.of(new ComplexWaterway(atlasEntity, waterBodyType));
            }
        }

        return complexWaterEntity;
    }
}
