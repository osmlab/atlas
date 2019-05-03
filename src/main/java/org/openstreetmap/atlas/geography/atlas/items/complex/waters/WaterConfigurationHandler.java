package org.openstreetmap.atlas.geography.atlas.items.complex.waters;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.ConfigurationReader;
import org.openstreetmap.atlas.utilities.configuration.ConfiguredFilter;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

/**
 * This class read in a resource and creates {@link ConfiguredFilter} for each water body type
 * 
 * @author sbhalekar
 */
public class WaterConfigurationHandler
{
    /**
     * Default water handler configuration from the resources
     */
    public static final String WATER_RESOURCE = "water-handlers.json";

    private final Configuration waterHandlerConfiguration;

    private final Map<String, ConfiguredFilter> waterHandlers;

    public WaterConfigurationHandler()
    {
        this(new InputStreamResource(
                () -> WaterConfigurationHandler.class.getResourceAsStream(WATER_RESOURCE)));
    }

    public WaterConfigurationHandler(final Resource resource)
    {
        this(new StandardConfiguration(resource));
    }

    public WaterConfigurationHandler(final Configuration configuration)
    {
        this.waterHandlerConfiguration = configuration;
        this.waterHandlers = readConfiguration();
    }

    public Configuration getWaterHandlerConfiguration()
    {
        return this.waterHandlerConfiguration;
    }

    public Map<String, ConfiguredFilter> getWaterHandlers()
    {
        return this.waterHandlers;
    }

    private Map<String, ConfiguredFilter> readConfiguration()
    {
        final Map<String, ConfiguredFilter> waterHandlers = new HashMap<>();
        final ConfigurationReader reader = new ConfigurationReader(
                ConfiguredFilter.CONFIGURATION_ROOT);
        final Set<String> waterBodyTypes = reader.configurationValue(this.waterHandlerConfiguration,
                Map<String, Object>::keySet);

        waterBodyTypes.forEach(waterBodyType ->
        {
            waterHandlers.put(waterBodyType.toLowerCase(),
                    ConfiguredFilter.from(waterBodyType, this.waterHandlerConfiguration));
        });

        return waterHandlers;
    }
}
