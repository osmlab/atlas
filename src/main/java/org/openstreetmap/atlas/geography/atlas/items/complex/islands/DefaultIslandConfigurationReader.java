package org.openstreetmap.atlas.geography.atlas.items.complex.islands;

import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.WaterIslandConfigurationReader;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.configuration.ConfiguredFilter;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

/**
 * Class used by {@link ComplexIslandFinder} to read the island configuration and map
 * {@link AtlasEntity} to {@link ComplexIsland}
 *
 * @author sbhalekar
 */
public class DefaultIslandConfigurationReader
        extends WaterIslandConfigurationReader<ConfiguredFilter>
{
    public DefaultIslandConfigurationReader(final Resource resource)
    {
        super(resource);
    }

    public DefaultIslandConfigurationReader(final String resourceName)
    {
        this(new InputStreamResource(
                () -> DefaultIslandConfigurationReader.class.getResourceAsStream(resourceName)));
    }

    @Override
    protected Optional<ComplexIsland> createComplexEntity(final AtlasEntity atlasEntity)
    {
        return this.getConfigurationMapper().test(atlasEntity)
                ? Optional.of(new ComplexIsland(atlasEntity))
                : Optional.empty();
    }

    @Override
    protected ConfiguredFilter readConfiguration(final Resource configurationResource)
    {
        return ConfiguredFilter.getDefaultFilter(new StandardConfiguration(configurationResource));
    }
}
