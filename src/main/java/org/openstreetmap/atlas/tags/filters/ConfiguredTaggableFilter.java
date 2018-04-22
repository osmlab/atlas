package org.openstreetmap.atlas.tags.filters;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * List of filters defined in a configuration object.
 *
 * @author matthieun
 */
public class ConfiguredTaggableFilter implements Predicate<Taggable>, Serializable
{
    private static final long serialVersionUID = -3849791821180104953L;
    public static final String FILTERS_CONFIGURATION_NAME = "filters";

    private final List<TaggableFilter> filters;

    @SuppressWarnings("unchecked")
    public ConfiguredTaggableFilter(final Configuration configuration)
    {
        this.filters = ((List<String>) configuration.get(FILTERS_CONFIGURATION_NAME).valueOption()
                .orElseThrow(() -> new CoreException("No filters defined in configuration {}",
                        configuration))).stream().map(TaggableFilter::new)
                                .collect(Collectors.toList());
    }

    @Override
    public boolean test(final Taggable taggable)
    {
        for (final TaggableFilter filter : this.filters)
        {
            if (!filter.test(taggable))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString()
    {
        return this.filters.toString();
    }

    protected List<TaggableFilter> getFilters()
    {
        return this.filters;
    }
}
