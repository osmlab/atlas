package org.openstreetmap.atlas.utilities.configuration;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.conversion.StringToPredicateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * This class reads in a configuration file with a specific schema and creates filters based on the
 * predicates and taggable filter specified in the file. Take a look at water-handlers.json for
 * reference
 * 
 * @author matthieun
 */
public final class ConfiguredFilter implements Predicate<AtlasEntity>, Serializable
{
    private static final long serialVersionUID = 7503301238426719144L;
    private static final Logger logger = LoggerFactory.getLogger(ConfiguredFilter.class);

    public static final String DEFAULT = "default";

    private static final String CONFIGURATION_GLOBAL = "global";
    public static final String CONFIGURATION_ROOT = CONFIGURATION_GLOBAL + ".filters";
    private static final String CONFIGURATION_PREDICATE_COMMAND = "predicate.command";
    private static final String CONFIGURATION_PREDICATE_IMPORTS = "predicate.imports";
    private static final String CONFIGURATION_TAGGABLE_FILTER = "taggableFilter";

    public static final ConfiguredFilter NO_FILTER = new ConfiguredFilter();

    private final String name;
    private final String predicate;
    private final List<String> imports;
    private final String taggableFilter;
    private transient Predicate<AtlasEntity> filter;

    public static ConfiguredFilter from(final String name, final Configuration configuration)
    {
        if (DEFAULT.equals(name))
        {
            return getDefaultFilter(configuration);
        }
        if (!isPresent(name, configuration))
        {
            logger.warn(
                    "Attempted to create ConfiguredFilter called \"{}\" but it was not found. It will be swapped with default passthrough filter.",
                    name);
            return getDefaultFilter(configuration);
        }
        return new ConfiguredFilter(name, configuration);
    }

    public static ConfiguredFilter getDefaultFilter(final Configuration configuration)
    {
        if (ConfiguredFilter.isPresent(DEFAULT, configuration))
        {
            return new ConfiguredFilter(DEFAULT, configuration);
        }
        return NO_FILTER;
    }

    public static boolean isPresent(final String name, final Configuration configuration)
    {
        return new ConfigurationReader(CONFIGURATION_ROOT).isPresent(configuration, name);
    }

    private ConfiguredFilter()
    {
        this("NO_FILTER", new StandardConfiguration(new StringResource("{}")));
    }

    private ConfiguredFilter(final String name, final Configuration configuration)
    {
        this.name = name;
        final ConfigurationReader reader = new ConfigurationReader(CONFIGURATION_ROOT + "." + name);
        this.predicate = reader.configurationValue(configuration, CONFIGURATION_PREDICATE_COMMAND,
                "");
        this.imports = reader.configurationValue(configuration, CONFIGURATION_PREDICATE_IMPORTS,
                Lists.newArrayList());
        this.taggableFilter = reader.configurationValue(configuration,
                CONFIGURATION_TAGGABLE_FILTER, "");
    }

    @Override
    public boolean test(final AtlasEntity atlasEntity)
    {
        return getFilter().test(atlasEntity);
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    private Predicate<AtlasEntity> getFilter()
    {
        if (this.filter == null)
        {
            Predicate<AtlasEntity> localTemporaryPredicate = atlasEntity -> true;
            if (!this.predicate.isEmpty())
            {
                final StringToPredicateConverter<AtlasEntity> predicateReader = new StringToPredicateConverter<>();
                predicateReader.withAddedStarImportPackages(this.imports);
                localTemporaryPredicate = predicateReader.convert(this.predicate);
            }
            final Predicate<AtlasEntity> localPredicate = localTemporaryPredicate;
            final TaggableFilter localTaggablefilter = TaggableFilter
                    .forDefinition(this.taggableFilter);
            this.filter = atlasEntity -> localPredicate.test(atlasEntity)
                    && localTaggablefilter.test(atlasEntity);
        }
        return this.filter;
    }
}
