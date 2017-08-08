package org.openstreetmap.atlas.tags.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Taggable} filter that relies on a String definition
 * <p>
 * Examples of String definition:
 * <p>
 * highway=motorway AND name=[not empty] <br>
 * highway-&gt;motorway&amp;name-&gt;*
 * <p>
 * highway=motorway OR oneway=[not]yes <br>
 * highway-&gt;motorway|oneway-&gt;!yes
 * <p>
 * highway=motorway OR [no "name" tag] <br>
 * highway-&gt;motorway|name-&gt;!
 * <p>
 * amenity=bus_station OR highway=bus_stop OR ( (bus=* OR trolleybus=*) AND
 * public_transport=[stop_position OR platform OR station] ) <br>
 * amenity-&gt;bus_station|highway-&gt;bus_stop|bus-&gt;*^trolleybus-&gt;*&amp;public_transport-&gt;
 * stop_position, platform,station
 *
 * @author matthieun
 */
public class TaggableFilter implements Predicate<Taggable>, Serializable
{
    /**
     * A tag group is a set of tags that are sufficient for an entity to have to be valid. It can be
     * made of combinations of tags, hence the set of multimaps.
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

    private static final Logger logger = LoggerFactory.getLogger(TaggableFilter.class);

    private static final String KEYS_SEPARATOR = "|";
    private static final String VALUES_SEPARATOR = ",";
    private static final String KEY_VALUE_SEPARATOR = "->";
    private static final String COUPLED_KEYS_SEPARATOR = "&";
    private static final String COUPLED_KEYS_GROUP = "^";
    private static final long serialVersionUID = -7554425273207469255L;
    private transient Validators validators;
    private final Class<?> childrenOf;
    private final Set<TagGroup> allowedTags;

    public TaggableFilter(final String definition)
    {
        this(definition, Taggable.class);
    }

    public TaggableFilter(final String definition, final Class<?> childrenOf)
    {
        this.childrenOf = childrenOf;
        try
        {
            if (definition.isEmpty())
            {
                this.allowedTags = new HashSet<>();
            }
            else
            {
                this.allowedTags = setAllowedTags(definition);
                checkAllowedTags();
            }
        }
        catch (final Throwable error)
        {
            throw new CoreException("Unable to setup TaggableFilter with definition {}", definition,
                    error);
        }
    }

    public synchronized Validators getValidators()
    {
        if (this.validators == null)
        {
            this.validators = new Validators(childrenOf);
        }
        return this.validators;
    }

    @Override
    public boolean test(final Taggable taggable)
    {
        if (this.allowedTags.isEmpty())
        {
            // Assume there is no filter.
            return true;
        }
        for (final TagGroup group : this.allowedTags)
        {
            if (isValid(taggable, group))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        return this.allowedTags.toString();
    }

    protected List<String> checkAllowedTags()
    {
        final List<String> failedValidation = new ArrayList<>();
        for (final TagGroup tagGroup : this.allowedTags)
        {
            for (final MultiMap<String, String> valuesWorkingTogether : tagGroup)
            {
                for (final String key : valuesWorkingTogether.keySet())
                {
                    // Check the key
                    if (this.getValidators().findClassDefining(key).isPresent())
                    {
                        for (final String value : valuesWorkingTogether.get(key))
                        {
                            String parsedValue = value;
                            if (parsedValue.startsWith("!"))
                            {
                                parsedValue = parsedValue.substring(1);
                                if (parsedValue.isEmpty())
                                {
                                    continue;
                                }
                            }
                            if (parsedValue.startsWith("*"))
                            {
                                continue;
                            }
                            // Check the value
                            if (!this.getValidators().isValidFor(key, parsedValue))
                            {
                                logger.warn(
                                        "Unable to recognize tag value {} associated with tag key {} in {}",
                                        parsedValue, key, tagGroup);
                                failedValidation.add(parsedValue);
                            }
                        }
                    }
                    else
                    {
                        logger.warn("Unable to recognize tag key {} in {}", key, tagGroup);
                        failedValidation.add(key);
                    }
                }
            }
        }
        return failedValidation;
    }

    /**
     * @param taggable
     *            The taggable to test
     * @param andGroup
     *            The group of tags it needs to have
     * @return True if this {@link Taggable} contains all the tags defined in the tag group
     */
    private boolean isValid(final Taggable taggable, final MultiMap<String, String> andGroup)
    {
        for (final String key : andGroup.keySet())
        {
            if (taggable.containsValue(key, andGroup.get(key)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param taggable
     *            The taggable to test
     * @param tagGroup
     *            The collection of groups of tags it needs to have
     * @return True if this {@link Taggable} contains at least one of the tag groups defined in the
     *         collection of tag groups.
     */
    private boolean isValid(final Taggable taggable, final TagGroup tagGroup)
    {
        // A tag group is counted only if the and items are all verified
        for (final MultiMap<String, String> andGroup : tagGroup)
        {
            if (!isValid(taggable, andGroup))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @param definition
     *            The filter definition
     * @return The {@link TagGroup} that corresponds to that definition
     */
    private Set<TagGroup> setAllowedTags(final String definition)
    {
        final Set<TagGroup> allowedTags = new HashSet<>();
        final StringList orGroups = StringList.split(definition, KEYS_SEPARATOR);
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
                    final StringList keyValue = StringList.split(caret, KEY_VALUE_SEPARATOR);
                    final String key = keyValue.get(0);
                    final StringList values = StringList.split(keyValue.get(1), VALUES_SEPARATOR);
                    values.forEach(value ->
                    {
                        // Each "," possible value
                        tags.add(key.toLowerCase(), value.toLowerCase());
                    });
                });
                tagGroup.addTags(tags);
            });
            allowedTags.add(tagGroup);
        });
        return allowedTags;
    }
}
