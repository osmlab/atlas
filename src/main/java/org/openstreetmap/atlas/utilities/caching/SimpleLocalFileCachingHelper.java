package org.openstreetmap.atlas.utilities.caching;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * A simple example that shows how to use and extend the {@link AbstractResourceCachingHelper}.
 *
 * @author lcram
 */
public class SimpleLocalFileCachingHelper extends AbstractResourceCachingHelper
{
    private static final String PROPERTY_USER_HOME = "user.home";

    public Resource fetchLocalFile()
    {
        return new File(this.getPathToResource());
    }

    /**
     * This simple implementation of getResource simply leverages the caching strategy with a local
     * default fetcher.
     */
    @Override
    public Optional<Resource> getResource()
    {
        return this.withFetcher(this::fetchLocalFile).getResourceWithCachingStrategy();
    }

    /**
     * A simple extension that allows users to specify a path to a resource relative to their home
     * directory.
     *
     * @param relativePathToResource
     *            path to a resource relative to the user's home directory
     * @return the updated {@link SimpleLocalFileCachingHelper}
     */
    public SimpleLocalFileCachingHelper withPathToResourceRelativeToHome(
            final String relativePathToResource)
    {
        final String home = System.getProperty(PROPERTY_USER_HOME);
        if (home == null)
        {
            throw new CoreException("Property {} could not be read.", PROPERTY_USER_HOME);
        }
        final Path homePath = Paths.get(home);
        final Path pathToResource = Paths.get(homePath.toString(), relativePathToResource);
        this.setPathToResource(pathToResource.toString());

        return this;
    }
}
