package org.openstreetmap.atlas.geography.atlas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load an {@link Atlas} from a {@link Resource} or an {@link Iterable} of {@link Resource}s.
 * Supports also loading based on a resource name filter. Note that by default, this class will
 * filter the provided {@link Iterable} and remove any {@link Resource} that does not have a valid
 * atlas file extension (defined in the {@link FileSuffix} enum). This funtionality can be disabled
 * by calling {@link AtlasResourceLoader#withAtlasFileExtensionFilterSetTo(boolean)} with
 * {@code false}. Disabling this functionality is useful if combining this class with atlases
 * fetched from a cache that does not respect the .atlas file extension convention.
 *
 * @author cstaylor
 * @author mgostintsev
 * @author matthieun
 * @author remegraw
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

    public static final Predicate<Resource> IS_ATLAS = FileSuffix.resourceFilter(FileSuffix.ATLAS)
            .or(FileSuffix.resourceFilter(FileSuffix.ATLAS, FileSuffix.GZIP));

    private static final Logger logger = LoggerFactory.getLogger(AtlasResourceLoader.class);

    private final Predicate<Resource> alwaysTrueAtlasFilter = resource -> true;

    private Predicate<Resource> resourceFilter;
    private Predicate<AtlasEntity> atlasEntityFilter;
    private String multiAtlasName;
    private boolean filterForAtlasFileExtension = true;

    public AtlasResourceLoader()
    {
        this.resourceFilter = resource -> true;
        this.atlasEntityFilter = null;
    }

    public Atlas load(final Iterable<? extends Resource> input)
    {
        final Predicate<Resource> toggleableAtlasFileFilter = this.filterForAtlasFileExtension
                ? IS_ATLAS : this.alwaysTrueAtlasFilter;

        final List<Resource> resources = Iterables.stream(input).flatMap(this::resourcesIn)
                .filter(toggleableAtlasFileFilter).filter(this.resourceFilter).collectToList();
        final long size = resources.size();
        if (size == 1)
        {
            Atlas result = PackedAtlas.load(resources.get(0));
            if (this.atlasEntityFilter != null)
            {
                final Optional<Atlas> subAtlas = result.subAtlas(this.atlasEntityFilter);
                result = subAtlas.isPresent() ? subAtlas.get() : null;
            }
            return result;
        }
        else if (size > 1)
        {
            final MultiAtlas result = this.atlasEntityFilter == null
                    ? MultiAtlas.loadFromPackedAtlas(resources)
                    : MultiAtlas.loadFromPackedAtlas(resources, this.atlasEntityFilter);
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
     * Optionally add an {@link AtlasEntity} filter
     *
     * @param filter
     *            filter which {@link AtlasEntity}s to include/exclude in the {@link Atlas}
     */
    public void setAtlasEntityFilter(final Predicate<AtlasEntity> filter)
    {
        this.atlasEntityFilter = filter;
    }

    /**
     * Optionally add a {@link Resource} filter
     *
     * @param filter
     *            filter which {@link Resource} to load
     */
    public void setResourceFilter(final Predicate<Resource> filter)
    {
        this.resourceFilter = filter;
    }

    /**
     * Optionally add an {@link AtlasEntity} filter
     *
     * @param filter
     *            filter which {@link AtlasEntity}s to include/exclude in the {@link Atlas}
     * @return fluent interface requires this be returned
     */
    public AtlasResourceLoader withAtlasEntityFilter(final Predicate<AtlasEntity> filter)
    {
        setAtlasEntityFilter(filter);
        return this;
    }

    /**
     * Enable or disable atlas file extension filtering on this loader.
     *
     * @param value
     *            whether to enable or disable
     * @return the modified {@link AtlasResourceLoader}
     */
    public AtlasResourceLoader withAtlasFileExtensionFilterSetTo(final boolean value)
    {
        this.filterForAtlasFileExtension = value;
        return this;
    }

    public AtlasResourceLoader withMultiAtlasName(final String multiAtlasName)
    {
        this.multiAtlasName = multiAtlasName;
        return this;
    }

    /**
     * Optionally add a {@link Resource} filter
     *
     * @param filter
     *            filter which {@link Resource} to load
     * @return fluent interface requires this be returned
     */
    public AtlasResourceLoader withResourceFilter(final Predicate<Resource> filter)
    {
        setResourceFilter(filter);
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
