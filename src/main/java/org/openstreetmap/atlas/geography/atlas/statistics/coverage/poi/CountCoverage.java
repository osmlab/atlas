package org.openstreetmap.atlas.geography.atlas.statistics.coverage.poi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.Coverage;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is specifically made for feature counts. It can count shops, lakes, bus stops, etc.
 * This is simpler than other metrics, so all the definitions of counts to run are defined in a
 * resource (text file for example), one per line.
 *
 * @author matthieun
 * @param <T>
 *            The type of {@link AtlasEntity} to count.
 */
public abstract class CountCoverage<T extends AtlasEntity> extends Coverage<T>
{
    /**
     * A tag group is a set of tags that are sufficient for an entity to have to be counted. It can
     * be made of combinaisons of tags, hence the set of multimaps.
     *
     * @author matthieun
     */
    public static class TagGroup implements Iterable<MultiMap<String, String>>, Serializable
    {
        private static final long serialVersionUID = -3963233623607200077L;
        private final Set<MultiMap<String, String>> valuesWorkingTogether = new HashSet<>();

        public void addTags(final MultiMap<String, String> tags)
        {
            this.valuesWorkingTogether.add(tags);
        }

        @Override
        public Iterator<MultiMap<String, String>> iterator()
        {
            return this.valuesWorkingTogether.iterator();
        }

        @Override
        public String toString()
        {
            return this.valuesWorkingTogether.toString();
        }
    }

    private static final String TYPE_SEPARATOR = ";";
    private static final String KEYS_SEPARATOR = "|";
    private static final String VALUES_SEPARATOR = ",";
    private static final String KEY_VALUE_SEPARATOR = "->";
    private static final String COUPLED_KEYS_SEPARATOR = "&";
    private static final String COUPLED_KEYS_GROUP = "^";

    private final Set<TagGroup> tagGroups;

    /**
     * Parse a count configuration file
     *
     * @param atlas
     *            The Atlas to crawl
     * @param coverages
     *            The configuration file
     * @return All the {@link CountCoverage}s defined in the file
     */
    public static Iterable<CountCoverage<AtlasEntity>> parseCountCoverages(final Atlas atlas,
            final Iterable<String> coverages)
    {
        final List<CountCoverage<AtlasEntity>> result = new ArrayList<>();
        final Iterable<String> filteredCoverages = Iterables.filter(coverages,
                line -> !(line.startsWith("#") || "".equals(line)));
        filteredCoverages.forEach(definition ->
        {
            try
            {
                // Each line
                final StringList split = StringList.split(definition, TYPE_SEPARATOR);
                final StringList sources = StringList.split(split.get(1), VALUES_SEPARATOR);
                final String type = split.get(0);
                final Set<TagGroup> allowedTags = new HashSet<>();
                final StringList orGroups = StringList.split(split.get(2), KEYS_SEPARATOR);
                orGroups.forEach(orGroup ->
                {
                    // Each "|" group
                    final TagGroup tagGroup = new TagGroup();
                    final StringList andGroups = StringList.split(orGroup, COUPLED_KEYS_SEPARATOR);
                    andGroups.forEach(andGroup ->
                    {
                        final MultiMap<String, String> tags = new MultiMap<>();
                        final StringList carets = StringList.split(andGroup, COUPLED_KEYS_GROUP);
                        carets.forEach(caret ->
                        {
                            final StringList keyValue = StringList.split(caret,
                                    KEY_VALUE_SEPARATOR);
                            final String key = keyValue.get(0);
                            final StringList values = StringList.split(keyValue.get(1),
                                    VALUES_SEPARATOR);
                            values.forEach(value ->
                            {
                                // Each "," possible value
                                tags.add(key, value);
                            });
                        });
                        tagGroup.addTags(tags);
                    });
                    allowedTags.add(tagGroup);
                });
                result.add(new CountCoverage<AtlasEntity>(LoggerFactory.getLogger(type), atlas)
                {
                    @Override
                    protected Iterable<AtlasEntity> getEntities()
                    {
                        if (sources.contains("all"))
                        {
                            return getAtlas();
                        }
                        final List<Iterable<? extends AtlasEntity>> result = new ArrayList<>();
                        if (sources.contains("nodes"))
                        {
                            result.add(getAtlas().nodes());
                        }
                        if (sources.contains("edges"))
                        {
                            result.add(getAtlas().edges());
                        }
                        if (sources.contains("areas"))
                        {
                            result.add(getAtlas().areas());
                        }
                        if (sources.contains("lines"))
                        {
                            result.add(getAtlas().lines());
                        }
                        if (sources.contains("points"))
                        {
                            result.add(getAtlas().points());
                        }
                        if (sources.contains("relations"))
                        {
                            result.add(getAtlas().relations());
                        }
                        return new MultiIterable<>(result);
                    }

                    @Override
                    protected String type()
                    {
                        return type;
                    }

                    @Override
                    protected Set<TagGroup> validKeyValuePairs()
                    {
                        return allowedTags;
                    }
                });
            }
            catch (final Exception e)
            {
                throw new CoreException("Error parsing {}", definition, e);
            }
        });
        return result;
    }

    public CountCoverage(final Logger logger, final Atlas atlas)
    {
        super(logger, atlas);
        this.tagGroups = validKeyValuePairs();
    }

    public CountCoverage(final Logger logger, final Atlas atlas, final Predicate<T> filter)
    {
        super(logger, atlas, filter);
        this.tagGroups = validKeyValuePairs();
    }

    @Override
    protected CoverageType coverageType()
    {
        return CoverageType.COUNT;
    }

    @Override
    protected Set<String> getKeys(final AtlasEntity item)
    {
        // Only All will be represented here
        return new HashSet<>();
    }

    @Override
    protected String getUnit()
    {
        return "count unit";
    }

    @Override
    protected double getValue(final T item)
    {
        return 1;
    }

    @Override
    protected boolean isCounted(final T item)
    {
        if (this.tagGroups.isEmpty())
        {
            // Assume there is no filter.
            return true;
        }
        for (final TagGroup group : this.tagGroups)
        {
            if (isCounted(item, group))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String subType()
    {
        return "true";
    }

    /**
     * @return All the tag key/value pairs that an entity needs to have to be counted. Extending
     *         this definition, if a TagGroup is empty, then all features are valid and will be
     *         counted.
     */
    protected abstract Set<TagGroup> validKeyValuePairs();

    private boolean isCounted(final AtlasEntity item, final MultiMap<String, String> andGroup)
    {
        for (final String key : andGroup.keySet())
        {
            if (item.containsValue(key, andGroup.get(key)))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isCounted(final T item, final TagGroup tagGroup)
    {
        // A tag group is counted only if the and items are all verified
        for (final MultiMap<String, String> andGroup : tagGroup)
        {
            if (!isCounted(item, andGroup))
            {
                return false;
            }
        }
        return true;
    }
}
