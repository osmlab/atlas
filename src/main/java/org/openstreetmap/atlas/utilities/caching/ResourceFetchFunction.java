package org.openstreetmap.atlas.utilities.caching;

import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * Defines an interface for a generic resource fetching function.
 *
 * @author lcram
 */
@FunctionalInterface
public interface ResourceFetchFunction
{
    /**
     * Fetch a {@link Resource}, given some path to the resource and a configuration.
     *
     * @param path
     *            the path to the desired {@link Resource}
     * @param configurationMap
     *            the configuration map, if one is necessary (eg. for hadoop files)
     * @return the resource corresponding to the given path with the given configuration
     */
    Resource fetch();
}
