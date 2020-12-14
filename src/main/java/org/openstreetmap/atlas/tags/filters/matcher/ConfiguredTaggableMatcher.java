package org.openstreetmap.atlas.tags.filters.matcher;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * List of {@link TaggableMatcher}s defined in a configuration object.
 *
 * @author lcram
 */
public class ConfiguredTaggableMatcher implements Predicate<Taggable>, Serializable
{
    public static final String MATCHERS_CONFIGURATION_NAME = "matchers";

    private static final long serialVersionUID = -1870768831799297979L;

    private final List<TaggableMatcher> matchers;

    @SuppressWarnings("unchecked")
    public ConfiguredTaggableMatcher(final Configuration configuration)
    {
        this.matchers = ((List<String>) configuration.get(MATCHERS_CONFIGURATION_NAME).valueOption()
                .orElseThrow(() -> new CoreException("No matchers defined in configuration {}",
                        configuration))).stream().map(TaggableMatcher::from)
                                .collect(Collectors.toList());
    }

    @Override
    public boolean test(final Taggable taggable)
    {
        for (final TaggableMatcher matcher : this.matchers)
        {
            if (!matcher.test(taggable))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString()
    {
        return this.matchers.toString();
    }

    protected List<TaggableMatcher> getMatchers()
    {
        return this.matchers;
    }
}
