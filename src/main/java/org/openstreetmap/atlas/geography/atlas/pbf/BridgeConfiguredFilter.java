package org.openstreetmap.atlas.geography.atlas.pbf;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.tags.filters.ConfiguredTaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.ConfigurationReader;
import org.openstreetmap.atlas.utilities.configuration.ConfiguredFilter;

import com.google.common.collect.Lists;

/**
 * This class is a bridge between {@link ConfiguredTaggableFilter} and {@link ConfiguredFilter} for
 * use in {@link AtlasLoadingOption}, as a backwards-compatible option until
 * {@link ConfiguredFilter} is the only option used.
 * 
 * @author matthieun
 */
public class BridgeConfiguredFilter implements Predicate<AtlasEntity>, Serializable
{
    private static final long serialVersionUID = -1496420126649881929L;
    private static final String EMPTY_MARKER = "N/A";

    private ConfiguredTaggableFilter configuredTaggableFilter;
    private ConfiguredFilter configuredFilter;

    public BridgeConfiguredFilter(final String root, final String name,
            final Configuration configuration)
    {
        final ConfigurationReader reader = new ConfigurationReader("");
        final List<String> configuredTaggableFilterFilters = reader.configurationValues(
                configuration, ConfiguredTaggableFilter.FILTERS_CONFIGURATION_NAME,
                Lists.newArrayList(EMPTY_MARKER));
        if (configuredTaggableFilterFilters.size() == 1
                && EMPTY_MARKER.equals(configuredTaggableFilterFilters.get(0)))
        {
            // It is a new ConfiguredFilter
            this.configuredFilter = ConfiguredFilter.from(root, name, configuration);
        }
        else
        {
            // It is a legacy ConfiguredTaggableFilter
            this.configuredTaggableFilter = new ConfiguredTaggableFilter(configuration);
        }
    }

    @Override
    public boolean test(final AtlasEntity atlasEntity)
    {
        return this.configuredFilter != null ? this.configuredFilter.test(atlasEntity)
                : this.configuredTaggableFilter.test(atlasEntity);
    }
}
