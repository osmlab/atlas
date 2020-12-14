package org.openstreetmap.atlas.utilities.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.converters.WkbMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.WktMultiPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.tags.filters.RegexTaggableFilter;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.utilities.conversion.HexStringByteArrayConverter;
import org.openstreetmap.atlas.utilities.conversion.StringToPredicateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * This class reads in a configuration file with a specific schema and creates filters based on the
 * predicates and taggable filter specified in the file. Take a look at water-handlers.json for
 * reference
 * 
 * @author matthieun
 */
public final class ConfiguredFilter implements Predicate<AtlasEntity>, Serializable
{
    public static final String CONFIGURATION_GLOBAL = "global";
    public static final String DEFAULT = "default";
    public static final ConfiguredFilter NO_FILTER = new ConfiguredFilter();
    public static final String CONFIGURATION_ROOT = CONFIGURATION_GLOBAL + ".filters";

    /*
     * JSON constants for the toJson method. We should probably handle this better so that we do not
     * duplicate String literals.
     */
    public static final String TYPE_JSON_PROPERTY_VALUE = "_filter";
    public static final String NAME_JSON_PROPERTY = "name";
    public static final String PREDICATE_JSON_PROPERTY = "predicate";
    public static final String UNSAFE_PREDICATE_JSON_PROPERTY = "unsafePredicate";
    public static final String IMPORTS_JSON_PROPERTY = "imports";
    public static final String TAGGABLE_FILTER_JSON_PROPERTY = "taggableFilter";
    public static final String REGEX_TAGGABLE_FILTER_JSON_PROPERTY = "regexTaggableFilter";
    public static final String TAGGABLE_MATCHER_JSON_PROPERTY = "taggableMatcher";
    public static final String NO_EXPANSION_JSON_PROPERTY = "noExpansion";

    private static final long serialVersionUID = 7503301238426719144L;
    private static final Logger logger = LoggerFactory.getLogger(ConfiguredFilter.class);
    private static final String CONFIGURATION_PREDICATE_COMMAND = "predicate.command";
    private static final String CONFIGURATION_PREDICATE_UNSAFE_COMMAND = "predicate.unsafeCommand";
    private static final String CONFIGURATION_PREDICATE_IMPORTS = "predicate.imports";
    private static final String CONFIGURATION_TAGGABLE_FILTER = "taggableFilter";
    private static final String CONFIGURATION_REGEX_TAGGABLE_FILTER = "regexTaggableFilter";
    private static final String CONFIGURATION_TAGGABLE_MATCHER = "taggableMatcher";
    private static final String CONFIGURATION_WKT_FILTER = "geometry.wkt";
    private static final String CONFIGURATION_WKB_FILTER = "geometry.wkb";
    private static final String CONFIGURATION_HINT_NO_EXPANSION = "hint.noExpansion";
    private static final WktMultiPolygonConverter WKT_MULTI_POLYGON_CONVERTER = new WktMultiPolygonConverter();
    private static final WkbMultiPolygonConverter WKB_MULTI_POLYGON_CONVERTER = new WkbMultiPolygonConverter();
    private static final HexStringByteArrayConverter HEX_STRING_BYTE_ARRAY_CONVERTER = new HexStringByteArrayConverter();

    private final String name;
    private final String predicate;
    private final String unsafePredicate;
    private transient Predicate<AtlasEntity> filter;
    private final List<String> imports;
    private final String taggableFilter;
    private final String regexTaggableFilter;
    private final String taggableMatcher;
    private final boolean noExpansion;
    private final List<MultiPolygon> geometryBasedFilters;

    /**
     * Create a new {@link ConfiguredFilter}.
     * <p>
     * For example, in the following json configuration:
     * 
     * <pre>
     * {@code
     * {
     *     "my":
     *     {
     *         "conf":
     *         {
     *             "filter":
     *             {
     *                 "predicate": "....",
     *                 "geometry.wkb":
     *                 [
     *                     "...", "..."
     *                 ],
     *                 "taggableFilter": "...",
     *                 "regexTaggableFilter": "...",
     *                 "taggableMatcher": "..."
     *             }
     *         }
     *     }
     * }
     * }
     * </pre>
     * 
     * the filter can be accessed using "my.conf" as root, and "filter" as name.
     * 
     * @param root
     *            The root of the configuration hierarchy, where to search for the name of the
     *            filter.
     * @param name
     *            The name of the filter, which is right under the root in the configuration
     * @param configuration
     *            The {@link Configuration} containing the configured filter
     * @return The constructed {@link ConfiguredFilter}
     */
    public static ConfiguredFilter from(final String root, final String name,
            final Configuration configuration)
    {
        if (DEFAULT.equals(name))
        {
            return getDefaultFilter(root, configuration);
        }
        if (!isPresent(root, name, configuration))
        {
            logger.warn(
                    "Attempted to create ConfiguredFilter called \"{}\" but it was not found. It will be swapped with default passthrough filter.",
                    name);
            return getDefaultFilter(root, configuration);
        }
        return new ConfiguredFilter(root, name, configuration);
    }

