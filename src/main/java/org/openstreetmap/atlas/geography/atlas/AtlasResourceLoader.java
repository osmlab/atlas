package org.openstreetmap.atlas.geography.atlas;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load an {@link Atlas} from a {@link Resource} or an {@link Iterable} of {@link Resource}s.
 * Supports also loading based on a resource name filter.
 *
 * @author cstaylor
 * @author mgostintsev
 * @author matthieun
 */
public class AtlasResourceLoader
{
    /**
     * @author matthieun
     */
    protected static final class AtlasFileSelector
    {
        public List<Resource> select(final File file)
        {
            final List<Resource> result = new ArrayList<>();
            if (file != null && file.exists())
            {
                file.listFilesRecursively().forEach(child ->
                {
                    if (child.isGzipped())
                    {
                        child.setDecompressor(Decompressor.GZIP);
                    }
                    result.add(child);
                });
            }
            else
            {
                logger.warn("File {} does not exist.", file);
            }
            return result;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AtlasResourceLoader.class);

    private Predicate<Resource> filter;
    private String multiAtlasName;

    public AtlasResourceLoader()
    {
        this.filter = resource -> true;
    }

    public Atlas load(final Iterable<? extends Resource> input)
    {
        final List<Resource> resources = Iterables.stream(input).flatMap(this::resourcesIn)
                .filter(Atlas::isAtlas).filter(this.filter).collectToList();
        final long size = resources.size();
        if (size == 1)
        {
            return PackedAtlas.load(resources.get(0));
        }
        else if (size > 1)
        {
            final MultiAtlas result = MultiAtlas.loadFromPackedAtlas(resources);
            if (this.multiAtlasName != null)
            {
                result.setName(this.multiAtlasName);
            }
            return result;
        }
        else
        {
            return null;
        }
    }

    public Atlas load(final Resource... resource)
    {
        return load(Iterables.from(resource));
    }

    /**
     * Optionally add a filter
     *
     * @param filter
     *            filter object with includes and excludes
     */
    public void setFilter(final Predicate<Resource> filter)
    {
        this.filter = filter;
    }

    /**
     * Optionally add a filter
     *
     * @param filter
     *            filter object with includes and excludes
     * @return fluent interface requires this be returned
     */
    public AtlasResourceLoader withFilter(final Predicate<Resource> filter)
    {
        setFilter(filter);
        return this;
    }

    public AtlasResourceLoader withMultiAtlasName(final String multiAtlasName)
    {
        this.multiAtlasName = multiAtlasName;
        return this;
    }

    private List<Resource> resourcesIn(final Resource resource)
    {
        final List<Resource> result = new ArrayList<>();
        if (resource != null)
        {
            if (resource instanceof File)
            {
                result.addAll(new AtlasFileSelector().select((File) resource));
            }
            else
            {
                result.add(resource);
            }
        }
        return result;
    }
}