    public static ConfiguredFilter from(final String name, final Configuration configuration)
    {
        return from(CONFIGURATION_ROOT, name, configuration);
    }

    public static ConfiguredFilter getDefaultFilter(final String root,
            final Configuration configuration)
    {
        if (ConfiguredFilter.isPresent(root, DEFAULT, configuration))
        {
            return new ConfiguredFilter(root, DEFAULT, configuration);
        }
        return NO_FILTER;
    }

    public static ConfiguredFilter getDefaultFilter(final Configuration configuration)
    {
        return getDefaultFilter(CONFIGURATION_ROOT, configuration);
    }

    public static boolean isPresent(final String name, final Configuration configuration)
    {
        return isPresent(CONFIGURATION_ROOT, name, configuration);
    }

    public static boolean isPresent(final String root, final String name,
            final Configuration configuration)
    {
        return new ConfigurationReader(root).isPresent(configuration, name);
    }

    private ConfiguredFilter()
    {
        this(CONFIGURATION_ROOT, "NO_FILTER", new StandardConfiguration(new StringResource("{}")));
    }

    private ConfiguredFilter(final String root, final String name,
            final Configuration configuration)
    {
        this.name = name;
        String readerRoot = "";
        if (root != null && !root.isEmpty())
        {
            readerRoot = root + ".";
        }
        final ConfigurationReader reader = new ConfigurationReader(readerRoot + name);
        this.predicate = reader.configurationValue(configuration, CONFIGURATION_PREDICATE_COMMAND,
                "");
        this.unsafePredicate = reader.configurationValue(configuration,
                CONFIGURATION_PREDICATE_UNSAFE_COMMAND, "");
        this.imports = reader.configurationValue(configuration, CONFIGURATION_PREDICATE_IMPORTS,
                Lists.newArrayList());
        this.taggableFilter = reader.configurationValue(configuration,
                CONFIGURATION_TAGGABLE_FILTER, "");
        this.regexTaggableFilter = reader.configurationValue(configuration,
                CONFIGURATION_REGEX_TAGGABLE_FILTER, "");
        this.taggableMatcher = reader.configurationValue(configuration,
                CONFIGURATION_TAGGABLE_MATCHER, "");
        this.noExpansion = readBoolean(configuration, reader, CONFIGURATION_HINT_NO_EXPANSION,
                false);
        this.geometryBasedFilters = readGeometries(configuration, reader);
    }

    public List<MultiPolygon> getGeometryBasedFilters()
    {
        return new ArrayList<>(this.geometryBasedFilters);
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isNoExpansion()
    {
        return this.noExpansion;
    }

    @Override
    public boolean test(final AtlasEntity atlasEntity)
    {
        return getFilter().test(atlasEntity);
    }

    public JsonObject toJson()
    {
        final JsonObject filterObject = new JsonObject();
        filterObject.addProperty("type", TYPE_JSON_PROPERTY_VALUE);
        filterObject.addProperty(NAME_JSON_PROPERTY, this.name);
        if (!this.predicate.isEmpty())
        {
            filterObject.addProperty(PREDICATE_JSON_PROPERTY, this.predicate);
        }
        if (!this.unsafePredicate.isEmpty())
        {
            filterObject.addProperty(UNSAFE_PREDICATE_JSON_PROPERTY, this.unsafePredicate);
        }
        final JsonArray importsArray = new JsonArray();
        if (!this.imports.isEmpty())
        {
            for (final String importString : this.imports)
            {
                importsArray.add(new JsonPrimitive(importString));
            }
            filterObject.add(IMPORTS_JSON_PROPERTY, importsArray);
        }
        if (!this.taggableFilter.isEmpty())
        {
            filterObject.addProperty(TAGGABLE_FILTER_JSON_PROPERTY, this.taggableFilter); // NOSONAR
        }
        if (!this.regexTaggableFilter.isEmpty())
        {
            filterObject.addProperty(REGEX_TAGGABLE_FILTER_JSON_PROPERTY, this.regexTaggableFilter); // NOSONAR
        }
        if (!this.taggableMatcher.isEmpty())
        {
            filterObject.addProperty(TAGGABLE_MATCHER_JSON_PROPERTY, this.taggableMatcher); // NOSONAR
        }
        filterObject.addProperty(NO_EXPANSION_JSON_PROPERTY, this.noExpansion);

        return filterObject;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    private Predicate<AtlasEntity> geometryPredicate()
    {
        if (this.geometryBasedFilters.isEmpty())
        {
            return atlasEntity -> true;
        }
        else
        {
            return atlasEntity ->
            {
                for (final MultiPolygon multiPolygon : this.geometryBasedFilters)
                {
                    if (atlasEntity.intersects(multiPolygon))
                    {
                        return true;
                    }
                }
                return false;
            };
        }
    }

    private Predicate<AtlasEntity> getFilter()
    {
        if (this.filter == null)
        {
            Predicate<AtlasEntity> localTemporaryPredicate = atlasEntity -> true;
            final StringToPredicateConverter<AtlasEntity> predicateReader = new StringToPredicateConverter<>();
            predicateReader.withAddedStarImportPackages(this.imports);
            if (!this.predicate.isEmpty() && !this.unsafePredicate.isEmpty())
            {
                throw new CoreException("Cannot specify both 'command' and 'unsafeCommand'");
            }
            if (!this.predicate.isEmpty())
            {
                localTemporaryPredicate = predicateReader.convert(this.predicate);
            }
            if (!this.unsafePredicate.isEmpty())
            {
                localTemporaryPredicate = predicateReader.convertUnsafe(this.unsafePredicate);
            }
            final Predicate<AtlasEntity> localPredicate = localTemporaryPredicate;
            final TaggableFilter localTaggablefilter = TaggableFilter
                    .forDefinition(this.taggableFilter);
            final RegexTaggableFilter localRegexTaggableFilter = new RegexTaggableFilter(
                    this.regexTaggableFilter);
            final TaggableMatcher localTaggableMatcher = TaggableMatcher.from(this.taggableMatcher);
            final Predicate<AtlasEntity> geometryPredicate = geometryPredicate();
            this.filter = atlasEntity -> localPredicate.test(atlasEntity)
                    && localTaggablefilter.test(atlasEntity) && geometryPredicate.test(atlasEntity)
                    && localRegexTaggableFilter.test(atlasEntity)
                    && localTaggableMatcher.test(atlasEntity);
        }
        return this.filter;
    }

    private boolean readBoolean(final Configuration configuration, final ConfigurationReader reader,
            final String booleanName, final boolean defaultValue)
    {
        try
        {
            return reader.configurationValue(configuration, booleanName, defaultValue);
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to read \"{}\"", booleanName, e);
        }
    }

    private List<MultiPolygon> readGeometries(final Configuration configuration,
            final ConfigurationReader reader)
    {
        final List<MultiPolygon> result = new ArrayList<>();
        final String defaultValue = "N/A";
        try
        {
            final List<String> values = reader.configurationValues(configuration,
                    CONFIGURATION_WKT_FILTER, new ArrayList<>());
            if (!values.isEmpty())
            {
                result.addAll(values.stream().map(WKT_MULTI_POLYGON_CONVERTER::backwardConvert)
                        .collect(Collectors.toList()));
            }
        }
        catch (final Exception e)
        {
            final String wktString = reader.configurationValue(configuration,
                    CONFIGURATION_WKT_FILTER, defaultValue);
            if (!defaultValue.equals(wktString))
            {
                result.add(WKT_MULTI_POLYGON_CONVERTER.backwardConvert(wktString));
            }
        }
        try
        {
            final List<String> values = reader.configurationValues(configuration,
                    CONFIGURATION_WKB_FILTER, new ArrayList<>());
            if (!values.isEmpty())
            {
                result.addAll(values.stream().map(HEX_STRING_BYTE_ARRAY_CONVERTER::convert)
                        .map(WKB_MULTI_POLYGON_CONVERTER::backwardConvert)
                        .collect(Collectors.toList()));
            }
        }
        catch (final Exception e)
        {
            final String wkbString = reader.configurationValue(configuration,
                    CONFIGURATION_WKB_FILTER, defaultValue);
            if (!defaultValue.equals(wkbString))
            {
                result.add(WKB_MULTI_POLYGON_CONVERTER
                        .backwardConvert(HEX_STRING_BYTE_ARRAY_CONVERTER.convert(wkbString)));
            }
        }
        return result;
    }
}
